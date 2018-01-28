package co.nano.nanowallet.di.persistence;

import android.content.Context;

import javax.inject.Named;

import co.nano.nanowallet.di.application.ApplicationScope;
import co.nano.nanowallet.util.SharedPreferencesUtil;
import dagger.Module;
import dagger.Provides;

@Module
public class PersistenceModule {

    @Provides
    @ApplicationScope
    SharedPreferencesUtil providesSharedPreferencesUtil(Context context) {
        return new SharedPreferencesUtil(context);
    }

    @Provides
    @Named("cachedir")
    @ApplicationScope
    String providesCacheDirectory(Context context) {
        return context.getCacheDir().getAbsolutePath();
    }
}
