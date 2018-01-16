package co.nano.nanowallet.ui.intro;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import co.nano.nanowallet.R;
import co.nano.nanowallet.databinding.FragmentIntroSeedBinding;
import co.nano.nanowallet.ui.common.BaseFragment;
import co.nano.nanowallet.ui.common.FragmentControl;
import co.nano.nanowallet.ui.common.FragmentUtility;
import co.nano.nanowallet.ui.common.UIUtil;
import co.nano.nanowallet.ui.home.HomeFragment;

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
            // go to home screen
            if (getActivity() instanceof FragmentControl) {
                ((FragmentControl) getActivity()).getFragmentUtility().replace(
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
            Log.d(TAG, "Camera Clicked");
        }
    }

    /**
     * Update the step count string
     */
    private void updateSteps() {
        binding.setSteps(getString(R.string.intro_seed_steps, currentStep));
    }

}
