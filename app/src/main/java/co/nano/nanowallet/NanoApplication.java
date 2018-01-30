package co.nano.nanowallet;

import android.app.Application;
import android.content.Context;

import com.facebook.stetho.Stetho;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import co.nano.nanowallet.di.application.ApplicationComponent;
import co.nano.nanowallet.di.application.ApplicationModule;
import co.nano.nanowallet.di.application.DaggerApplicationComponent;
import io.realm.Realm;

/**
 * Any custom application logic can go here
 */

public class NanoApplication extends Application {
    private ApplicationComponent mApplicationComponent;
    private static final int SCHEMA_VERSION = 1;
    private static final String REALM_NAME = "nano.realm";

    public void onCreate() {
        super.onCreate();

        // initialize Realm database
        Realm.init(this);

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
                        .build());

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
