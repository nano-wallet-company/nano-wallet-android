package co.nano.nanowallet.ui.home;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.math.BigDecimal;

import co.nano.nanowallet.NanoWallet;
import co.nano.nanowallet.R;
import co.nano.nanowallet.databinding.FragmentHomeBinding;
import co.nano.nanowallet.ui.common.BaseFragment;
import co.nano.nanowallet.ui.common.FragmentControl;
import co.nano.nanowallet.ui.settings.SettingsDialogFragment;

/**
 * The Intro Screen to the app
 */

public class HomeFragment extends BaseFragment {
    private FragmentHomeBinding binding;
    public static String TAG = HomeFragment.class.getSimpleName();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // set status bar to blue
        setStatusBarBlue();

        // inflate the view
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_home, container, false);
        View view = binding.getRoot();

        // bind data to view
        NanoWallet wallet = new NanoWallet();
        wallet.setAccountBalance(new BigDecimal("18024.12"));

        binding.setWallet(wallet);
        binding.setHandlers(new ClickHandlers());

        // set up view pager
        binding.homeViewpager.setAdapter(new HomeAdapter(getContext()));
        binding.homeTabs.setupWithViewPager(binding.homeViewpager, true);

        return view;
    }

    public class ClickHandlers {
        public void onClickSettings(View view) {
            if (getActivity() instanceof FragmentControl) {
                // show settings dialog
                SettingsDialogFragment dialog = SettingsDialogFragment.newInstance();
                dialog.show(((FragmentControl) getActivity()).getFragmentUtility().getFragmentManager(),
                        SettingsDialogFragment.TAG);

                // make sure that dialog is not null
                ((FragmentControl) getActivity()).getFragmentUtility().getFragmentManager().executePendingTransactions();

                // reset status bar to blue when dialog is closed
                dialog.getDialog().setOnDismissListener(dialogInterface -> setStatusBarBlue());
            }

        }
//
//        public void onClickHaveWallet(View view) {
//            Log.d(TAG, "Have Wallet");
//        }
    }


}
