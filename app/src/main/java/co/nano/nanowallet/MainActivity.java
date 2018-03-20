package co.nano.nanowallet;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.hwangjr.rxbus.annotation.Subscribe;

import javax.inject.Inject;

import co.nano.nanowallet.bus.Logout;
import co.nano.nanowallet.bus.OpenWebView;
import co.nano.nanowallet.bus.RxBus;
import co.nano.nanowallet.di.activity.ActivityComponent;
import co.nano.nanowallet.di.activity.ActivityModule;
import co.nano.nanowallet.di.activity.DaggerActivityComponent;
import co.nano.nanowallet.di.application.ApplicationComponent;
import co.nano.nanowallet.model.Credentials;
import co.nano.nanowallet.model.NanoWallet;
import co.nano.nanowallet.network.AccountService;
import co.nano.nanowallet.ui.common.ActivityWithComponent;
import co.nano.nanowallet.ui.common.FragmentUtility;
import co.nano.nanowallet.ui.common.WindowControl;
import co.nano.nanowallet.ui.home.HomeFragment;
import co.nano.nanowallet.ui.intro.IntroNewWalletFragment;
import co.nano.nanowallet.ui.intro.IntroWelcomeFragment;
import co.nano.nanowallet.ui.webview.WebViewDialogFragment;
import co.nano.nanowallet.util.SharedPreferencesUtil;
import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity implements WindowControl, ActivityWithComponent {
    private FragmentUtility mFragmentUtility;
    private Toolbar mToolbar;
    private TextView mToolbarTitle;
    protected ActivityComponent mActivityComponent;


    @Inject
    Realm realm;

    @Inject
    AccountService accountService;

    @Inject
    NanoWallet nanoWallet;

    @Inject
    SharedPreferencesUtil sharedPreferencesUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        disableScreenCapture();

        // build the activity component
        mActivityComponent = DaggerActivityComponent
                .builder()
                .applicationComponent(NanoApplication.getApplication(this).getApplicationComponent())
                .activityModule(new ActivityModule(this))
                .build();

        // perform dagger injections
        mActivityComponent.inject(this);

        // subscribe to bus
        RxBus.get().register(this);

        initUi();
    }

    private void disableScreenCapture() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // stop websocket on pause
        if (accountService != null) {
            accountService.close();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // start websocket on resume
        if (accountService != null && realm != null && !realm.isClosed() && realm.where(Credentials.class).findFirst() != null) {
            accountService.open();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // unregister from bus
        RxBus.get().unregister(this);

        // close realm connection
        if (realm != null) {
            realm.close();
            realm = null;
        }

        // close wallet so app can clean up
        if (nanoWallet != null) {
            nanoWallet.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void initUi() {
        // set main content view
        setContentView(R.layout.activity_main);

        // create fragment utility instance
        mFragmentUtility = new FragmentUtility(getSupportFragmentManager());
        mFragmentUtility.setContainerViewId(R.id.container);

        // set up toolbar
        mToolbar = findViewById(R.id.toolbar);
        mToolbarTitle = findViewById(R.id.toolbar_title);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // get wallet seed if it exists
        Credentials credentials = realm.where(Credentials.class).findFirst();

        if (credentials == null) {
            // if we don't have a wallet, start the intro
            mFragmentUtility.clearStack();
            mFragmentUtility.replace(new IntroWelcomeFragment());
        } else {
            if (sharedPreferencesUtil.getConfirmedSeedBackedUp()) {
                // go to home screen
                mFragmentUtility.clearStack();
                mFragmentUtility.replace(new HomeFragment());
            } else {
                // go to
                mFragmentUtility.clearStack();
                mFragmentUtility.replace(new IntroNewWalletFragment());
            }
        }
    }

    @Subscribe
    public void logOut(Logout logout) {
        Answers.getInstance().logCustom(new CustomEvent("User Logged Out"));

        // delete user seed data before logging out
        final RealmResults<Credentials> results = realm.where(Credentials.class).findAll();
        realm.executeTransaction(realm1 -> results.deleteAllFromRealm());

        // stop the websocket
        accountService.close();

        // clear wallet
        nanoWallet.clear();

        // null out component
        mActivityComponent = null;

        sharedPreferencesUtil.setConfirmedSeedBackedUp(false);

        // go to the welcome fragment
        getFragmentUtility().clearStack();
        getFragmentUtility().replace(new IntroWelcomeFragment(), FragmentUtility.Animation.CROSSFADE);
    }

    @Subscribe
    public void openWebView(OpenWebView openWebView) {
        WebViewDialogFragment
                .newInstance(openWebView.getUrl(), openWebView.getTitle() != null ? openWebView.getTitle() : "")
                .show(getFragmentUtility().getFragmentManager(), WebViewDialogFragment.TAG);
    }

    @Override
    public FragmentUtility getFragmentUtility() {
        return mFragmentUtility;
    }

    /**
     * Set the status bar to a particular color
     *
     * @param color color resource id
     */
    @Override
    public void setStatusBarColor(int color) {
        // we can only set it 5.x and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, color));
        }
    }

    @Override
    public void setDarkIcons(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
        }
    }

    /**
     * Set visibility of app toolbar
     *
     * @param visible true if toolbar should be visible
     */
    @Override
    public void setToolbarVisible(boolean visible) {
        if (mToolbar != null) {
            mToolbar.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Set title of the app toolbar
     *
     * @param title Title of the toolbar
     */
    @Override
    public void setTitle(String title) {
        if (mToolbarTitle != null) {
            mToolbarTitle.setText(title);
        }
        setToolbarVisible(true);
    }

    /**
     * Set title drawable of app toolbar
     *
     * @param drawable Drawable to show next to title on the toolbar
     */
    @Override
    public void setTitleDrawable(int drawable) {
        if (mToolbarTitle != null) {
            mToolbarTitle.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0);
        }
        setToolbarVisible(true);
    }

    @Override
    public void setBackEnabled(boolean enabled) {
        if (mToolbar != null) {
            if (enabled) {
                mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
                mToolbar.setNavigationOnClickListener(view -> mFragmentUtility.pop());
            } else {
                mToolbar.setNavigationIcon(null);
                mToolbar.setNavigationOnClickListener(null);
            }
        }
    }

    @Override
    public ActivityComponent getActivityComponent() {
        if (mActivityComponent == null) {
            // build the activity component
            mActivityComponent = DaggerActivityComponent
                    .builder()
                    .applicationComponent(NanoApplication.getApplication(this).getApplicationComponent())
                    .activityModule(new ActivityModule(this))
                    .build();
        }
        return mActivityComponent;
    }

    @Override
    public ApplicationComponent getApplicationComponent() {
        return NanoApplication.getApplication(this).getApplicationComponent();
    }
}
