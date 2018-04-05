package co.nano.nanowallet.di.activity;

import com.google.gson.Gson;

import co.nano.nanowallet.MainActivity;
import co.nano.nanowallet.analytics.AnalyticsService;
import co.nano.nanowallet.di.application.ApplicationComponent;
import co.nano.nanowallet.model.NanoWallet;
import co.nano.nanowallet.network.AccountService;
import co.nano.nanowallet.ui.home.HomeFragment;
import co.nano.nanowallet.ui.intro.IntroLegalFragment;
import co.nano.nanowallet.ui.intro.IntroNewWalletFragment;
import co.nano.nanowallet.ui.intro.IntroSeedFragment;
import co.nano.nanowallet.ui.intro.IntroWelcomeFragment;
import co.nano.nanowallet.ui.pin.CreatePinDialogFragment;
import co.nano.nanowallet.ui.pin.PinDialogFragment;
import co.nano.nanowallet.ui.receive.ReceiveDialogFragment;
import co.nano.nanowallet.ui.send.SendFragment;
import co.nano.nanowallet.ui.settings.SettingsDialogFragment;
import dagger.Component;

@Component(modules = {ActivityModule.class}, dependencies = {ApplicationComponent.class})
@ActivityScope
public interface ActivityComponent {
    @ActivityScope
    AccountService provideAccountService();

    // wallet
    NanoWallet provideNanoWallet();

    @ActivityScope
    Gson provideGson();

    void inject(AccountService accountService);

    void inject(CreatePinDialogFragment createPinDialogFragment);

    void inject(HomeFragment homeFragment);

    void inject(IntroLegalFragment introLegalFragment);

    void inject(IntroNewWalletFragment introNewWalletFragment);

    void inject(IntroWelcomeFragment introWelcomeFragment);

    void inject(IntroSeedFragment introSeedFragment);

    void inject(MainActivity mainActivity);

    void inject(NanoWallet nanoWallet);

    void inject(PinDialogFragment pinDialogFragment);

    void inject(ReceiveDialogFragment receiveDialogFragment);

    void inject(SendFragment sendFragment);

    void inject(SettingsDialogFragment settingsDialogFragment);
}
