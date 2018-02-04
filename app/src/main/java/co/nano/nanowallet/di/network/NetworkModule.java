package co.nano.nanowallet.di.network;

import android.content.Context;

import co.nano.nanowallet.di.activity.ActivityScope;
import co.nano.nanowallet.network.AccountService;
import dagger.Module;
import dagger.Provides;

@Module
public class NetworkModule {
    @Provides
    @ActivityScope
    AccountService providesAccountService(Context context) {
        return new AccountService(context);
    }
}
