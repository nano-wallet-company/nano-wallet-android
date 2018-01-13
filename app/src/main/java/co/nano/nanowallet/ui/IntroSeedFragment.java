package co.nano.nanowallet.ui;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import co.nano.nanowallet.R;
import co.nano.nanowallet.databinding.FragmentIntroSeedBinding;

/**
 * The Intro Screen to the app
 */

public class IntroSeedFragment extends Fragment {
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
            colorizeSpannable(binding.introSeedSeed.getText());
        }

        /**
         * Confirm button listener
         *
         * @param view
         */
        public void onClickConfirm(View view) {
            Log.d(TAG, "Confirm Seed Clicked");
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

    /**
     * Colorize a string in the following manner:
     * First 9 characters are blue
     * Last 5 characters are orange
     *
     * @param s
     * @return Colorized Spannable String
     */
    private void colorizeSpannable(Spannable s) {
        s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.dark_sky_blue)), 0, s.length() > 8 ? 9 : s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (s.length() > 59) {
            s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.burnt_yellow)), 59, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }


}
