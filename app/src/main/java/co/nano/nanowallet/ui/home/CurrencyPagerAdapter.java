package co.nano.nanowallet.ui.home;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.nano.nanowallet.NanoWallet;
import co.nano.nanowallet.BR;
import co.nano.nanowallet.model.AvailableCurrency;

/**
 * ViewPager Adapter that is used for listing the wallet transactions on the home screen
 */

public class CurrencyPagerAdapter extends PagerAdapter {

    private Context mContext;
    private NanoWallet mNanoWallet;
    private AvailableCurrency mLocalCurrency;

    public CurrencyPagerAdapter(Context context, NanoWallet wallet, AvailableCurrency localCurrency) {
        mContext = context;
        mNanoWallet = wallet;
        mLocalCurrency = localCurrency;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        CurrencyPagerEnum customPagerEnum = CurrencyPagerEnum.values()[position];
        LayoutInflater inflater = LayoutInflater.from(mContext);

        ViewDataBinding layout = DataBindingUtil.inflate(inflater, customPagerEnum.getLayoutResId(), container, false);
        layout.setVariable(BR.wallet, mNanoWallet);
        layout.setVariable(BR.localCurrency, mLocalCurrency);

        container.addView(layout.getRoot());
        return layout.getRoot();
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return CurrencyPagerEnum.values().length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return null;
    }

}

