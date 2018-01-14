package co.nano.nanowallet;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Any custom application logic can go here
 */

public class NanoApplication extends Application {
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
