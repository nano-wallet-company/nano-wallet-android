package co.nano.nanowallet.ui.home;

import co.nano.nanowallet.R;

/**
 * Created by szeidner on 12/01/2018.
 */

public enum HomePagerEnum {
    NANO(R.layout.view_home_wallet),
    USD(R.layout.view_home_wallet),
    BTC(R.layout.view_home_wallet);

    private int mLayoutResId;

    HomePagerEnum(int layoutResId) {
        mLayoutResId = layoutResId;
    }

    public int getLayoutResId() {
        return mLayoutResId;
    }
}
