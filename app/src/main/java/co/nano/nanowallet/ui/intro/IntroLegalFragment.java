package co.nano.nanowallet.ui.intro;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import javax.inject.Inject;

import co.nano.nanowallet.R;
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
        FragmentIntroLegalBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_intro_legal, container, false);
        view = binding.getRoot();

        setStatusBarWhite(view);
        hideToolbar();

        // subscribe to bus
        RxBus.get().register(this);

        // bind data to view
        binding.setHandlers(new ClickHandlers());

        accountService.open();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // unregister from bus
        RxBus.get().unregister(this);
    }

    public class ClickHandlers {
        /**
         * Confirm button listener
         *
         * @param view View
         */
        public void onClickConfirm(View view) {
            if (true) {
                //Answers.getInstance().logCustom(new CustomEvent("X Agreement").putCustomAttribute("description", reason.name()));

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
