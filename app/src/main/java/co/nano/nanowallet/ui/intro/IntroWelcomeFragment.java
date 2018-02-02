package co.nano.nanowallet.ui.intro;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import co.nano.nanowallet.BuildConfig;
import co.nano.nanowallet.NanoUtil;
import co.nano.nanowallet.R;
import co.nano.nanowallet.databinding.FragmentIntroWelcomeBinding;
import co.nano.nanowallet.model.Credentials;
import co.nano.nanowallet.ui.common.ActivityWithComponent;
import co.nano.nanowallet.ui.common.BaseFragment;
import co.nano.nanowallet.ui.common.FragmentUtility;
import co.nano.nanowallet.ui.common.WindowControl;
import io.realm.Realm;

/**
 * The Intro Screen to the app
 */

public class IntroWelcomeFragment extends BaseFragment {
    private FragmentIntroWelcomeBinding binding;
    public static String TAG = IntroWelcomeFragment.class.getSimpleName();

    @Inject
    Realm realm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // init dependency injection
        if (getActivity() instanceof ActivityWithComponent) {
            ((ActivityWithComponent) getActivity()).getActivityComponent().inject(this);
        }
        // inflate the view
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_intro_welcome, container, false);
        View view = binding.getRoot();

        setStatusBarWhite(view);
        hideToolbar();

        // bind data to view
        binding.setVersion(getString(R.string.version_display, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
        binding.setHandlers(new ClickHandlers());

        return view;
    }

    public class ClickHandlers {
        public void onClickNewWallet(View view) {
            createAndStoreWallet();

            // go to home screen
            if (getActivity() instanceof WindowControl) {
                ((WindowControl) getActivity()).getFragmentUtility().replace(
                        IntroNewWalletFragment.newInstance(),
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

    private void createAndStoreWallet() {
        // store wallet seed
        realm.beginTransaction();
        Credentials credentials = realm.createObject(Credentials.class);
        credentials.setSeed(NanoUtil.generateSeed());
        realm.commitTransaction();
        realm.close();
    }


}
