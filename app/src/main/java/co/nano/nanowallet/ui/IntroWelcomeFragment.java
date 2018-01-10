package co.nano.nanowallet.ui;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the view
        FragmentIntroWelcomeBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_intro_welcome, container, false);
        View view = binding.getRoot();

        // bind data to view
        binding.setVersion(getString(R.string.version_display, BuildConfig.VERSION_NAME));
        
        return view;
    }


}
