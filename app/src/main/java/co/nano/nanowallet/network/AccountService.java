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
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import co.nano.nanowallet.bus.RxBus;
import co.nano.nanowallet.bus.SocketError;
import co.nano.nanowallet.model.Address;
import co.nano.nanowallet.model.Credentials;
import co.nano.nanowallet.model.NanoWallet;
import co.nano.nanowallet.model.PreconfiguredRepresentatives;
import co.nano.nanowallet.network.model.BaseResponse;
import co.nano.nanowallet.network.model.BlockTypes;
import co.nano.nanowallet.network.model.RequestItem;
import co.nano.nanowallet.network.model.request.AccountHistoryRequest;
import co.nano.nanowallet.network.model.request.PendingTransactionsRequest;
import co.nano.nanowallet.network.model.request.ProcessRequest;
import co.nano.nanowallet.network.model.request.SubscribeRequest;
import co.nano.nanowallet.network.model.request.WorkRequest;
import co.nano.nanowallet.network.model.request.block.Block;
import co.nano.nanowallet.network.model.request.block.OpenBlock;
import co.nano.nanowallet.network.model.request.block.ReceiveBlock;
import co.nano.nanowallet.network.model.request.block.SendBlock;
import co.nano.nanowallet.network.model.response.CurrentPriceResponse;
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
    private static final String CONNECTION_URL = "wss://light.nano.org:443";
    private static final int TIMEOUT_MILLISECONDS = 5000;

    private WebSocket websocket;
    private boolean connected = false;
    private Queue<RequestItem> requestQueue = new LinkedList<>();
    private String private_key;
    private Address address;

    @Inject
    SharedPreferencesUtil sharedPreferencesUtil;

    @Inject
    NanoWallet wallet;

    @Inject
    Gson gson;

    @Inject
    Realm realm;

    @Inject
    @Named("encryption_key")
    byte[] encryption_key;

    public AccountService(Context context) {
        // init dependency injection
        if (context instanceof ActivityWithComponent) {
            ((ActivityWithComponent) context).getActivityComponent().inject(this);
        }
    }

    public void open() {
        wallet.setBlockCount(-1);

        // initialize the web socket
        if (!connected) {
            initWebSocket();
        }

        private_key = getPrivateKey();
        address = getAddress();

    }

    /**
     * Initialize websocket and event listeners
     */
    private void initWebSocket() {
        // create websocket
        OkHttpClient client = new OkHttpClient.Builder()
                .build();

        Request request = new Request.Builder().url(CONNECTION_URL).build();

        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                super.onOpen(webSocket, response);
                if (response.code() == 101) {
                    Timber.d("OPENED");
                    connected = true;
                    requestUpdate();
                }
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
                connected = false;
                switch (code) {
                    case 1000: // CLOSE_NORMAL
                        Timber.d("CLOSED");
                        break;
                    default: // Abnormal closure
                        checkState();
                        break;
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
                super.onFailure(webSocket, t, response);
                ExceptionHandler.handle(t);
                if (connected) {
                    close();
                    checkState();
                } else {
                    post(new SocketError(t));
                }
            }
        };

        // create websocket with listeners
        websocket = client.newWebSocket(request, listener);

        // trigger shutdown of the dispatcher's executor so this process can exit cleanly.
        client.dispatcher().executorService().shutdown();

        processQueue();
    }

    /**
     * Generic message handler. Convert to an object and process or post to bus.
     *
     * @param message String message
     */
    private void handleMessage(String message) {
        // deserialize message if possible
        BaseResponse event = null;
        try {
            event = gson.fromJson(message, BaseResponse.class);
        } catch (JsonSyntaxException e) {
            ExceptionHandler.handle(e);
        }

        if (event != null && event.getMessageType() == null) {
            // try parsing to a linked tree map object if event type is null
            // for now, these are the blocks that come back from a pending request
            handleNullMessageTypes(message);
        } else if (event != null && event instanceof WorkResponse) {
            // process a work response
            handleWorkResponse((WorkResponse) event);
        } else if (event != null && event instanceof TransactionResponse) {
            // a transaction was pushed to the app via the socket
            TransactionResponse transactionResponse = (TransactionResponse) event;
            PendingTransactionResponseItem pendingTransactionResponseItem = new PendingTransactionResponseItem(
                    transactionResponse.getAccount(), transactionResponse.getAmount(), transactionResponse.getHash());
            if (transactionResponse.getBlock().getType().equals(BlockTypes.SEND.toString())) {
                handleTransactionResponse(pendingTransactionResponseItem);
            }
        } else if (event != null && event instanceof ProcessResponse) {
            handleProcessResponse((ProcessResponse) event);
        } else {
            // update block count on subscribe request
            if (event instanceof SubscribeResponse) {
                if (((SubscribeResponse) event).getBlock_count() != null) {
                    updateBlockCount(((SubscribeResponse) event).getBlock_count());
                }
                if (((SubscribeResponse) event).getFrontier() != null) {
                    updateFrontier(((SubscribeResponse) event).getFrontier());
                }
            }

            // post whatever the response type is to the bus
            if (event != null) {
                post(event);
            }

            // remove item from queue and process
            if (!(event instanceof CurrentPriceResponse)) {
                requestQueue.poll();
            }
            processQueue();
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
     * Handle a transaction response by create an open or a receive block
     *
     * @param item Pending transaction response item
     */
    private void handleTransactionResponse(PendingTransactionResponseItem item) {
        Timber.d(item.toString());
        if (!queueContainsRequestWithHash(item.getHash())) {
            if (wallet.getOpenBlock() == null && !queueContainsOpenBlock()) {
                requestOpen(item.getHash());
            } else {
                requestReceive(item.getHash());
            }
        }
    }


    /**
     * Here is where we handle any work response that comes back
     *
     * @param workResponse Work response
     */
    private void handleWorkResponse(WorkResponse workResponse) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            // work response received so remove that work item from the queue
            requestQueue.poll();

            // make sure the next item is a Block type and update the work on that type
            RequestItem nextRequest = requestQueue.peek();
            if (nextRequest != null && nextRequest.getRequest() instanceof Block) {
                ((Block) nextRequest.getRequest()).setWork(workResponse.getWork());
            } else {
                // Work was submitted without a block request following - should never happen
                ExceptionHandler.handle(new Throwable("Queue Error: work was submitted without a block request following"));
            }
            processQueue();
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
            // see what type of request sent this response
            RequestItem requestItem = requestQueue.peek();
            if (requestItem != null) {
                if (requestItem.getRequest() instanceof Block) {
                    if (requestItem.getRequest() instanceof OpenBlock) {
                        updateFrontier(processResponse.getHash());
                        updateBlockCount(1);
                    } else if (requestItem.getRequest() instanceof ReceiveBlock) {
                        updateFrontier(processResponse.getHash());
                        updateBlockCount(wallet.getBlockCount() + 1);
                    } else if (requestItem.getRequest() instanceof SendBlock) {
                        updateBlockCount(wallet.getBlockCount() + 1);
                        post(processResponse);
                    }

                    requestSubscribe();
                    requestAccountHistory();
                } else {
                    // something is out of sync if this wasn't a block - should never happen
                    ExceptionHandler.handle(new Throwable("Queue Error: something is out of sync if this wasn't a block"));
                }
            }
            requestQueue.poll();
            processQueue();
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
            } else {
                requestQueue.poll();
                processQueue();
            }
        } catch (JsonSyntaxException e) {
            ExceptionHandler.handle(e);
            requestQueue.poll();
            processQueue();
        }
    }

    /**
     * Process a linked tree map to see if there are pending blocks to handle
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
                        handleTransactionResponse(pendingTransactionResponseItem);
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                    }
                }
            }
        }
        requestQueue.poll();
        processQueue();
    }

    /**
     * Process the next item in the queue if item is not currently processing
     */
    private void processQueue() {
        if (requestQueue != null && requestQueue.size() > 0) {
            RequestItem requestItem = requestQueue.peek();
            if (requestItem != null && !requestItem.isProcessing()) {
                // process item
                requestItem.setProcessing(true);

                if (requestItem.getRequest() instanceof Block) {
                    // escape the block to match https://github.com/clemahieu/raiblocks/wiki/RPC-protocol#process-block
                    // use jackson here to maintain field order
                    ObjectMapper mapper = new ObjectMapper();
                    String block = "";
                    try {
                        block = mapper.writeValueAsString(requestItem.getRequest());
                    } catch (JsonProcessingException e) {
                        ExceptionHandler.handle(e);
                    }

                    checkState();
                    Timber.d("SEND: %s", gson.toJson(new ProcessRequest(block)));
                    websocket.send(gson.toJson(new ProcessRequest(block)));
                } else {
                    checkState();
                    Timber.d("SEND: %s", gson.toJson(requestItem.getRequest()));
                    websocket.send(gson.toJson(requestItem.getRequest()));
                }
            }
        }
    }

    /**
     * Request all the account info
     */
    public void requestUpdate() {
        if (address != null && address.getAddress() != null) {
            requestQueue.add(new RequestItem<>(new SubscribeRequest(address.getAddress(), getLocalCurrency())));
            requestQueue.add(new RequestItem<>(new AccountHistoryRequest(address.getAddress(), wallet.getBlockCount() != null ? wallet.getBlockCount() : 10)));
            requestQueue.add(new RequestItem<>(new PendingTransactionsRequest(address.getAddress(), true, wallet.getBlockCount())));
            processQueue();
        }
    }

    /**
     * Request subscribe
     */
    public void requestSubscribe() {
        if (address != null && address.getAddress() != null) {
            requestQueue.add(new RequestItem<>(new SubscribeRequest(address.getAddress(), getLocalCurrency())));
            processQueue();
        }
    }

    /**
     * Request Pending Blocks
     */
    public void requestPending() {
        if (address != null && address.getAddress() != null) {
            requestQueue.add(new RequestItem<>(new PendingTransactionsRequest(address.getAddress(), true, wallet.getBlockCount())));
            processQueue();
        }
    }

    /**
     * Request AccountHistory
     */
    public void requestAccountHistory() {
        if (address != null && address.getAddress() != null) {
            requestQueue.add(new RequestItem<>(new AccountHistoryRequest(address.getAddress(), wallet.getBlockCount() != null ? wallet.getBlockCount() : 10)));
            processQueue();
        }
    }

    /**
     * Make an open block request
     *
     * @param source Source
     */
    private void requestOpen(String source) {
        // create a work block
        requestQueue.add(new RequestItem<>(new WorkRequest(wallet.getFrontierBlock())));

        // create an open block
        requestQueue.add(new RequestItem<>(new OpenBlock(
                private_key,
                source,
                PreconfiguredRepresentatives.getRepresentative()))
        );
        processQueue();
    }

    /**
     * Make a receive block request
     *
     * @param source Source
     */
    private void requestReceive(String source) {
        // create a work block
        requestQueue.add(new RequestItem<>(new WorkRequest(wallet.getFrontierBlock())));

        // create an open block
        requestQueue.add(new RequestItem<>(new ReceiveBlock(
                private_key,
                wallet.getFrontierBlock(),
                source))
        );
        processQueue();
    }

    /**
     * Make a send request
     *
     * @param previous    Previous hash
     * @param destination Destination
     * @param balance     Remaining balance after a send
     */
    public void requestSend(String previous, Address destination, BigInteger balance) {
        // create a work block
        requestQueue.add(new RequestItem<>(new WorkRequest(previous)));

        // create a send block
        requestQueue.add(new RequestItem<>(new SendBlock(
                private_key,
                previous,
                destination.getAddress(),
                balance.toString()))
        );
        processQueue();
    }


    /**
     * Get credentials from realm and return address
     *
     * @return Address object
     */
    private Address getAddress() {
        Credentials _credentials = realm.where(Credentials.class).findFirst();
        if (_credentials == null) {
            return null;
        } else {
            Credentials credentials = realm.copyFromRealm(_credentials);
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
        Credentials _credentials = realm.where(Credentials.class).findFirst();
        if (_credentials == null) {
            return null;
        } else {
            Credentials credentials = realm.copyFromRealm(_credentials);
            return credentials.getPrivateKey();
        }
    }

    /**
     * Check to see if queue already contains an open block
     *
     * @return true if queue has an open block in it already
     */
    private boolean queueContainsOpenBlock() {
        if (requestQueue == null) {
            return false;
        }
        for (RequestItem item : requestQueue) {
            if (item.getRequest() instanceof OpenBlock) {
                return true;
            }
        }
        return false;
    }

    /**
     * See if this block is already in the queue
     * @param source Source hash
     * @return true if block is already in the queue with the same source
     */
    private boolean queueContainsRequestWithHash(String source) {
        if (requestQueue == null) {
            return false;
        }
        for (RequestItem item : requestQueue) {
            if ((item.getRequest() instanceof OpenBlock && ((OpenBlock) item.getRequest()).getSource().equals(source)) ||
                    (item.getRequest() instanceof ReceiveBlock && ((ReceiveBlock) item.getRequest()).getSource().equals(source))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Update block count in wallet and on pending requests
     *
     * @param blockCount Block count
     */
    private void updateBlockCount(int blockCount) {
        wallet.setBlockCount(blockCount);
        if (requestQueue != null) {
            for (RequestItem item : requestQueue) {
                if (item.getRequest() instanceof AccountHistoryRequest && !item.isProcessing()) {
                    ((AccountHistoryRequest) item.getRequest()).setCount(blockCount);
                } else if (item.getRequest() instanceof PendingTransactionsRequest && !item.isProcessing()) {
                    ((PendingTransactionsRequest) item.getRequest()).setCount(blockCount);
                }
            }
        }
    }


    /**
     * Update frontier block in wallet and on any pending receive requests
     *
     * @param frontier
     */
    private void updateFrontier(String frontier) {
        wallet.setFrontierBlock(frontier);
        if (requestQueue != null) {
            for (RequestItem item : requestQueue) {
                if (item.getRequest() instanceof ReceiveBlock && !item.isProcessing()) {
                    ((ReceiveBlock) item.getRequest()).setPrevious(frontier);
                } else if (item.getRequest() instanceof WorkRequest && !item.isProcessing()) {
                    ((WorkRequest) item.getRequest()).setHash(frontier);
                }
            }
        }
    }

    /**
     * Close the web socket
     */
    public void close() {
        if (!connected) {
            return;
        }
        try {
            websocket.close(1000, "Closed");
            connected = false;
        } catch (IllegalStateException e) {
            connected = false;
            ExceptionHandler.handle(e);
        }
    }

    private void checkState() {
        if (!connected) {
            initWebSocket();
        }
    }
}
