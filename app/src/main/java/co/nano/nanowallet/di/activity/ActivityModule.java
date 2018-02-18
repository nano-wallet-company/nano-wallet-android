package co.nano.nanowallet.di.activity;

import android.content.Context;

import co.nano.nanowallet.model.NanoWallet;
import co.nano.nanowallet.network.AccountService;
import dagger.Module;
import dagger.Provides;

@Module
public class ActivityModule {
    private final Context mContext;
    private NanoWallet mNanoWallet;

    public ActivityModule(Context context) {
        mContext = context;
    }

    @Provides
    Context providesActivityContext() {
        return mContext;
    }

    @Provides
    NanoWallet providesNanoWallet(Context context) {
        if (mNanoWallet == null) {
            mNanoWallet = new NanoWallet(context);
        }
        return mNanoWallet;
    }

    @Provides
    @ActivityScope
    AccountService providesAccountService(Context context) {
        return new AccountService(context);
    }
}
