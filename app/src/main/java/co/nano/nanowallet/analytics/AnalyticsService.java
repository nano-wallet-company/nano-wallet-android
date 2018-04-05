package co.nano.nanowallet.analytics;


import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.core.CrashlyticsCore;

import java.util.HashMap;
import java.util.Map;

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
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore())
                .answers(new Answers())
                .build();
        Fabric.with(context, crashlyticsKit);
    }

    /**
     * Start analytics services
     */
    public void startAnswersOnly() {
        // initialize crashlytics
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(true).build())
                .answers(new Answers())
                .build();
        Fabric.with(context, crashlyticsKit);
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
     * Track a basic event
     *
     * @param event Event title string
     */
    public void track(String event) {
        Credentials credentials = realm.where(Credentials.class).findFirst();
        if (credentials == null) {
            return;
        }

        if (credentials.getHasAgreedToTracking() || !credentials.getHasAnsweredAnalyticsQuestion()) {
            Answers.getInstance().logCustom(new CustomEvent(event));
        }
    }

    /**
     * Track an event with custom data
     *
     * @param event      Event title string
     * @param customData Hashmap of custom attribute fields
     */
    public void track(String event, HashMap<String, String> customData) {
        Credentials credentials = realm.where(Credentials.class).findFirst();
        if (credentials == null) {
            return;
        }

        if (credentials.getHasAgreedToTracking() || !credentials.getHasAnsweredAnalyticsQuestion()) {
            CustomEvent customEvent = new CustomEvent(event);
            for (Map.Entry<String, String> entry : customData.entrySet()) {
                customEvent.putCustomAttribute(entry.getKey(), entry.getValue());
            }
            Answers.getInstance().logCustom(customEvent);
        }
    }

    /**
     * Track a non-fatal exception
     *
     * @param t Throwable error
     */
    public static void trackCustomException(Throwable t) {
        Crashlytics.logException(t);
    }


    public void close() {
        context = null;
    }
}
