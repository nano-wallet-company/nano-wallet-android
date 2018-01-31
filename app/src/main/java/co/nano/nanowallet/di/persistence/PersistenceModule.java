package co.nano.nanowallet.di.persistence;

import android.content.Context;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.SecureRandom;

import javax.inject.Named;

import co.nano.nanowallet.di.application.ApplicationScope;
import co.nano.nanowallet.util.SharedPreferencesUtil;
import dagger.Module;
import dagger.Provides;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import timber.log.Timber;

@Module
public class PersistenceModule {

    @Provides
    @ApplicationScope
    SharedPreferencesUtil providesSharedPreferencesUtil(Context context) {
        return new SharedPreferencesUtil(context);
    }

    @Provides
    Realm providesRealmInstance(@Named("encryption_key") byte[] key) {
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .name("nano.realm")
                .schemaVersion(1)
                .build();

        // Open the Realm with encryption enabled
        return Realm.getInstance(realmConfiguration);
    }

    @Provides
    @Named("cachedir")
    @ApplicationScope
    String providesCacheDirectory(Context context) {
        return context.getCacheDir().getAbsolutePath();
    }

    @Provides
    @Named("encryption_key")
    byte[] providesKeyPair() {
        byte[] key = new byte[64];
        new SecureRandom().nextBytes(key);
        return key;
    }

    @Provides
    @ApplicationScope
    KeyStore providesKeystore() {
        try {
            return KeyStore.getInstance("Nano");
        } catch (KeyStoreException e) {
            Timber.e(e.getMessage());
        }
        return null;
    }
}
