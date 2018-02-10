package co.nano.nanowallet.ui.intro;

import android.app.AlertDialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import co.nano.nanowallet.NanoUtil;
import co.nano.nanowallet.R;
import co.nano.nanowallet.broadcastreceiver.ClipboardAlarmReceiver;
import co.nano.nanowallet.databinding.FragmentIntroNewWalletBinding;
import co.nano.nanowallet.model.Credentials;
import co.nano.nanowallet.network.AccountService;
import co.nano.nanowallet.ui.common.ActivityWithComponent;
import co.nano.nanowallet.ui.common.BaseFragment;
import co.nano.nanowallet.ui.common.FragmentUtility;
import co.nano.nanowallet.ui.common.WindowControl;
import co.nano.nanowallet.ui.home.HomeFragment;
import io.realm.Realm;

/**
 * The Intro Screen to the app
 */

public class IntroNewWalletFragment extends BaseFragment {
    private FragmentIntroNewWalletBinding binding;
    private int currentStep = 1;
    public static String TAG = IntroNewWalletFragment.class.getSimpleName();
    private String seed;

    @Inject
    Realm realm;

    @Inject
    AccountService accountService;

    /**
     * Create new instance of the fragment (handy pattern if any data needs to be passed to it)
     *
     * @return
     */
    public static IntroNewWalletFragment newInstance() {
        Bundle args = new Bundle();
        IntroNewWalletFragment fragment = new IntroNewWalletFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // init dependency injection
        if (getActivity() instanceof ActivityWithComponent) {
            ((ActivityWithComponent) getActivity()).getActivityComponent().inject(this);
        }
        // inflate the view
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_intro_new_wallet, container, false);
        View view = binding.getRoot();

        setStatusBarWhite(view);
        hideToolbar();

        // create wallet seed
        realm.executeTransaction(realm -> {
            Credentials credentials = realm.createObject(Credentials.class);
            credentials.setSeed(NanoUtil.generateSeed());
            seed = credentials.getSeed();
        });

        accountService.open();

        // bind data to view
        binding.setHandlers(new ClickHandlers());
        binding.setSeed(seed);
        binding.introNewWalletMessage.setText(Html.fromHtml(getString(R.string.intro_new_wallet_message)));

        return view;
    }

    public class ClickHandlers {

        /**
         * Confirm button listener
         *
         * @param view
         */
        public void onClickConfirm(View view) {
            // show the copy seed dialog
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Light_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(getContext());
            }
            builder.setTitle(R.string.intro_new_wallet_continue_title)
                    .setMessage(R.string.intro_new_wallet_continue_message)
                    .setPositiveButton(R.string.intro_new_wallet_continue_positive, (dialog, which) -> {
                        // go to home screen
                        if (getActivity() instanceof WindowControl) {
                            ((WindowControl) getActivity()).getFragmentUtility().replace(
                                    HomeFragment.newInstance(),
                                    FragmentUtility.Animation.ENTER_LEFT_EXIT_RIGHT,
                                    FragmentUtility.Animation.ENTER_RIGHT_EXIT_LEFT,
                                    HomeFragment.TAG
                            );
                        }
                    })
                    .setNegativeButton(R.string.intro_new_wallet_continue_negative, (dialog, which) -> {

                    })
                    .show();
        }

        /**
         * Seed Click Listener
         *
         * @param view
         */
        public void onClickSeed(View view) {
            // copy address to clipboard
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText(ClipboardAlarmReceiver.CLIPBOARD_NAME, seed);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
            }
            // clear clipboard in 2 minutes
            setClearClipboardAlarm();

            // show the copy seed dialog
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Light_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(getContext());
            }
            builder.setTitle(R.string.intro_new_wallet_seed_copied_title)
                    .setMessage(R.string.intro_new_wallet_seed_copied)
                    .setPositiveButton(R.string.intro_new_wallet_seed_done, (dialog, which) -> {

                    })
                    .show();
        }
    }
}
