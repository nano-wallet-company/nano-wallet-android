package co.nano.nanowallet.ui.home;

import android.databinding.BindingMethod;
import android.databinding.BindingMethods;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.hwangjr.rxbus.annotation.Subscribe;

import javax.inject.Inject;

import co.nano.nanowallet.R;
import co.nano.nanowallet.bus.RxBus;
import co.nano.nanowallet.bus.WalletHistoryUpdate;
import co.nano.nanowallet.bus.WalletPriceUpdate;
import co.nano.nanowallet.bus.WalletSubscribeUpdate;
import co.nano.nanowallet.databinding.FragmentHomeBinding;
import co.nano.nanowallet.model.NanoWallet;
import co.nano.nanowallet.network.AccountService;
import co.nano.nanowallet.ui.common.ActivityWithComponent;
import co.nano.nanowallet.ui.common.BaseDialogFragment;
import co.nano.nanowallet.ui.common.BaseFragment;
import co.nano.nanowallet.ui.common.FragmentUtility;
import co.nano.nanowallet.ui.common.WindowControl;
import co.nano.nanowallet.ui.receive.ReceiveDialogFragment;
import co.nano.nanowallet.ui.send.SendFragment;
import co.nano.nanowallet.ui.settings.SettingsDialogFragment;
import co.nano.nanowallet.util.SharedPreferencesUtil;

/**
 * Home Wallet Screen
 */

@BindingMethods({
        @BindingMethod(type = android.support.v7.widget.AppCompatImageView.class,
                attribute = "srcCompat",
                method = "setImageDrawable")
})
public class HomeFragment extends BaseFragment {
    private FragmentHomeBinding binding;
    private WalletController controller;
    public static String TAG = HomeFragment.class.getSimpleName();

    @Inject
    SharedPreferencesUtil sharedPreferencesUtil;

    @Inject
    AccountService accountService;

    @Inject
    NanoWallet wallet;

    /**
     * Create new instance of the fragment (handy pattern if any data needs to be passed to it)
     *
     * @return
     */
    public static HomeFragment newInstance() {
        Bundle args = new Bundle();
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.home_settings:
                if (getActivity() instanceof WindowControl) {
                    // show settings dialog
                    SettingsDialogFragment dialog = SettingsDialogFragment.newInstance();
                    dialog.show(((WindowControl) getActivity()).getFragmentUtility().getFragmentManager(),
                            SettingsDialogFragment.TAG);

                    // make sure that dialog is not null
                    ((WindowControl) getActivity()).getFragmentUtility().getFragmentManager().executePendingTransactions();

                    // reset status bar to blue when dialog is closed
                    dialog.getDialog().setOnDismissListener(dialogInterface -> {
                        setStatusBarBlue();
                        if (binding.homeViewpager != null) {
                            binding.homeViewpager.setAdapter(new CurrencyPagerAdapter(getContext(), wallet, sharedPreferencesUtil.getLocalCurrency()));
                        }
                    });
                }
                return true;
        }

        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // unregister from bus
        RxBus.get().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // init dependency injection
        if (getActivity() instanceof ActivityWithComponent) {
            ((ActivityWithComponent) getActivity()).getActivityComponent().inject(this);
        }

        // subscribe to bus
        RxBus.get().register(this);

        // set status bar to blue
        setStatusBarBlue();
        setTitle("");
        setTitleDrawable(R.drawable.ic_logo_toolbar);
        setBackEnabled(false);

        // inflate the view
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_home, container, false);
        View view = binding.getRoot();

        binding.setHandlers(new ClickHandlers());

        // initialize view pager (swipeable currency list)
        binding.homeViewpager.setAdapter(new CurrencyPagerAdapter(getContext(), wallet, sharedPreferencesUtil.getLocalCurrency()));
        binding.homeTabs.setupWithViewPager(binding.homeViewpager, true);

        // initialize recyclerview (list of wallet transactions)
        controller = new WalletController();
        binding.homeRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.homeRecyclerview.setAdapter(controller.getAdapter());
        binding.homeSwiperefresh.setOnRefreshListener(accountService::requestHistory);
        if (wallet != null && wallet.getAccountHistory() != null) {
            controller.setData(wallet.getAccountHistory());
        }

        return view;
    }

    @Subscribe
    public void receiveHistory(WalletHistoryUpdate walletHistoryUpdate) {
        controller.setData(wallet.getAccountHistory());
        binding.homeSwiperefresh.setRefreshing(false);
    }

    @Subscribe
    public void receivePrice(WalletPriceUpdate walletPriceUpdate) {
        updateAmounts();
    }

    @Subscribe
    public void receiveSubscribe(WalletSubscribeUpdate walletSubscribeUpdate) {
        updateAmounts();
    }

    private void updateAmounts() {
        if (wallet != null) {
            binding.homeViewpager.setAdapter(new CurrencyPagerAdapter(getContext(), wallet, sharedPreferencesUtil.getLocalCurrency()));
        }
    }

    public class ClickHandlers {
        public void onClickReceive(View view) {
            if (getActivity() instanceof WindowControl) {
                // show receive dialog
                ReceiveDialogFragment dialog = ReceiveDialogFragment.newInstance();
                dialog.show(((WindowControl) getActivity()).getFragmentUtility().getFragmentManager(),
                        ReceiveDialogFragment.TAG);

                resetStatusBar(dialog);
            }
        }

        public void onClickSend(View view) {
            if (getActivity() instanceof WindowControl) {
                // navigate to send screen
                ((WindowControl) getActivity()).getFragmentUtility().add(
                        SendFragment.newInstance(),
                        FragmentUtility.Animation.ENTER_LEFT_EXIT_RIGHT,
                        FragmentUtility.Animation.ENTER_RIGHT_EXIT_LEFT,
                        SendFragment.TAG
                );
            }
        }

        /**
         * Execute all pending transactions and set up a listener to set the status bar to
         * blue when the dialog is closed
         *
         * @param dialog
         */
        private void resetStatusBar(BaseDialogFragment dialog) {
            // make sure that dialog is not null
            ((WindowControl) getActivity()).getFragmentUtility().getFragmentManager().executePendingTransactions();

            // reset status bar to blue when dialog is closed
            dialog.getDialog().setOnDismissListener(dialogInterface -> setStatusBarBlue());
        }
    }

}
