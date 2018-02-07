package co.nano.nanowallet.network;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import javax.inject.Inject;

import co.nano.nanowallet.bus.RxBus;
import co.nano.nanowallet.model.Address;
import co.nano.nanowallet.model.Credentials;
import co.nano.nanowallet.network.model.Actions;
import co.nano.nanowallet.network.model.BaseNetworkModel;
import co.nano.nanowallet.network.model.request.AccountHistoryRequest;
import co.nano.nanowallet.network.model.request.CurrentPriceRequest;
import co.nano.nanowallet.network.model.request.SubscribeRequest;
import co.nano.nanowallet.network.model.response.AccountHistoryResponse;
import co.nano.nanowallet.network.model.response.CurrentPriceResponse;
import co.nano.nanowallet.network.model.response.SubscribeResponse;
import co.nano.nanowallet.ui.common.ActivityWithComponent;
import co.nano.nanowallet.util.ExceptionHandler;
import co.nano.nanowallet.util.SharedPreferencesUtil;
import co.nano.nanowallet.websocket.RxWebSocket;
import io.gsonfire.GsonFireBuilder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import timber.log.Timber;

/**
 * Methods for calling the account service
 */

public class AccountService {
    private RxWebSocket rxWebSocket;
    private static final String CONNECTION_URL = "wss://raicast.lightrai.com:443";
    private Address address;
    private String localCurrency;
    private Integer count;
    private Gson gson;
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

        // configure gson to detect and set proper types
        GsonFireBuilder builder = new GsonFireBuilder()
                .registerPreProcessor(BaseNetworkModel.class, (clazz, src, gson) -> {
                    // figure out the response type based on what fields are in the response
                    if (src.isJsonObject() && src.getAsJsonObject().get("messageType") == null) {
                        if (src.getAsJsonObject().get("frontier") != null) {
                            // subscribe response
                            src.getAsJsonObject().addProperty("messageType", Actions.SUBSCRIBE.toString());
                        } else if (src.getAsJsonObject().get("history") != null) {
                            // history response
                            src.getAsJsonObject().addProperty("messageType", Actions.HISTORY.toString());
                        } else if (src.getAsJsonObject().get("currency") != null) {
                            // current price
                            src.getAsJsonObject().addProperty("messageType", Actions.PRICE.toString());
                        }
                    }
                }).registerTypeSelector(BaseNetworkModel.class, readElement -> {
                    // return proper type based on the message type that was set
                    if (readElement.isJsonObject() && readElement.getAsJsonObject().get("messageType") != null) {
                        String kind = readElement.getAsJsonObject().get("messageType").getAsString();
                        if (kind.equals(Actions.SUBSCRIBE.toString())) {
                            return SubscribeResponse.class;
                        } else if (kind.equals(Actions.HISTORY.toString())) {
                            return AccountHistoryResponse.class;
                        } else if (kind.equals(Actions.PRICE.toString())) {
                            return CurrentPriceResponse.class;
                        } else {
                            return null; // returning null will trigger Gson's default behavior
                        }
                    } else {
                        return null;
                    }
                });
        gson = builder.createGson();
    }

    public void open() {
        // get user's address
        address = getAddress();

        // get local currency
        localCurrency = getLocalCurrency();

        // initialize the web socket
        if (rxWebSocket == null) {
            initWebSocket();
        }
    }

    /**
     * Initialize websocket and all listeners
     */
    private void initWebSocket() {
        rxWebSocket = new RxWebSocket(CONNECTION_URL);

        rxWebSocket.onOpen()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(socketOpenEvent -> {
                    Timber.i("Opened: " + socketOpenEvent);
                    // account subscribe
                    rxWebSocket.sendMessage(gson, new SubscribeRequest(address.getLongAddress(), localCurrency))
                            .subscribe(o -> {}, ExceptionHandler::handle);

                    // current price request
                    rxWebSocket.sendMessage(gson, new CurrentPriceRequest(localCurrency))
                            .subscribe(o -> {}, ExceptionHandler::handle);

                    // account history request
                    rxWebSocket.sendMessage(gson, new AccountHistoryRequest(address.getLongAddress(), 10))
                            .subscribe(o -> {}, ExceptionHandler::handle);
                }, ExceptionHandler::handle);

        rxWebSocket.onClosed()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(socketClosedEvent -> {
                    Timber.i("Closed: " + socketClosedEvent.getReason());
                }, ExceptionHandler::handle);

        rxWebSocket.onTextMessage()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(socketMessageEvent -> {
                    try {
                        // post whatever the response type is to the bus
                        RxBus.get().post(gson.fromJson(socketMessageEvent.getText(), BaseNetworkModel.class));
                    } catch (JsonSyntaxException e) {
                        ExceptionHandler.handle(e);
                    }
                }, ExceptionHandler::handle);

        rxWebSocket.onFailure()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(socketFailureEvent -> {
                    Timber.e("Error: " + socketFailureEvent.getException().getMessage());
                }, ExceptionHandler::handle);

        rxWebSocket.connect();
    }

    /**
     * Request the account history
     */
    public void requestHistory() {
        if (address != null) {
            rxWebSocket.sendMessage(gson, new AccountHistoryRequest(address.getLongAddress(), 10))
                    .subscribe(o -> {}, ExceptionHandler::handle);
        }
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
     * Close the web socket
     */
    public void close() {
        if (rxWebSocket != null) {
            rxWebSocket.close().subscribe(o -> rxWebSocket = null);
        }
    }
}
