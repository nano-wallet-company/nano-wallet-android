package co.nano.nanowallet.di.application;


import co.nano.nanowallet.di.persistence.PersistenceModule;
import co.nano.nanowallet.util.SharedPreferencesUtil;
import dagger.Component;

@Component(modules = {ApplicationModule.class, PersistenceModule.class})
@ApplicationScope
public interface ApplicationComponent {
    // persistence module
    SharedPreferencesUtil provideSharedPreferencesUtil();
}
