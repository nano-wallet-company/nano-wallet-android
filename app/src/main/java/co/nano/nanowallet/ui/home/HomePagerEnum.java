package co.nano.nanowallet.ui.home;

import co.nano.nanowallet.R;

/**
 * Created by szeidner on 12/01/2018.
 */

public enum HomePagerEnum {
    NANO(R.layout.view_home_amount_nano),
    BTC(R.layout.view_home_amount_btc),
    USD(R.layout.view_home_amount_usd);

    private int mLayoutResId;

    HomePagerEnum(int layoutResId) {
        mLayoutResId = layoutResId;
    }

    public int getLayoutResId() {
        return mLayoutResId;
    }
}
