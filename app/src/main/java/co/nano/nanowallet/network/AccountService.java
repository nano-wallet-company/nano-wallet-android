package co.nano.nanowallet.network;

import android.content.Context;

import javax.inject.Inject;

import co.nano.nanowallet.model.Address;
import co.nano.nanowallet.model.Credentials;
import co.nano.nanowallet.network.model.request.CurrentPriceRequest;
import co.nano.nanowallet.network.model.request.SubscribeRequest;
import co.nano.nanowallet.ui.common.ActivityWithComponent;
import co.nano.nanowallet.util.SharedPreferencesUtil;
import co.nano.nanowallet.websocket.RxWebSocket;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import timber.log.Timber;

/**
 * Methods for calling the account service
 */

public class AccountService {
    private RxWebSocket rxWebSocket;
    private Context context;
    private static final String CONNECTION_URL = "wss://raicast.lightrai.com:443";
    private Address address;
    private String localCurrency;

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

        // get local currency
        localCurrency = getLocalCurrency();

        // initialize the web socket
        initWebSocket();
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
                    Timber.i("Opened");
                    rxWebSocket.sendMessage(new SubscribeRequest(address.getLongAddress(), localCurrency).serialize());
                    rxWebSocket.sendMessage(new CurrentPriceRequest(localCurrency).serialize());
                }, Throwable::printStackTrace);

        rxWebSocket.onClosed()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(socketClosedEvent -> {
                    Timber.i("Closed: " + socketClosedEvent.getReason());
                }, Throwable::printStackTrace);

        rxWebSocket.onClosing()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(socketClosingEvent -> {
                    Timber.i("Closing: " + socketClosingEvent.getReason());
                }, Throwable::printStackTrace);

        rxWebSocket.onTextMessage()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(socketMessageEvent -> {
                    Timber.i(socketMessageEvent.getText());
                }, Throwable::printStackTrace);

        rxWebSocket.onBinaryMessage()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(socketMessageEvent -> {
                    Timber.i(socketMessageEvent.getText());
                }, Throwable::printStackTrace);

        rxWebSocket.onFailure()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(socketFailureEvent -> {
                    Timber.i("Error: " + socketFailureEvent.getException().getMessage());
                }, Throwable::printStackTrace);

        rxWebSocket.connect();
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
     * @return
     */
    public String getLocalCurrency() {
        return sharedPreferencesUtil.getLocalCurrency().toString();
    }

    public void close() {
        rxWebSocket.close();
        context = null;
    }
}
