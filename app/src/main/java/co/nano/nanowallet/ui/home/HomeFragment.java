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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import co.nano.nanowallet.NanoWallet;
import co.nano.nanowallet.R;
import co.nano.nanowallet.databinding.FragmentHomeBinding;
import co.nano.nanowallet.model.Transaction;
import co.nano.nanowallet.ui.common.BaseDialogFragment;
import co.nano.nanowallet.ui.common.BaseFragment;
import co.nano.nanowallet.ui.common.FragmentUtility;
import co.nano.nanowallet.ui.common.WindowControl;
import co.nano.nanowallet.ui.receive.ReceiveDialogFragment;
import co.nano.nanowallet.ui.send.SendFragment;
import co.nano.nanowallet.ui.settings.SettingsDialogFragment;

/**
 * Home Wallet Screen
 */

@BindingMethods({
        @BindingMethod(type = android.support.v7.widget.AppCompatImageView.class,
                attribute = "app:srcCompat",
                method = "setImageDrawable")
})
public class HomeFragment extends BaseFragment {
    private FragmentHomeBinding binding;
    private WalletController controller;
    public static String TAG = HomeFragment.class.getSimpleName();

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
        super.onCreateOptionsMenu(menu,inflater);
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
                    dialog.getDialog().setOnDismissListener(dialogInterface -> setStatusBarBlue());
                }
                return true;
        }

        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // set status bar to blue
        setStatusBarBlue();
        setTitle("");
        setTitleDrawable(R.drawable.ic_logo_toolbar);
        setBackEnabled(false);

        // inflate the view
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_home, container, false);
        View view = binding.getRoot();

        // bind data to view
        NanoWallet wallet = new NanoWallet();
        wallet.setAccountBalance(new BigDecimal("18024.12"));

        binding.setWallet(wallet);
        binding.setHandlers(new ClickHandlers());

        // initialize view pager (swipeable currency list)
        binding.homeViewpager.setAdapter(new CurrencyPagerAdapter(getContext(), wallet));
        binding.homeTabs.setupWithViewPager(binding.homeViewpager, true);

        // initialize recyclerview (list of wallet transactions)
        controller = new WalletController();
        binding.homeRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.homeRecyclerview.setAdapter(controller.getAdapter());
        // TODO: use real data here
        controller.setData(generateTestTransactions(), CurrencyPagerEnum.NANO);

        binding.homeSwiperefresh.setOnRefreshListener(() -> {
            // TODO: Refresh wallet data
            binding.homeSwiperefresh.setRefreshing(false);
        });

        return view;
    }

    /**
     * Generate a list of transactions to test the view with
     *
     * @return
     */
    private List<Transaction> generateTestTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        Date d1 = getRandomDate(new Date());
        Date d2 = getRandomDate(d1);
        Date d3 = getRandomDate(d2);
        Date d4 = getRandomDate(d3);
        Date d5 = getRandomDate(d4);
        Date d6 = getRandomDate(d5);
        Date d7 = getRandomDate(d6);
        Date d8 = getRandomDate(d7);
        Date d9 = getRandomDate(d8);
        transactions.add(new Transaction(new BigDecimal("223.438"), d1, "3gntuoguehi9d1mnhnar6ojx7jseeerwj5hesb4b4jga7oybbdbqyzap7ijg", true));
        transactions.add(new Transaction(new BigDecimal("100000"), d2, "3gntuoguehi9d1mnhnar6ojx7jseeerwj5hesb4b4jga7oybbdbqyzap7ijg", false));
        transactions.add(new Transaction(new BigDecimal("223.438"), d3, "3gntuoguehi9d1mnhnar6ojx7jseeerwj5hesb4b4jga7oybbdbqyzap7ijg", true));
        transactions.add(new Transaction(new BigDecimal("100000"), d4, "3gntuoguehi9d1mnhnar6ojx7jseeerwj5hesb4b4jga7oybbdbqyzap7ijg", false));
        transactions.add(new Transaction(new BigDecimal("223.438"), d5, "3gntuoguehi9d1mnhnar6ojx7jseeerwj5hesb4b4jga7oybbdbqyzap7ijg", true));
        transactions.add(new Transaction(new BigDecimal("100000"), d6, "3gntuoguehi9d1mnhnar6ojx7jseeerwj5hesb4b4jga7oybbdbqyzap7ijg", false));
        transactions.add(new Transaction(new BigDecimal("223.438"), d7, "3gntuoguehi9d1mnhnar6ojx7jseeerwj5hesb4b4jga7oybbdbqyzap7ijg", true));
        transactions.add(new Transaction(new BigDecimal("100000"), d8, "3gntuoguehi9d1mnhnar6ojx7jseeerwj5hesb4b4jga7oybbdbqyzap7ijg", false));
        transactions.add(new Transaction(new BigDecimal("223.438"), d9, "3gntuoguehi9d1mnhnar6ojx7jseeerwj5hesb4b4jga7oybbdbqyzap7ijg", true));
        return transactions;
    }

    /**
     * Generate a random date to use for test data
     *
     * @return
     */
    private Date getRandomDate(Date d1) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d1);
        cal.add(Calendar.DATE, -1);
        Date d2 = cal.getTime();

        long random;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            random = ThreadLocalRandom.current().nextLong(d2.getTime(), d1.getTime());
        } else {
            Random rnd = new Random();
            random = -946771200000L + (Math.abs(rnd.nextLong()) % (70L * 365 * 24 * 60 * 60 * 1000));
        }
        return new Date(random);
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
