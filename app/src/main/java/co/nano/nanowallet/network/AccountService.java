package co.nano.nanowallet.network;

import android.content.Context;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.navin.flintstones.rxwebsocket.RxWebsocket;

import java.math.BigInteger;
import java.util.Set;

import javax.inject.Inject;

import co.nano.nanowallet.bus.RxBus;
import co.nano.nanowallet.model.Address;
import co.nano.nanowallet.model.Credentials;
import co.nano.nanowallet.network.model.BaseNetworkModel;
import co.nano.nanowallet.network.model.request.AccountCheckRequest;
import co.nano.nanowallet.network.model.request.AccountHistoryRequest;
import co.nano.nanowallet.network.model.request.CurrentPriceRequest;
import co.nano.nanowallet.network.model.request.PendingTransactionsRequest;
import co.nano.nanowallet.network.model.request.SendBlock;
import co.nano.nanowallet.network.model.request.SendRequest;
import co.nano.nanowallet.network.model.request.SubscribeRequest;
import co.nano.nanowallet.network.model.request.WorkRequest;
import co.nano.nanowallet.network.model.response.PendingTransactionResponseItem;
import co.nano.nanowallet.network.model.response.SubscribeResponse;
import co.nano.nanowallet.ui.common.ActivityWithComponent;
import co.nano.nanowallet.util.ExceptionHandler;
import co.nano.nanowallet.util.ObservableQueue;
import co.nano.nanowallet.util.SharedPreferencesUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import timber.log.Timber;

/**
 * Methods for calling the account service
 */

public class AccountService {
    private RxWebsocket websocket;
    private static final String CONNECTION_URL = "wss://raicast.lightrai.com:443";
    //private static final String CONNECTION_URL = "wss://light.nano.org:443";
    private Address address;
    private Integer blockCount;
    private ObservableQueue<PendingTransactionResponseItem> pendingTransactionQueue;

    @Inject
    Realm realm;

    @Inject
    SharedPreferencesUtil sharedPreferencesUtil;

    public AccountService(Context context) {
        // init dependency injection
        if (context instanceof ActivityWithComponent) {
            ((ActivityWithComponent) context).getActivityComponent().inject(this);
        }
    }

    public void open() {
        // get user's address
        address = getAddress();

        // initialize pending transaction queue
        pendingTransactionQueue = new ObservableQueue<>();

        // initialize the web socket
        if (websocket == null) {
            initWebSocket();
        }

        consumePendingTransactions();
    }

    /**
     * Initialize websocket and event listeners
     */
    private void initWebSocket() {
        // create websocket
        websocket = new RxWebsocket.Builder()
                .addConverterFactory(WebSocketConverterFactory.create())
                .build(CONNECTION_URL);

        // set up event stream handling
        websocket.eventStream()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(event -> {
                    if (event instanceof RxWebsocket.Open) {
                        requestUpdate();
                    } else if (event instanceof RxWebsocket.Closed) {
                        Timber.d("DISCONNECTED");
                    } else if (event instanceof RxWebsocket.QueuedMessage) {
                        Timber.d("[MESSAGE QUEUED]:%s", ((RxWebsocket.QueuedMessage) event).message());
                    } else if (event instanceof RxWebsocket.Message) {
                        Timber.d("[MESSAGE RECEIVED]:%s", ((RxWebsocket.Message) event).data());
                        handleMessage((RxWebsocket.Message) event);
                    }
                })
                .subscribe(event -> {
                }, this::handleError);
    }

    /**
     * Connect to the web socket
     */
    private void connect() {
        if (websocket != null) {
            // connect to web socket
            websocket.connect()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            event -> Timber.d(event.toString()),
                            ExceptionHandler::handle
                    );
        }
    }

    /**
     * Generic message hander. Convert to an object and process or post to bus.
     *
     * @param message Websocket Message
     */
    private void handleMessage(RxWebsocket.Message message) {
        BaseNetworkModel event = null;
        try {
            event = message.data(BaseNetworkModel.class);
        } catch (Throwable throwable) {
            ExceptionHandler.handle(throwable);
        }

        if (event != null && event.getMessageType() == null) {
            // try parsing to a linked tree map object if event type is null
            handleNullMessageTypes(message);
        } else {
            // keep track of current block count for more efficient requests
            if (event instanceof SubscribeResponse) {
                blockCount = ((SubscribeResponse) event).getBlock_count();
            }

            // post whatever the response type is to the bus
            if (event != null) {
                RxBus.get().post(event);
            }
        }
    }

    /**
     * Objects that are not mapped to a known response can be processed here
     *
     * @param message Websocket Message
     */
    private void handleNullMessageTypes(RxWebsocket.Message message) {
        try {
            Object o = message.data(Object.class);
            if (o instanceof LinkedTreeMap) {
                processLinkedTreeMap((LinkedTreeMap) o);
            }
        } catch (Throwable throwable) {
            ExceptionHandler.handle(throwable);
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
                        pendingTransactionQueue.put(pendingTransactionResponseItem);
                    } catch (Throwable throwable) {
                        ExceptionHandler.handle(throwable);
                    }
                }
            }
        }
    }

    /**
     * Process the queue of pending transactions
     */
    private void consumePendingTransactions() {
        pendingTransactionQueue
                .observe()
                .observeOn(Schedulers.io())
                .forEach(item ->
                        Timber.d("Processed: %s", item.toString())
                );
    }

    /**
     * Request check to see if account is ready
     */
    public void requestAccountCheck() {
        if (websocket != null && address != null) {
            // account subscribe
            websocket.send(new AccountCheckRequest(address.getAddress()))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(event -> {
                    }, this::handleError);
        }
    }

    /**
     * Request Pending Blocks
     */
    public void requestPending() {
        if (websocket != null && address != null) {
            // account subscribe
            websocket.send(new PendingTransactionsRequest(
                    address.getAddress(),
                    true,
                    blockCount))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(event -> {
                    }, this::handleError);
        }
    }

    /**
     * Request all the account info
     */
    public void requestUpdate() {
        if (websocket != null && address != null) {
            // account subscribe
            websocket.send(new SubscribeRequest(address.getAddress(), getLocalCurrency()))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(event -> {
                    }, this::handleError);

            // current price request
            websocket.send(new CurrentPriceRequest(getLocalCurrency()))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(event -> {
                    }, this::handleError);

            // price in bitcoin request
            websocket.send(new CurrentPriceRequest("BTC"))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(event -> {
                    }, this::handleError);

            // account history request
            websocket.send(new AccountHistoryRequest(address.getAddress(), blockCount != null ? blockCount : 10))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(event -> {
                    }, this::handleError);
        }
    }

    /**
     * Make a work request for a send
     *
     * @param previous the hash of the last block in our chain, our current frontier
     */
    public void requestWorkSend(String previous) {
        if (websocket != null) {
            // request work
            websocket.send(new WorkRequest(previous))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(event -> {
                    }, this::handleError);
        }
    }

    public void requestSend(String previous, Address destination, BigInteger balance, String work) {
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

        if (websocket != null) {
            // send the send request
            websocket.send(new SendRequest(block))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(event -> {
                    }, this::handleError);
        }
    }

    private void handleError(Throwable error) {
        if (error instanceof IllegalStateException) {
            connect();
        } else {
            ExceptionHandler.handle(error);
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
            websocket.disconnect(1000, "Disconnect");
        }
    }
}
