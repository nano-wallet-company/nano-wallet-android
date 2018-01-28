package co.nano.nanowallet;

import android.app.Application;
import android.content.Context;

import com.facebook.stetho.Stetho;

import co.nano.nanowallet.di.application.ApplicationComponent;
import co.nano.nanowallet.di.application.ApplicationModule;
import co.nano.nanowallet.di.application.DaggerApplicationComponent;

/**
 * Any custom application logic can go here
 */

public class NanoApplication extends Application {
    private ApplicationComponent mApplicationComponent;

    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);

        // create new instance of the application component (DI)
        mApplicationComponent = DaggerApplicationComponent
                .builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    /**
     * Retrieve instance of application Dagger component
     *
     * @return ApplicationComponent
     */
    public ApplicationComponent getApplicationComponent() {
        return mApplicationComponent;
    }

    public static NanoApplication getApplication(Context context) {
        return (NanoApplication) context.getApplicationContext();
    }
}
