package co.nano.nanowallet.di.activity;

import co.nano.nanowallet.MainActivity;
import co.nano.nanowallet.di.application.ApplicationComponent;
import co.nano.nanowallet.ui.settings.SettingsDialogFragment;
import dagger.Component;

@Component(modules = {ActivityModule.class}, dependencies = {ApplicationComponent.class})
@ActivityScope
public interface ActivityComponent {
    void inject(MainActivity mainActivity);

    void inject(SettingsDialogFragment settingsDialogFragment);
}
