package co.nano.nanowallet.ui.home;

import android.databinding.BindingMethod;
import android.databinding.BindingMethods;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hwangjr.rxbus.annotation.Subscribe;

import java.math.BigDecimal;

import javax.inject.Inject;

import co.nano.nanowallet.R;
import co.nano.nanowallet.analytics.AnalyticsEvents;
import co.nano.nanowallet.analytics.AnalyticsService;
import co.nano.nanowallet.bus.RxBus;
import co.nano.nanowallet.bus.SocketError;
import co.nano.nanowallet.bus.WalletHistoryUpdate;
import co.nano.nanowallet.bus.WalletPriceUpdate;
import co.nano.nanowallet.bus.WalletSubscribeUpdate;
import co.nano.nanowallet.databinding.FragmentHomeBinding;
import co.nano.nanowallet.model.Credentials;
import co.nano.nanowallet.model.NanoWallet;
import co.nano.nanowallet.network.AccountService;
import co.nano.nanowallet.network.model.response.AccountCheckResponse;
import co.nano.nanowallet.network.model.response.AccountHistoryResponseItem;
import co.nano.nanowallet.ui.common.ActivityWithComponent;
import co.nano.nanowallet.ui.common.BaseDialogFragment;
import co.nano.nanowallet.ui.common.BaseFragment;
import co.nano.nanowallet.ui.common.FragmentUtility;
import co.nano.nanowallet.ui.common.KeyboardUtil;
import co.nano.nanowallet.ui.common.WindowControl;
import co.nano.nanowallet.ui.receive.ReceiveDialogFragment;
import co.nano.nanowallet.ui.send.SendFragment;
import co.nano.nanowallet.ui.settings.SettingsDialogFragment;
import co.nano.nanowallet.ui.webview.WebViewDialogFragment;
import co.nano.nanowallet.util.ExceptionHandler;
import io.realm.Realm;

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
    private boolean logoutClicked = false;

    @Inject
    AccountService accountService;

    @Inject
    NanoWallet wallet;

    @Inject
    AnalyticsService analyticsService;

    @Inject
    Realm realm;

    /**
     * Create new instance of the fragment (handy pattern if any data needs to be passed to it)
     *
     * @return HomeFragment
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
                            updateAmounts();
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

        analyticsService.track(AnalyticsEvents.HOME_VIEWED);

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
        view = binding.getRoot();

        // hide keyboard
        KeyboardUtil.hideKeyboard(getActivity());

        binding.setHandlers(new ClickHandlers());

        // initialize view pager (swipeable currency list)
        binding.homeViewpager.setAdapter(new CurrencyPagerAdapter(getContext(), wallet));
        binding.homeTabs.setupWithViewPager(binding.homeViewpager, true);

        // initialize recyclerview (list of wallet transactions)
        controller = new WalletController();
        binding.homeRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.homeRecyclerview.setAdapter(controller.getAdapter());
        binding.homeSwiperefresh.setOnRefreshListener(() -> {
            accountService.requestUpdate();
            new Handler().postDelayed(() -> binding.homeSwiperefresh.setRefreshing(false), 5000);
        });
        if (wallet != null && wallet.getAccountHistory() != null) {
            controller.setData(wallet.getAccountHistory(), new ClickHandlers());
        }

        updateAmounts();

        Credentials credentials = realm.where(Credentials.class).findFirst();
//        if (credentials != null && !credentials.getHasAnsweredAnalyticsQuestion()) {
//            showAnalyticsOptIn(analyticsService, realm);
//        }

        if (credentials != null && !credentials.getSeedIsSecure() && !credentials.getHasSentToNewSeed()) {
            showSeedUpdateAlert();
        } else if (credentials != null && credentials.getNewlyGeneratedSeed() != null) {
            showSeedReminderAlert(credentials.getNewlyGeneratedSeed());
        }

        return view;
    }

    @Subscribe
    public void receiveHistory(WalletHistoryUpdate walletHistoryUpdate) {
        controller.setData(wallet.getAccountHistory(), new ClickHandlers());
        binding.homeSwiperefresh.setRefreshing(false);
        binding.homeRecyclerview.getLayoutManager().scrollToPosition(0);
    }

    @Subscribe
    public void receivePrice(WalletPriceUpdate walletPriceUpdate) {
        updateAmounts();
    }

    @Subscribe
    public void receiveSubscribe(WalletSubscribeUpdate walletSubscribeUpdate) {
        updateAmounts();
    }

    @Subscribe
    public void receiveAccountCheck(AccountCheckResponse accountCheckResponse) {
        if (accountCheckResponse.getReady()) {
            // account is on the network, so send a pending request
            accountService.requestPending();
        }
    }

    @Subscribe
    public void receiveError(SocketError error) {
        binding.homeSwiperefresh.setRefreshing(false);
        Toast.makeText(getContext(),
                getString(R.string.error_message),
                Toast.LENGTH_SHORT)
                .show();
    }

    private void updateAmounts() {
        if (wallet != null) {
            ((CurrencyPagerAdapter) binding.homeViewpager.getAdapter()).updateData(wallet);
            if (wallet.getAccountBalanceNanoRaw() != null &&
                    wallet.getAccountBalanceNanoRaw().compareTo(new BigDecimal(0)) == 1) {
                // if balance > 0, enable send button
                binding.homeSendButton.setEnabled(true);
            } else {
                binding.homeSendButton.setEnabled(false);
            }
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

        public void onClickTransaction(View view) {
            if (getActivity() instanceof WindowControl) {
                AccountHistoryResponseItem accountHistoryItem = (AccountHistoryResponseItem) view.getTag();
                if (accountHistoryItem != null) {
                    // show webview dialog
                    WebViewDialogFragment dialog = WebViewDialogFragment.newInstance(getString(R.string.home_explore_url, accountHistoryItem.getHash()), "");
                    dialog.show(((WindowControl) getActivity()).getFragmentUtility().getFragmentManager(),
                            WebViewDialogFragment.TAG);

                    resetStatusBar(dialog);
                }
            }
        }

        /**
         * Execute all pending transactions and set up a listener to set the status bar to
         * blue when the dialog is closed
         *
         * @param dialog Instance of the dialog to listen for closing on
         */
        private void resetStatusBar(BaseDialogFragment dialog) {
            // make sure that dialog is not null
            ((WindowControl) getActivity()).getFragmentUtility().getFragmentManager().executePendingTransactions();

            // reset status bar to blue when dialog is closed
            if (dialog != null && dialog.getDialog() != null) {
                dialog.getDialog().setOnDismissListener(dialogInterface -> setStatusBarBlue());
            }
        }
    }
}
