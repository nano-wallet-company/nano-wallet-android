package co.nano.nanowallet.di.activity;

import co.nano.nanowallet.di.application.ApplicationComponent;
import co.nano.nanowallet.model.NanoWalletTest;
import dagger.Component;

@Component(modules = {ActivityModule.class}, dependencies = {ApplicationComponent.class})
@ActivityScope
public interface TestActivityComponent extends ActivityComponent {
    void inject(NanoWalletTest nanoWalletTest);
}
