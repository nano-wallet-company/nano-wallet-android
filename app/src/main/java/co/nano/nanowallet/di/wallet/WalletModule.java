package co.nano.nanowallet.di.wallet;

import android.content.Context;

import co.nano.nanowallet.model.NanoWallet;
import dagger.Module;
import dagger.Provides;

@Module
public class WalletModule {
    @Provides
    NanoWallet providesNanoWallet(Context context) {
        return new NanoWallet(context);
    }
}
