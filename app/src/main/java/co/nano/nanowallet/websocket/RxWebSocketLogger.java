package co.nano.nanowallet.websocket;


import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import co.nano.nanowallet.websocket.entities.SocketEvent;
import timber.log.Timber;


public class RxWebSocketLogger implements Subscriber<SocketEvent> {

    private final String TAG;

    public RxWebSocketLogger(String tag) {
        TAG = tag + ": ";
    }

    @Override
    public void onComplete() {
        Timber.d("Complete");
    }

    @Override
    public void onError(Throwable e) {
        Timber.e("Error");
        Timber.e(e.getMessage());
        e.printStackTrace();
    }

    @Override
    public void onSubscribe(Subscription s) {
        Timber.e("Subscribe");
    }

    @Override
    public void onNext(SocketEvent socketEvent) {
        Timber.d("Next");
        Timber.d(socketEvent.toString());
    }
}
