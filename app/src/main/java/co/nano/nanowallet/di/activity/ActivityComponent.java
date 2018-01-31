package co.nano.nanowallet.di.activity;

import co.nano.nanowallet.MainActivity;
import co.nano.nanowallet.di.application.ApplicationComponent;
import co.nano.nanowallet.ui.home.HomeFragment;
import co.nano.nanowallet.ui.intro.IntroSeedFragment;
import co.nano.nanowallet.ui.intro.IntroWelcomeFragment;
import co.nano.nanowallet.ui.receive.ReceiveDialogFragment;
import co.nano.nanowallet.ui.send.SendFragment;
import co.nano.nanowallet.ui.settings.SettingsDialogFragment;
import dagger.Component;

@Component(modules = {ActivityModule.class}, dependencies = {ApplicationComponent.class})
@ActivityScope
public interface ActivityComponent {
    void inject(MainActivity mainActivity);

    void inject(SettingsDialogFragment settingsDialogFragment);

    void inject(HomeFragment homeFragment);

    void inject(SendFragment sendFragment);

    void inject(IntroWelcomeFragment introWelcomeFragment);

    void inject(IntroSeedFragment introSeedFragment);

    void inject(ReceiveDialogFragment receiveDialogFragment);
}
