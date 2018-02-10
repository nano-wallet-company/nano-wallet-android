package co.nano.nanowallet.websocket;


import android.support.annotation.NonNull;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.util.concurrent.TimeUnit;

import co.nano.nanowallet.BuildConfig;
import co.nano.nanowallet.websocket.entities.SocketEvent;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class WebSocketOnSubscribe implements FlowableOnSubscribe<SocketEvent> {

    private final OkHttpClient client;
    private final Request request;

    public WebSocketOnSubscribe(@NonNull String url) {
        if (BuildConfig.DEBUG) {
            client = new OkHttpClient.Builder()
                    .readTimeout(0, TimeUnit.MILLISECONDS)
                    .addNetworkInterceptor(new StethoInterceptor())
                    .build();
        } else {
            client = new OkHttpClient.Builder()
                    .readTimeout(0, TimeUnit.MILLISECONDS)
                    .build();
        }

        request = new Request.Builder()
                .url(url)
                .build();
    }

    public WebSocketOnSubscribe(@NonNull OkHttpClient client, @NonNull String url) {
        this.client = client;
        request = new Request.Builder()
                .url(url)
                .build();
    }

    @Override
    public void subscribe(FlowableEmitter<SocketEvent> emitter) throws Exception {
        client.newWebSocket(request, new WebSocketEventRouter(emitter));
    }
}

