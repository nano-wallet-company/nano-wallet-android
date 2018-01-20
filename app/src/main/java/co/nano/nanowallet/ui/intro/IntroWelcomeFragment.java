package co.nano.nanowallet.ui.intro;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.nano.nanowallet.BuildConfig;
import co.nano.nanowallet.R;
import co.nano.nanowallet.databinding.FragmentIntroWelcomeBinding;
import co.nano.nanowallet.ui.home.HomeFragment;
import co.nano.nanowallet.ui.common.BaseFragment;
import co.nano.nanowallet.ui.common.WindowControl;
import co.nano.nanowallet.ui.common.FragmentUtility;

/**
 * The Intro Screen to the app
 */

public class IntroWelcomeFragment extends BaseFragment {
    private FragmentIntroWelcomeBinding binding;
    public static String TAG = IntroWelcomeFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the view
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_intro_welcome, container, false);
        View view = binding.getRoot();

        setStatusBarWhite(view);
        hideToolbar();

        // bind data to view
        binding.setVersion(getString(R.string.version_display, BuildConfig.VERSION_NAME));
        binding.setHandlers(new ClickHandlers());

        return view;
    }


    public class ClickHandlers {
        public void onClickNewWallet(View view) {
            // TODO: Create new wallet

            // go to home screen
            if (getActivity() instanceof WindowControl) {
                ((WindowControl) getActivity()).getFragmentUtility().replace(
                        new HomeFragment(),
                        FragmentUtility.Animation.ENTER_LEFT_EXIT_RIGHT,
                        FragmentUtility.Animation.ENTER_RIGHT_EXIT_LEFT,
                        IntroSeedFragment.TAG,
                        binding.introWelcomeLogo
                );
            }
        }

        public void onClickHaveWallet(View view) {
            // let user input their existing wallet
            if (getActivity() instanceof WindowControl) {
                ((WindowControl) getActivity()).getFragmentUtility().add(
                        new IntroSeedFragment(),
                        FragmentUtility.Animation.CROSSFADE,
                        FragmentUtility.Animation.CROSSFADE,
                        IntroSeedFragment.TAG,
                        binding.introWelcomeLogo
                );
            }
        }
    }


}
