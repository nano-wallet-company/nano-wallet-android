package co.nano.nanowallet.ui.intro;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.databinding.DataBindingUtil;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.hwangjr.rxbus.annotation.Subscribe;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.inject.Inject;

import co.nano.nanowallet.R;
import co.nano.nanowallet.analytics.AnalyticsEvents;
import co.nano.nanowallet.analytics.AnalyticsService;
import co.nano.nanowallet.bus.AcceptAgreement;
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
import co.nano.nanowallet.ui.webview.WebViewAgreementDialogFragment;
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

    @Inject
    AnalyticsService analyticsService;

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

    @SuppressLint("ClickableViewAccessibility")
    private OnTouchListener checkBoxTouchListener = (view, motionEvent) -> {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            view.callOnClick();
            return true; //this will prevent checkbox from changing state
        }
        return false;
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // init dependency injection
        if (getActivity() instanceof ActivityWithComponent) {
            ((ActivityWithComponent) getActivity()).getActivityComponent().inject(this);
        }

        analyticsService.track(AnalyticsEvents.LEGAL_VIEWED);

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
        binding.introLegalCheckboxEula.setOnTouchListener(checkBoxTouchListener);
        binding.introLegalLabelEula.setPaintFlags(binding.introLegalCheckboxEula.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        binding.introLegalCheckboxPp.setOnTouchListener(checkBoxTouchListener);
        binding.introLegalLabelPp.setPaintFlags(binding.introLegalCheckboxPp.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        accountService.open();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // unregister from bus
        RxBus.get().unregister(this);
    }

    @Subscribe
    public void receiveHistory(AcceptAgreement acceptAgreement) {
        if (binding != null &&
                binding.introLegalCheckboxEula != null &&
                binding.introLegalCheckboxPp != null) {
            if (acceptAgreement.getAgreementId().equals(LegalTypes.EULA.getName())) {
                binding.introLegalCheckboxEula.setChecked(true);
            } else if (acceptAgreement.getAgreementId().equals(LegalTypes.PRIVACY.getName())) {
                binding.introLegalCheckboxPp.setChecked(true);
            }
        }
    }

    /**
     * Set the state of the agree button
     */
    private void setAgreeButtonState() {
        if (binding.introLegalCheckboxEula.isChecked() &&
                binding.introLegalCheckboxPp.isChecked()) {
            binding.introLegalButtonConfirm.setEnabled(true);
        } else {
            binding.introLegalButtonConfirm.setEnabled(false);
        }
    }

    /**
     * Send an analytics event when a checkmark is toggled
     *
     * @param title     Title of the event
     * @param isChecked Whether the checkbox is checked or not
     */
    private void sendCheckToggledEvent(String title, boolean isChecked) {
        // get date
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy-HH:mm:ssZZZZZ", Locale.getDefault());

        // send event
        final HashMap<String, String> customData = new HashMap<>();
        customData.put("device_id", sharedPreferencesUtil.getAppInstallUuid());
        customData.put("accepted", isChecked ? "true" : "false");
        customData.put("date", df.format(c));
        analyticsService.track(title, customData);
    }

    /**
     * Send an analytics event when a link is viewed
     *
     * @param title Title of the event
     */
    private void sendLinkViewedEvent(String title) {
        // get date
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy-HH:mm:ssZZZZZ", Locale.getDefault());

        // send event
        final HashMap<String, String> customData = new HashMap<>();
        customData.put("device_id", sharedPreferencesUtil.getAppInstallUuid());
        customData.put("date", df.format(c));
        analyticsService.track(title, customData);
    }

    private void openAgreementView(String url, String title, String id) {
        if (getActivity() instanceof WindowControl) {
            WebViewAgreementDialogFragment
                    .newInstance(url, title, id)
                    .show(
                            ((WindowControl) getActivity()).getFragmentUtility().getFragmentManager(),
                            WebViewAgreementDialogFragment.TAG
                    );
        }
    }

    public class ClickHandlers {

        public void onDisclaimerCheckChanged(CompoundButton view, boolean isChecked) {
            setAgreeButtonState();
            sendCheckToggledEvent(AnalyticsEvents.DISCLAIMER_AGREEMENT_TOGGLED, isChecked);
        }

        public void onEULACheckChanged(CompoundButton view, boolean isChecked) {
            setAgreeButtonState();
            sendCheckToggledEvent(AnalyticsEvents.EULA_AGREEMENT_TOGGLED, isChecked);
        }

        public void onPPCheckChanged(CompoundButton view, boolean isChecked) {
            setAgreeButtonState();
            sendCheckToggledEvent(AnalyticsEvents.PRIVACY_POLICY_AGREEMENT_TOGGLED, isChecked);
        }

        public void onEULALinkClicked(View view) {
            openAgreementView(getString(R.string.intro_legal_checkbox_eula_link),
                    getString(R.string.intro_legal_checkbox_eula_label),
                    LegalTypes.EULA.getName());
            sendLinkViewedEvent(AnalyticsEvents.EULA_VIEWED);
        }

        public void onPPLinkClicked(View view) {
            openAgreementView(getString(R.string.intro_legal_checkbox_pp_link),
                    getString(R.string.intro_legal_checkbox_pp_label),
                    LegalTypes.PRIVACY.getName());
            sendLinkViewedEvent(AnalyticsEvents.PRIVACY_POLICY_VIEWED);
        }

        /**
         * Deny button listener
         *
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
         *
         * @param view View
         */
        public void onClickConfirm(View view) {
            if (binding.introLegalCheckboxEula.isChecked() &&
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
