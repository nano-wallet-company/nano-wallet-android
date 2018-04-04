package co.nano.nanowallet;

import android.content.Context;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AppCompatDelegate;
import android.util.Base64;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.github.ajalt.reprint.core.Reprint;

import co.nano.nanowallet.di.application.ApplicationComponent;
import co.nano.nanowallet.di.application.ApplicationModule;
import co.nano.nanowallet.di.application.DaggerApplicationComponent;
import co.nano.nanowallet.util.Vault;
import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import timber.log.Timber;

/**
 * Any custom application logic can go here
 */

public class NanoApplication extends MultiDexApplication {
    private ApplicationComponent mApplicationComponent;

    public void onCreate() {
        super.onCreate();

        // initialize Realm database
        Realm.init(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        // create new instance of the application component (DI)
        mApplicationComponent = DaggerApplicationComponent
                .builder()
                .applicationModule(new ApplicationModule(this))
                .build();

        // initialize vault
        Vault.initializeVault(this);
        generateEncryptionKey();

        // initialize fingerprint
        Reprint.initialize(this);

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    /**
     * generate an encryption key and store in the vault
     */
    private void generateEncryptionKey() {
        if (Vault.getVault().getString(Vault.ENCRYPTION_KEY_NAME, null) == null) {
            Vault.getVault()
                    .edit()
                    .putString(Vault.ENCRYPTION_KEY_NAME,
                            Base64.encodeToString(Vault.generateKey(), Base64.DEFAULT))
                    .apply();
        }
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
