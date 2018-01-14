package co.nano.nanowallet;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by szeidner on 13/01/2018.
 */

public class NanoApplication extends Application {
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
