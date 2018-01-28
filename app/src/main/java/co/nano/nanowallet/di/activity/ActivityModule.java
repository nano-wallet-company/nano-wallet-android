package co.nano.nanowallet.di.activity;

import android.content.Context;

import dagger.Module;

@Module
public class ActivityModule {
    private final Context mContext;

    public ActivityModule(Context context) {
        mContext = context;
    }

}
