package co.nano.nanowallet.di.application;

import android.content.Context;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {
    final Context mContext;

    public ApplicationModule(Context context) {
        mContext = context;
    }

    @Provides
    Context providesApplicationContext() {
        return mContext;
    }


}
