package co.nano.nanowallet.ui.home;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.nano.nanowallet.BR;
import co.nano.nanowallet.model.NanoWallet;

/**
 * ViewPager Adapter that is used for listing the wallet transactions on the home screen
 */

public class CurrencyPagerAdapter extends PagerAdapter {

    private Context mContext;
    private NanoWallet mNanoWallet;

    public CurrencyPagerAdapter(Context context, NanoWallet wallet) {
        mContext = context;
        mNanoWallet = wallet;
    }

    public void updateData(NanoWallet wallet) {
        mNanoWallet = wallet;
        notifyDataSetChanged();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        CurrencyPagerEnum customPagerEnum = CurrencyPagerEnum.values()[position];
        LayoutInflater inflater = LayoutInflater.from(mContext);

        ViewDataBinding layout = DataBindingUtil.inflate(inflater, customPagerEnum.getLayoutResId(), container, false);
        layout.setVariable(BR.wallet, mNanoWallet);
        layout.setVariable(BR.localCurrency, mNanoWallet.getLocalCurrency());

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

    @Override
    public int getItemPosition(Object object){
        return POSITION_NONE;
    }

}

