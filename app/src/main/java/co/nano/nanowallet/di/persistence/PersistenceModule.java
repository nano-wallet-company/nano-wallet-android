package co.nano.nanowallet.di.persistence;

import android.content.Context;
import android.util.Base64;

import javax.inject.Named;

import co.nano.nanowallet.db.UuidMigration;
import co.nano.nanowallet.di.application.ApplicationScope;
import co.nano.nanowallet.util.SharedPreferencesUtil;
import co.nano.nanowallet.util.Vault;
import dagger.Module;
import dagger.Provides;
import io.realm.Realm;
import io.realm.RealmConfiguration;

@Module
public class PersistenceModule {

    @Provides
    @ApplicationScope
    SharedPreferencesUtil providesSharedPreferencesUtil(Context context) {
        return new SharedPreferencesUtil(context);
    }

    @Provides
    Realm providesRealmInstance(@Named("encryption_key") byte[] key) {
        if (key != null) {
            RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                    .name("nano.realm")
                    .encryptionKey(key)
                    .schemaVersion(2)
                    .migration(new UuidMigration())
                    .build();

            // Open the Realm with encryption enabled
            return Realm.getInstance(realmConfiguration);
        } else {
            RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                    .name("nano.realm")
                    .schemaVersion(2)
                    .migration(new UuidMigration())
                    .build();

            // Open the Realm with encryption enabled
            return Realm.getInstance(realmConfiguration);
        }
    }

    @Provides
    @Named("cachedir")
    @ApplicationScope
    String providesCacheDirectory(Context context) {
        return context.getCacheDir().getAbsolutePath();
    }

    @Provides
    @Named("encryption_key")
    byte[] providesEncryptionKey() {
        if (Vault.getVault().getString(Vault.ENCRYPTION_KEY_NAME, null) == null) {
            Vault.getVault()
                    .edit()
                    .putString(Vault.ENCRYPTION_KEY_NAME,
                            Base64.encodeToString(Vault.generateKey(), Base64.DEFAULT))
                    .apply();
        }
        if (Vault.getVault() != null) {
            return Base64.decode(Vault.getVault().getString(Vault.ENCRYPTION_KEY_NAME, null), Base64.DEFAULT);
        } else {
            return null;
        }
    }
}
