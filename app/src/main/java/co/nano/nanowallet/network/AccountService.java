package co.nano.nanowallet.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.LinkedTreeMap;

import java.math.BigInteger;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.inject.Inject;

import co.nano.nanowallet.bus.RxBus;
import co.nano.nanowallet.bus.SocketError;
import co.nano.nanowallet.model.Address;
import co.nano.nanowallet.model.Credentials;
import co.nano.nanowallet.model.NanoWallet;
import co.nano.nanowallet.model.PreconfiguredRepresentatives;
import co.nano.nanowallet.network.model.BaseNetworkModel;
import co.nano.nanowallet.network.model.BlockTypes;
import co.nano.nanowallet.network.model.request.AccountCheckRequest;
import co.nano.nanowallet.network.model.request.AccountHistoryRequest;
import co.nano.nanowallet.network.model.request.OpenBlock;
import co.nano.nanowallet.network.model.request.PendingTransactionsRequest;
import co.nano.nanowallet.network.model.request.ProcessRequest;
import co.nano.nanowallet.network.model.request.ReceiveBlock;
import co.nano.nanowallet.network.model.request.SendBlock;
import co.nano.nanowallet.network.model.request.SubscribeRequest;
import co.nano.nanowallet.network.model.request.WorkRequest;
import co.nano.nanowallet.network.model.response.PendingTransactionResponseItem;
import co.nano.nanowallet.network.model.response.ProcessResponse;
import co.nano.nanowallet.network.model.response.SubscribeResponse;
import co.nano.nanowallet.network.model.response.TransactionResponse;
import co.nano.nanowallet.network.model.response.WorkResponse;
import co.nano.nanowallet.ui.common.ActivityWithComponent;
import co.nano.nanowallet.util.ExceptionHandler;
import co.nano.nanowallet.util.SharedPreferencesUtil;
import io.realm.Realm;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import timber.log.Timber;

/**
 * Methods for calling the account service
 */

public class AccountService {
    private WebSocket websocket;
    private OkHttpClient client;
    private static final String CONNECTION_URL = "wss://light.nano.org:443";
    //private static final String CONNECTION_URL = "wss://raicast.lightrai.com:443";
    private Address address;
    private Integer blockCount;
    private BlockTypes recentWorkRequestType;
    private PendingTransactionResponseItem recentPendingTransactionResponseItem;
    private int errorCount;
    private static final int MAX_ERROR_COUNT = 3;
    private static final int TIMEOUT_MILLISECONDS = 3000;

    @Inject
    Realm realm;

    @Inject
    SharedPreferencesUtil sharedPreferencesUtil;

    @Inject
    NanoWallet wallet;

    @Inject
    Gson gson;

    public AccountService(Context context) {
        // init dependency injection
        if (context instanceof ActivityWithComponent) {
            ((ActivityWithComponent) context).getActivityComponent().inject(this);
        }
    }

    public void open() {
        // get user's address
        address = getAddress();

        blockCount = -1;

        // initialize the web socket
        if (websocket == null) {
            initWebSocket();
        }

        processNextTransaction();
    }

    /**
     * Initialize websocket and event listeners
     */
    private void initWebSocket() {
        // create websocket
        client = new OkHttpClient.Builder()
                .readTimeout(TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS)
                .writeTimeout(TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS)
                .connectTimeout(TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS).build();
        Request request = new Request.Builder().url(CONNECTION_URL).build();
        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                super.onOpen(webSocket, response);
                Timber.d("OPENED");
                requestUpdate();
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);
                Timber.d("RECEIVED %s", text);
                handleMessage(text);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                super.onClosing(webSocket, code, reason);
                Timber.d("CLOSING");
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                super.onClosed(webSocket, code, reason);
                Timber.d("CLOSED");
                AccountService.this.websocket = null;
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
                super.onFailure(webSocket, t, response);
                ExceptionHandler.handle(t);
                webSocket.cancel();
                AccountService.this.websocket = null;
                post(new SocketError(t));
            }
        };

        websocket = client.newWebSocket(request, listener);

        client.dispatcher().executorService().shutdown();
    }

    /**
     * Generic message hand'er. Convert to an object and process or post to bus.
     *
     * @param message String message
     */
    private void handleMessage(String message) {
        BaseNetworkModel event = null;
        try {
            event = gson.fromJson(message, BaseNetworkModel.class);
        } catch (JsonSyntaxException e) {
            ExceptionHandler.handle(e);
        }

        if (event != null && event.getMessageType() == null) {
            // try parsing to a linked tree map object if event type is null
            // for now, these are the blocks that come back from a pending request
            handleNullMessageTypes(message);
        } else if (event != null && event instanceof WorkResponse) {
            // process a work response
            processWork((WorkResponse) event);
        } else if (event != null && event instanceof TransactionResponse) {
            // a transaction was pushed to the app, so push onto pending transaction queue
            TransactionResponse transactionResponse = (TransactionResponse) event;
            PendingTransactionResponseItem pendingTransactionResponseItem = new PendingTransactionResponseItem(
                    transactionResponse.getAccount(), transactionResponse.getAmount(), transactionResponse.getHash());
            wallet.getPendingTransactions().add(pendingTransactionResponseItem);
            processNextTransaction();
        } else if (event != null && event instanceof ProcessResponse) {
            handleProcessResponse((ProcessResponse) event);
        } else {
            // keep track of current block count for more efficient requests
            if (event instanceof SubscribeResponse) {
                blockCount = ((SubscribeResponse) event).getBlock_count();
            }

            // post whatever the response type is to the bus
            if (event != null) {
                post(event);
            }
        }
    }

    /**
     * Post event to bus on UI thread
     *
     * @param event Object to post to bus
     */
    private void post(Object event) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> RxBus.get().post(event));
    }


    /**
     * Here is where we handle any work response that comes back
     *
     * @param workResponse Work response
     */
    private void processWork(WorkResponse workResponse) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            if (recentWorkRequestType != null && recentWorkRequestType.toString().equals(BlockTypes.OPEN.toString())) {
                // create open block
                OpenBlock openBlock = new OpenBlock(
                        getPrivateKey(),
                        recentPendingTransactionResponseItem.getHash(),
                        PreconfiguredRepresentatives.getRepresentative(),
                        workResponse.getWork()
                );
                // escape the block to match https://github.com/clemahieu/raiblocks/wiki/RPC-protocol#process-block
                // use jackson here to maintain field order
                ObjectMapper mapper = new ObjectMapper();
                String block = "";
                try {
                    block = mapper.writeValueAsString(openBlock);
                } catch (JsonProcessingException e) {
                    ExceptionHandler.handle(e);
                }
                Timber.d(block);

                checkState();
                // send the send request
                websocket.send(gson.toJson(new ProcessRequest(block)));
            } else if (recentWorkRequestType != null && recentWorkRequestType.toString().equals(BlockTypes.RECEIVE.toString())) {
                // create a receive block string
                ReceiveBlock receiveBlock = new ReceiveBlock(
                        getPrivateKey(),
                        wallet.getFrontierBlock(),
                        recentPendingTransactionResponseItem.getHash(),
                        workResponse.getWork());

                // escape the block to match https://github.com/clemahieu/raiblocks/wiki/RPC-protocol#process-block
                // use jackson here to maintain field order
                ObjectMapper mapper = new ObjectMapper();
                String block = "";
                try {
                    block = mapper.writeValueAsString(receiveBlock);
                } catch (JsonProcessingException e) {
                    ExceptionHandler.handle(e);
                }
                Timber.d(block);


                checkState();
                // send the send request
                websocket.send(gson.toJson(new ProcessRequest(block)));

            } else if (recentWorkRequestType != null && recentWorkRequestType.toString().equals(BlockTypes.SEND.toString())) {
                // send work so post to bus
                // this could probably all be handled here
                post(workResponse);
            }
        });
    }

    /**
     * When an OPEN, SEND, or RECEIVE block comes back successfully with a hash
     *
     * @param processResponse Process Response
     */
    private void handleProcessResponse(ProcessResponse processResponse) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            if (recentWorkRequestType != null &&
                    (recentWorkRequestType.toString().equals(BlockTypes.OPEN.toString()) ||
                            recentWorkRequestType.toString().equals(BlockTypes.RECEIVE.toString()))) {
                wallet.setFrontierBlock(processResponse.getHash());

                if (recentWorkRequestType.toString().equals(BlockTypes.OPEN.toString())) {
                    blockCount = 1;
                } else {
                    blockCount++;
                }

                recentPendingTransactionResponseItem.setComplete(true);

                // account subscribe
                websocket.send(gson.toJson(new SubscribeRequest(address.getAddress(), getLocalCurrency())));

                // account history request
                websocket.send(gson.toJson(new AccountHistoryRequest(address.getAddress(), blockCount != null ? blockCount : 10)));

                processNextTransaction();
            } else if (recentWorkRequestType != null &&
                    (recentWorkRequestType.toString().equals(BlockTypes.SEND.toString()))) {
                blockCount++;
                post(processResponse);
            }
        });
    }

    /**
     * Objects that are not mapped to a known response can be processed here
     *
     * @param message Websocket Message
     */
    private void handleNullMessageTypes(String message) {
        try {
            Object o = gson.fromJson(message, Object.class);
            if (o instanceof LinkedTreeMap) {
                processLinkedTreeMap((LinkedTreeMap) o);
            }
        } catch (JsonSyntaxException e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Process a linked tree map to see if there are pending blocks
     * Producer of the queue
     *
     * @param linkedTreeMap Linked Tree Map
     */
    private void processLinkedTreeMap(LinkedTreeMap linkedTreeMap) {
        if (linkedTreeMap.containsKey("blocks")) {
            // this is a set of blocks
            Object blocks = linkedTreeMap.get("blocks");
            if (blocks instanceof LinkedTreeMap) {
                // blocks is not empty
                Set keys = ((LinkedTreeMap) blocks).keySet();
                for (Object key : keys) {
                    try {
                        PendingTransactionResponseItem pendingTransactionResponseItem = new Gson().fromJson(String.valueOf(((LinkedTreeMap) blocks).get(key)), PendingTransactionResponseItem.class);
                        pendingTransactionResponseItem.setHash(key.toString());
                        wallet.getPendingTransactions().add(pendingTransactionResponseItem);
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                    }
                }
                processNextTransaction();
            }
        }
    }

    private void processNextTransaction() {
        if (wallet.getPendingTransactions().size() > 0) {
            // peek at first item
            PendingTransactionResponseItem item = wallet.getPendingTransactions().element();
            if (item.isComplete() || wallet.getFrontierBlock().equals(item.getHash())) {
                // pending transaction has been completed
                wallet.getPendingTransactions().poll();
                processNextTransaction();
            } else if (!item.isInProgress()) {
                // no item is in progress
                item.setInProgress(true);

                // keep track of which item we are processing
                recentPendingTransactionResponseItem = item;

                if (blockCount <= 0) {
                    // process open block
                    requestWorkSend(wallet.getFrontierBlock(), BlockTypes.OPEN);
                } else {
                    // process receive otherwise
                    requestWorkSend(wallet.getFrontierBlock(), BlockTypes.RECEIVE);
                }
            }


        }
    }

    /**
     * Request check to see if account is ready
     */

    public void requestAccountCheck() {
        if (address != null) {
            checkState();
            // account subscribe
            websocket.send(gson.toJson(new AccountCheckRequest(address.getAddress())));
        }
    }

    /**
     * Request Pending Blocks
     */
    public void requestPending() {
        if (address != null) {
            checkState();
            // account pending
            websocket.send(gson.toJson(new PendingTransactionsRequest(address.getAddress(), true, blockCount)));
        }
    }

    /**
     * Request all the account info
     */
    public void requestUpdate() {
        if (address != null) {
            checkState();
            if (websocket != null) {
                // account subscribe
                websocket.send(gson.toJson(new SubscribeRequest(address.getAddress(), getLocalCurrency())));

                // account history request
                websocket.send(gson.toJson(new AccountHistoryRequest(address.getAddress(), blockCount != null ? blockCount : 10)));

                requestPending();
            }
        }
    }

    /**
     * Make a work request for a send
     *
     * @param previous the hash of the last block in our chain, our current frontier
     */
    public void requestWorkSend(String previous, BlockTypes type) {
        checkState();
        recentWorkRequestType = type;

        // request work
        websocket.send(gson.toJson(new WorkRequest(previous)));
    }

    public void requestSend(String previous, Address destination, BigInteger balance, String
            work) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            // create a send block string
            SendBlock sendBlock = new SendBlock(getPrivateKey(), previous, destination.getAddress(), balance.toString(), work);

            // escape the block to match https://github.com/clemahieu/raiblocks/wiki/RPC-protocol#process-block
            // use jackson here to maintain field order
            ObjectMapper mapper = new ObjectMapper();
            String block = "";
            try {
                block = mapper.writeValueAsString(sendBlock);
            } catch (JsonProcessingException e) {
                ExceptionHandler.handle(e);
            }
            Timber.d(block);

            checkState();
            // send the send request
            websocket.send(gson.toJson(new ProcessRequest(block)));
        });
    }

    private void handleError(Throwable error) {
        if ((error instanceof IllegalStateException || error instanceof UnknownHostException) &&
                errorCount < MAX_ERROR_COUNT) {
            errorCount++;
            initWebSocket();
        } else {
            post(new SocketError(error));
            ExceptionHandler.handle(error);
            errorCount = 0;
        }
    }


    /**
     * Get credentials from realm and return address
     *
     * @return Address object
     */
    private Address getAddress() {
        Credentials credentials = realm.where(Credentials.class).findFirst();
        if (credentials == null) {
            return null;
        } else {
            return new Address(credentials.getAddressString());
        }
    }

    /**
     * Get local currency from shared preferences
     *
     * @return Local Currency
     */
    public String getLocalCurrency() {
        return sharedPreferencesUtil.getLocalCurrency().toString();
    }

    /**
     * Get private key from realm
     *
     * @return Private Key
     */
    private String getPrivateKey() {
        Credentials credentials = realm.where(Credentials.class).findFirst();
        if (credentials == null) {
            return null;
        } else {
            return credentials.getPrivateKey();
        }
    }

    /**
     * Close the web socket
     */
    public void close() {
        if (websocket != null) {
            websocket.close(1000, "Closed");
            websocket = null;
        }
    }

    private void checkState() {
        if (websocket == null) {
            initWebSocket();
        }
    }
}
