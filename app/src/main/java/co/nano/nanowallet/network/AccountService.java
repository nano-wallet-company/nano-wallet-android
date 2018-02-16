package co.nano.nanowallet.network;

import android.content.Context;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.reflect.TypeToken;
import com.navin.flintstones.rxwebsocket.RxWebsocket;

import java.math.BigInteger;

import javax.inject.Inject;

import co.nano.nanowallet.bus.RxBus;
import co.nano.nanowallet.model.Address;
import co.nano.nanowallet.model.Credentials;
import co.nano.nanowallet.network.model.BaseNetworkModel;
import co.nano.nanowallet.network.model.request.AccountHistoryRequest;
import co.nano.nanowallet.network.model.request.CurrentPriceRequest;
import co.nano.nanowallet.network.model.request.SendBlock;
import co.nano.nanowallet.network.model.request.SendRequest;
import co.nano.nanowallet.network.model.request.SubscribeRequest;
import co.nano.nanowallet.network.model.request.WorkRequest;
import co.nano.nanowallet.network.model.response.SubscribeResponse;
import co.nano.nanowallet.ui.common.ActivityWithComponent;
import co.nano.nanowallet.util.ExceptionHandler;
import co.nano.nanowallet.util.SharedPreferencesUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
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
    private TypeToken<BaseNetworkModel> requestListTypeToken;

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

        // initialize the web socket
        if (websocket == null) {
            initWebSocket();
        }
    }

    /**
     * Initialize websocket and all listeners
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
                        Timber.d("[MESSAGE QUEUED]:%s", ((RxWebsocket.QueuedMessage) event).message().toString());
                    } else if (event instanceof RxWebsocket.Message) {
                        handleEvent((RxWebsocket.Message) event);
                    }
                })
                .subscribe(event -> {
                }, ExceptionHandler::handle);

        // connect to web socket
        websocket.connect()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        event -> Timber.d(event.toString()),
                        ExceptionHandler::handle
                );

        websocket.connect();
    }

    private void handleEvent(RxWebsocket.Message message) {
        BaseNetworkModel event = null;
        try {
            event = message.data(BaseNetworkModel.class);
        } catch (Throwable throwable) {
            ExceptionHandler.handle(throwable);
        }

        // keep track of current block count for more efficient requests
        if (event instanceof SubscribeResponse) {
            blockCount = ((SubscribeResponse) event).getBlock_count();
        }

        // post whatever the response type is to the bus
        if (event != null) {
            RxBus.get().post(event);
        }
    }

    /**
     * Request all the account info
     */
    public void requestUpdate() {
        if (websocket == null) {
            initWebSocket();
        } else if (address != null) {
            // account subscribe
            websocket.send(new SubscribeRequest(address.getAddress(), getLocalCurrency())).subscribe();

            // current price request
            websocket.send(new CurrentPriceRequest(getLocalCurrency())).subscribe();

            // price in bitcoin request
            websocket.send(new CurrentPriceRequest("BTC")).subscribe();

            // account history request
            websocket.send(new AccountHistoryRequest(address.getAddress(), blockCount != null ? blockCount : 10)).subscribe();
        }
    }

    /**
     * Make a work request for a send
     *
     * @param previous the hash of the last block in our chain, our current frontier
     */
    public void requestWorkSend(String previous) {
        // request work
        websocket.send(new WorkRequest(previous));
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

        // send the send request
        websocket.send(new SendRequest(block));
    }


    /**
     * Get credentials from realm and return address
     *
     * @return
     */
    private Address getAddress() {
        Credentials credentials = null;
        credentials = realm.where(Credentials.class).findFirst();
        return new Address(credentials.getAddressString());
    }

    /**
     * Get local currency from shared preferences
     *
     * @return
     */
    public String getLocalCurrency() {
        return sharedPreferencesUtil.getLocalCurrency().toString();
    }

    /**
     * Get private key from realm
     *
     * @return
     */
    public String getPrivateKey() {
        Credentials credentials = null;
        credentials = realm.where(Credentials.class).findFirst();
        return credentials.getPrivateKey();
    }

    /**
     * Close the web socket
     */
    public void close() {
        if (websocket != null) {
            websocket.disconnect(1, "App closed socket");
        }
    }
}
