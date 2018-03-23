package co.nano.nanowallet.ui.intro;

import android.app.AlertDialog;
import android.databinding.DataBindingUtil;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import co.nano.nanowallet.R;
import co.nano.nanowallet.bus.Logout;
import co.nano.nanowallet.bus.RxBus;
import co.nano.nanowallet.databinding.FragmentIntroLegalBinding;
import co.nano.nanowallet.model.Credentials;
import co.nano.nanowallet.network.AccountService;
import co.nano.nanowallet.ui.common.ActivityWithComponent;
import co.nano.nanowallet.ui.common.BaseFragment;
import co.nano.nanowallet.ui.common.FragmentUtility;
import co.nano.nanowallet.ui.common.WindowControl;
import co.nano.nanowallet.ui.home.HomeFragment;
import co.nano.nanowallet.util.SharedPreferencesUtil;
import io.realm.Realm;

/**
 * The Intro Screen to the app
 */

public class IntroLegalFragment extends BaseFragment {
    public static String TAG = IntroLegalFragment.class.getSimpleName();
    private String seed;
    private FragmentIntroLegalBinding binding;

    @Inject
    Realm realm;

    @Inject
    AccountService accountService;

    @Inject
    SharedPreferencesUtil sharedPreferencesUtil;

    /**
     * Create new instance of the fragment (handy pattern if any data needs to be passed to it)
     *
     * @return IntroNewWalletFragment instance
     */
    public static IntroLegalFragment newInstance() {
        Bundle args = new Bundle();
        IntroLegalFragment fragment = new IntroLegalFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Answers.getInstance().logCustom(new CustomEvent("Legal VC Viewed"));

        // init dependency injection
        if (getActivity() instanceof ActivityWithComponent) {
            ((ActivityWithComponent) getActivity()).getActivityComponent().inject(this);
        }
        // inflate the view
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_intro_legal, container, false);
        view = binding.getRoot();

        setStatusBarWhite(view);
        hideToolbar();

        // subscribe to bus
        RxBus.get().register(this);

        // bind data to view
        binding.setHandlers(new ClickHandlers());
        binding.introLegalTitle.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);

        // set up the links on terms of service checkbox
        createLink(binding.introLegalLabelDisclaimer, R.string.intro_legal_checkbox_disclaimer);
        createLink(binding.introLegalLabelEula, R.string.intro_legal_checkbox_eula);
        createLink(binding.introLegalLabelPp, R.string.intro_legal_checkbox_pp);

        accountService.open();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // unregister from bus
        RxBus.get().unregister(this);
    }

    /**
     * Set the state of the agree button
     */
    private void setAgreeButtonState() {
        if (binding.introLegalCheckboxDisclaimer.isChecked() &&
                binding.introLegalCheckboxEula.isChecked() &&
                binding.introLegalCheckboxPp.isChecked()) {
            binding.introLegalButtonConfirm.setEnabled(true);
        } else {
            binding.introLegalButtonConfirm.setEnabled(false);
        }
    }

    /**
     * Send an analytics event when a checkmark is toggled
     * @param title Title of the event
     * @param isChecked Whether the checkbox is checked or not
     */
    private void sendCheckToggledEvent(String title, boolean isChecked) {
        // get date
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy-HH:mm:ssXXX", Locale.getDefault());

        // send event
        Answers.getInstance().logCustom(new CustomEvent(title)
                .putCustomAttribute("device_id", sharedPreferencesUtil.getAppInstallUuid())
                .putCustomAttribute("accepted", isChecked ? "true" : "false")
                .putCustomAttribute("date", df.format(c))
        );
    }

    /**
     * Send an analytics event when a link is viewed
     * @param title Title of the event
     */
    private void sendLinkViewedEvent(String title) {
        // get date
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy-HH:mm:ssXXX", Locale.getDefault());

        // send event
        Answers.getInstance().logCustom(new CustomEvent(title)
                .putCustomAttribute("device_id", sharedPreferencesUtil.getAppInstallUuid())
                .putCustomAttribute("date", df.format(c))
        );
    }

    public class ClickHandlers {

        public void onDisclaimerCheckChanged(CompoundButton view, boolean isChecked) {
            setAgreeButtonState();
            sendCheckToggledEvent("Mobile Disclaimer Agreement Toggled", isChecked);
        }

        public void onEULACheckChanged(CompoundButton view, boolean isChecked) {
            setAgreeButtonState();
            sendCheckToggledEvent("Mobile EULA Agreement Toggled", isChecked);
        }

        public void onPPCheckChanged(CompoundButton view, boolean isChecked) {
            setAgreeButtonState();
            sendCheckToggledEvent("Mobile Privacy Policy Agreement Toggled", isChecked);
        }

        public void onDisclaimerLinkClicked(View view) {
            sendLinkViewedEvent("Mobile Disclaimer Viewed");
        }

        public void onEULALinkClicked(View view) {
            sendLinkViewedEvent("Mobile EULA Viewed");
        }

        public void onPPLinkClicked(View view) {
            sendLinkViewedEvent("Mobile Privacy Policy Viewed");
        }

        /**
         * Deny button listener
         * @param view View
         */
        public void onClickDeny(View view) {
            if (getActivity() instanceof WindowControl) {
                // show the logout are-you-sure dialog
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Light_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(getContext());
                }
                builder.setTitle(R.string.intro_legal_alert_title)
                        .setMessage(R.string.intro_legal_alert_message)
                        .setPositiveButton(R.string.intro_legal_alert_confirm_cta, (dialog, which) -> {
                            RxBus.get().post(new Logout());
                        })
                        .setNegativeButton(R.string.intro_legal_alert_cancel_cta, (dialog, which) -> {
                            // do nothing which dismisses the dialog
                        })
                        .show();
            }
        }


        /**
         * Confirm button listener
         * @param view View
         */
        public void onClickConfirm(View view) {
            if (binding.introLegalCheckboxDisclaimer.isChecked() &&
                    binding.introLegalCheckboxEula.isChecked() &&
                    binding.introLegalCheckboxPp.isChecked()) {

                // set confirm flag
                realm.beginTransaction();
                Credentials credentials = realm.where(Credentials.class).findFirst();
                if (credentials != null) {
                    credentials.setHasCompletedLegalAgreements(true);
                }
                realm.commitTransaction();

                // proceed
                if (sharedPreferencesUtil.getFromNewWallet()) {
                    if (getActivity() instanceof WindowControl) {
                        ((WindowControl) getActivity()).getFragmentUtility().replace(
                                IntroNewWalletFragment.newInstance(),
                                FragmentUtility.Animation.ENTER_LEFT_EXIT_RIGHT,
                                FragmentUtility.Animation.ENTER_RIGHT_EXIT_LEFT,
                                IntroSeedFragment.TAG
                        );
                    }
                } else {
                    if (getActivity() instanceof WindowControl) {
                        ((WindowControl) getActivity()).getFragmentUtility().replace(
                                HomeFragment.newInstance(),
                                FragmentUtility.Animation.ENTER_LEFT_EXIT_RIGHT,
                                FragmentUtility.Animation.ENTER_RIGHT_EXIT_LEFT,
                                HomeFragment.TAG
                        );
                    }
                }
            }
        }
    }
}
