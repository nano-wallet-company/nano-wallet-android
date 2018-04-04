package co.nano.nanowallet.analytics;


import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.core.CrashlyticsCore;

import co.nano.nanowallet.model.Credentials;
import io.fabric.sdk.android.Fabric;
import io.realm.Realm;

public class AnalyticsService {

    private Realm realm;
    private Context context;

    /**
     * Constructor for analytics service
     *
     * @param context Application context
     * @param realm   Realm DB instance
     */
    public AnalyticsService(Context context, Realm realm) {
        this.context = context;
        this.realm = realm;
    }

    /**
     * Start analytics services
     */
    public void start() {
        // initialize crashlytics
        Fabric.with(context, new Crashlytics());
        // initialize answers
        Fabric.with(context, new Answers());
    }

    /**
     * Stop analytics services
     */
    public void stop() {
        // disable crashlytics
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(true).build())
                .build();
        Fabric.with(context, crashlyticsKit);
    }

    /**
     * Trace a basic event
     *
     * @param event String of event title
     */
    public void track(String event) {
        Credentials credentials = realm.where(Credentials.class).findFirst();
        if (credentials != null && credentials.getHasCompletedLegalAgreements()) {
            Answers.getInstance().logCustom(new CustomEvent(event));
        }
    }

    public void close() {
        context = null;
    }
}
