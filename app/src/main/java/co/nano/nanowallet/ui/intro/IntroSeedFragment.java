package co.nano.nanowallet.ui.intro;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.github.ajalt.reprint.core.Reprint;
import com.hwangjr.rxbus.annotation.Subscribe;

import javax.inject.Inject;

import co.nano.nanowallet.R;
import co.nano.nanowallet.bus.CreatePin;
import co.nano.nanowallet.bus.RxBus;
import co.nano.nanowallet.databinding.FragmentIntroSeedBinding;
import co.nano.nanowallet.model.Credentials;
import co.nano.nanowallet.network.AccountService;
import co.nano.nanowallet.ui.common.ActivityWithComponent;
import co.nano.nanowallet.ui.common.BaseFragment;
import co.nano.nanowallet.ui.common.FragmentUtility;
import co.nano.nanowallet.ui.common.UIUtil;
import co.nano.nanowallet.ui.common.WindowControl;
import co.nano.nanowallet.ui.scan.ScanActivity;
import co.nano.nanowallet.util.SharedPreferencesUtil;
import io.realm.Realm;

import static android.app.Activity.RESULT_OK;

/**
 * The Intro Screen to the app
 */

public class IntroSeedFragment extends BaseFragment {
    private FragmentIntroSeedBinding binding;
    private int currentStep = 1;
    public static String TAG = IntroSeedFragment.class.getSimpleName();
    private boolean preventTextUpdate = false;

    @Inject
    Realm realm;

    @Inject
    AccountService accountService;

    @Inject
    SharedPreferencesUtil sharedPreferencesUtil;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // init dependency injection
        if (getActivity() instanceof ActivityWithComponent) {
            ((ActivityWithComponent) getActivity()).getActivityComponent().inject(this);
        }

        // inflate the view
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_intro_seed, container, false);
        view = binding.getRoot();

        setStatusBarWhite(view);
        hideToolbar();

        // subscribe to bus
        RxBus.get().register(this);

        // bind data to view
        binding.setSteps(getString(R.string.intro_seed_steps, currentStep));
        binding.setHandlers(new ClickHandlers());

        // set focus changed listener to clear hint on focus (can't be set via binding)
        binding.introSeedSeed.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                ((EditText) v).setHint("");
            } else {
                ((EditText) v).setHint(getString(R.string.intro_seed_seed_hint));
            }
        });

        // keyboard raised listener
        view.getViewTreeObserver().addOnGlobalLayoutListener(() -> {

            Rect r = new Rect();
            view.getWindowVisibleDisplayFrame(r);
            int screenHeight = view.getRootView().getHeight();

            // r.bottom is the position above soft keypad or device button.
            // if keypad is shown, the r.bottom is smaller than that before.
            int keypadHeight = screenHeight - r.bottom;

            if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                binding.introSeedScrollview.smoothScrollTo(0, binding.introSeedScrollview.getMaxScrollAmount());
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // unregister from bus
        RxBus.get().unregister(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == SCAN_RESULT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Bundle res = data.getExtras();
                if (res != null) {
                    // set to scanned value
                    binding.introSeedSeed.setText(res.getString(ScanActivity.QR_CODE_RESULT));
                }
            }
        }
    }

    @Subscribe
    public void receiveCreatePin(CreatePin createPin) {
        realm.beginTransaction();
        Credentials credentials = realm.where(Credentials.class).findFirst();
        if (credentials != null) {
            credentials.setPin(createPin.getPin());
        }
        realm.commitTransaction();
        goToHomeScreen();
    }

    private void removeInvalidCharacters(Editable text) {
        for (int i = 0; i < text.length(); i++) {
            char letter = text.toString().toLowerCase().charAt(i);
            if (!Credentials.VALID_SEED_CHARACTERS.contains(letter)) {
                if (text.length() > 0) {
                    text.delete(i, i + 1);
                }
            }
        }
    }

    public class ClickHandlers {

        /**
         * Listener for styling updates when text changes
         *
         * @param s      Character sequence
         * @param start  Starting character
         * @param before Character that came before
         * @param count  Total character count
         */
        public void onSeedTextChanged(CharSequence s, int start, int before, int count) {
            if (!preventTextUpdate) {
                preventTextUpdate = true;
                // update background color based on entry length
                if (s.length() > 0) {
                    binding.introSeedSeed.setBackgroundResource(R.drawable.bg_seed_input_active);
                } else {
                    binding.introSeedSeed.setBackgroundResource(R.drawable.bg_seed_input);
                }

                // validate input string and update styles if valid
                if (Credentials.isValidSeed(s.toString())) {
                    currentStep = 2;
                    updateSteps();
                    binding.introSeedIconCheck.setVisibility(View.VISIBLE);
                    binding.introSeedButtonConfirm.setEnabled(true);
                } else {
                    currentStep = 1;
                    updateSteps();
                    binding.introSeedIconCheck.setVisibility(View.GONE);
                    binding.introSeedButtonConfirm.setEnabled(false);
                }

                // remove any invalid characters
                removeInvalidCharacters(binding.introSeedSeed.getText());

                // colorize input string
                UIUtil.colorizeSeed(binding.introSeedSeed.getText(), getContext());
            }
        }

        public void afterSeedTextChanged(Editable s) {
            if (preventTextUpdate) {
                preventTextUpdate = false;
                return;
            }
        }

        /**
         * Confirm button listener
         *
         * @param view View
         */
        public void onClickConfirm(View view) {
            createAndStoreCredentials(binding.introSeedSeed.getText().toString());
            accountService.open();

            sharedPreferencesUtil.setConfirmedSeedBackedUp(true);

            if (!Reprint.isHardwarePresent() || !Reprint.hasFingerprintRegistered()) {
                // if no fingerprint software is present or user has not registered
                // a fingerprint show pin screen
                showCreatePinScreen();
            } else {
                // otherwise, go on in
                goToHomeScreen();
            }
        }

        /**
         * Camera click listener
         *
         * @param view View
         */
        public void onClickCamera(View view) {
            startScanActivity(getString(R.string.scan_instruction_label), true);
        }
    }

    private void goToHomeScreen() {
        // go to home screen
        if (getActivity() instanceof WindowControl) {
            ((WindowControl) getActivity()).getFragmentUtility().clearStack();
            ((WindowControl) getActivity()).getFragmentUtility().replace(
                    IntroLegalFragment.newInstance(),
                    FragmentUtility.Animation.ENTER_LEFT_EXIT_RIGHT,
                    FragmentUtility.Animation.ENTER_RIGHT_EXIT_LEFT,
                    IntroLegalFragment.TAG
            );
        }
    }

    private void createAndStoreCredentials(String seed) {
        realm.beginTransaction();
        Credentials credentials = realm.createObject(Credentials.class);
        credentials.setSeed(seed);
        realm.commitTransaction();
    }

    /**
     * Update the step count string
     */
    private void updateSteps() {
        if (isAdded()) {
            binding.setSteps(getString(R.string.intro_seed_steps, currentStep));
        }
    }

}
