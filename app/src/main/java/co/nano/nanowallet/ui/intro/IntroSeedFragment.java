package co.nano.nanowallet.ui.intro;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import co.nano.nanowallet.R;
import co.nano.nanowallet.databinding.FragmentIntroSeedBinding;
import co.nano.nanowallet.ui.common.BaseFragment;
import co.nano.nanowallet.ui.common.FragmentUtility;
import co.nano.nanowallet.ui.common.UIUtil;
import co.nano.nanowallet.ui.common.WindowControl;
import co.nano.nanowallet.ui.home.HomeFragment;
import co.nano.nanowallet.ui.scan.ScanActivity;

import static android.app.Activity.RESULT_OK;

/**
 * The Intro Screen to the app
 */

public class IntroSeedFragment extends BaseFragment {
    private FragmentIntroSeedBinding binding;
    private int currentStep = 1;
    public static String TAG = IntroSeedFragment.class.getSimpleName();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the view
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_intro_seed, container, false);
        View view = binding.getRoot();

        setStatusBarWhite(view);
        hideToolbar();

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

        return view;
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

    public class ClickHandlers {

        /**
         * Listener for styling updates when text changes
         *
         * @param s
         * @param start
         * @param before
         * @param count
         */
        public void onSeedTextChanged(CharSequence s, int start, int before, int count) {
            // update background color of edittext and step we are on
            if (s.length() > 0) {
                currentStep = 2;
                updateSteps();
                binding.introSeedSeed.setBackgroundResource(R.drawable.bg_seed_input_active);
            } else {
                currentStep = 1;
                updateSteps();
                binding.introSeedSeed.setBackgroundResource(R.drawable.bg_seed_input);
            }

            // colorize input string
            UIUtil.colorizeSpannable(binding.introSeedSeed.getText(), getContext());
        }

        /**
         * Confirm button listener
         *
         * @param view
         */
        public void onClickConfirm(View view) {
            // TODO: Create wallet from seed

            // go to home screen
            if (getActivity() instanceof WindowControl) {
                ((WindowControl) getActivity()).getFragmentUtility().replace(
                        new HomeFragment(),
                        FragmentUtility.Animation.ENTER_LEFT_EXIT_RIGHT,
                        FragmentUtility.Animation.ENTER_RIGHT_EXIT_LEFT,
                        IntroSeedFragment.TAG
                );
            }
        }

        /**
         * Camera click listener
         *
         * @param view
         */
        public void onClickCamera(View view) {
            startScanActivity();
        }
    }

    /**
     * Update the step count string
     */
    private void updateSteps() {
        binding.setSteps(getString(R.string.intro_seed_steps, currentStep));
    }

}
