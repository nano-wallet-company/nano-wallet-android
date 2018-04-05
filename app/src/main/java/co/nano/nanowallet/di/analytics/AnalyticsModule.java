package co.nano.nanowallet.di.analytics;

import android.content.Context;
import android.util.Base64;

import javax.inject.Named;

import co.nano.nanowallet.analytics.AnalyticsService;
import co.nano.nanowallet.bus.Logout;
import co.nano.nanowallet.bus.RxBus;
import co.nano.nanowallet.db.Migration;
import co.nano.nanowallet.di.activity.ActivityScope;
import co.nano.nanowallet.di.application.ApplicationScope;
import co.nano.nanowallet.di.persistence.PersistenceModule;
import co.nano.nanowallet.util.SharedPreferencesUtil;
import co.nano.nanowallet.util.Vault;
import dagger.Module;
import dagger.Provides;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmFileException;

@Module(includes = PersistenceModule.class)
public class AnalyticsModule {
    @Provides
    @ApplicationScope
    AnalyticsService providesAnalyticsService(Context context, Realm realm) {
        return new AnalyticsService(context.getApplicationContext(), realm);
    }
}
