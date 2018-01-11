package co.nano.nanowallet.ui;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.nano.nanowallet.BuildConfig;
import co.nano.nanowallet.R;
import co.nano.nanowallet.databinding.FragmentIntroWelcomeBinding;

/**
 * The Intro Screen to the app
 */

public class IntroWelcomeFragment extends Fragment {
    public String TAG = IntroWelcomeFragment.class.getSimpleName();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the view
        FragmentIntroWelcomeBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_intro_welcome, container, false);
        View view = binding.getRoot();

        // bind data to view
        binding.setVersion(getString(R.string.version_display, BuildConfig.VERSION_NAME));
        binding.setHandlers(new ClickHandlers());

        return view;
    }


    public class ClickHandlers {
        public void onClickNewWallet(View view) {
            Log.d(TAG, "New Wallet");
        }

        public void onClickHaveWallet(View view) {
            Log.d(TAG, "Have Wallet");
        }
    }


}
