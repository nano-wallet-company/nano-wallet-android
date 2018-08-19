package co.nano.nanowallet;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.hwangjr.rxbus.annotation.Subscribe;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;

import co.nano.nanowallet.analytics.AnalyticsEvents;
import co.nano.nanowallet.analytics.AnalyticsService;
import co.nano.nanowallet.bus.HideOverlay;
import co.nano.nanowallet.bus.Logout;
import co.nano.nanowallet.bus.OpenWebView;
import co.nano.nanowallet.bus.RxBus;
import co.nano.nanowallet.bus.SeedCreatedWithAnotherWallet;
import co.nano.nanowallet.bus.ShowOverlay;
import co.nano.nanowallet.di.activity.ActivityComponent;
import co.nano.nanowallet.di.activity.ActivityModule;
import co.nano.nanowallet.di.activity.DaggerActivityComponent;
import co.nano.nanowallet.di.application.ApplicationComponent;
import co.nano.nanowallet.model.Credentials;
import co.nano.nanowallet.model.NanoWallet;
import co.nano.nanowallet.network.AccountService;
import co.nano.nanowallet.model.RFIDViewMessage;
import co.nano.nanowallet.ui.RFID.RFIDUiDebugMessage;
import co.nano.nanowallet.ui.RFID.RFIDUiInvoice;
import co.nano.nanowallet.ui.RFID.RFIDUiStatus;
import co.nano.nanowallet.ui.common.ActivityWithComponent;
import co.nano.nanowallet.ui.common.FragmentUtility;
import co.nano.nanowallet.ui.common.WindowControl;
import co.nano.nanowallet.ui.home.HomeFragment;
import co.nano.nanowallet.ui.intro.IntroLegalFragment;
import co.nano.nanowallet.ui.intro.IntroNewWalletFragment;
import co.nano.nanowallet.ui.intro.IntroWelcomeFragment;
import co.nano.nanowallet.ui.webview.WebViewDialogFragment;
import co.nano.nanowallet.util.SharedPreferencesUtil;
import io.realm.Realm;
import io.realm.RealmResults;

import android.support.v4.app.Fragment;
import co.nano.nanowallet.model.RFIDCardService;

public class MainActivity extends AppCompatActivity implements WindowControl, ActivityWithComponent {
    private FragmentUtility mFragmentUtility;
    private Toolbar mToolbar;
    private TextView mToolbarTitle;
    protected ActivityComponent mActivityComponent;
    private FrameLayout mOverlay;
    public RFIDStaticHandler handler;
    Credentials credentials;
    static HashMap<String, Fragment> existingRFIDFragments = new HashMap<>();
    static String lastRFIDFragmentName = "";
    static String currentRFIDFragmentName;

    @Inject
    Realm realm;

    @Inject
    AccountService accountService;

    @Inject
    NanoWallet nanoWallet;

    @Inject
    SharedPreferencesUtil sharedPreferencesUtil;

    @Inject
    AnalyticsService analyticsService;

    public Credentials getCredentials()
    {
        return credentials;
    }

    private static Fragment getRFIDFragmentForShow(RFIDViewMessage viewMsg) {
        Fragment frgmt = null;

        // Create either fragment of 'new invoice' or 'status of current invoice' or get the old fragment if it has been created before
        if(viewMsg.getIsShowInvoice()) {
            currentRFIDFragmentName = "Invoice";
        }else {
            currentRFIDFragmentName = "Status";
        }
        lastRFIDFragmentName = currentRFIDFragmentName;

        if(existingRFIDFragments.containsKey(currentRFIDFragmentName))
            frgmt = existingRFIDFragments.get(currentRFIDFragmentName);
        else {
            if(currentRFIDFragmentName.equals("Invoice"))
                frgmt = new RFIDUiInvoice();
            else if (currentRFIDFragmentName.equals("Status"))
                frgmt = new RFIDUiStatus();
            existingRFIDFragments.put(currentRFIDFragmentName,frgmt);
        }
        return frgmt;
    }

    /*
     * The RFID stuff happens on a different thread. That thread needs this handler to interact with the MainActivity to change the views etc.
     * Android Studio says this handler needs to be static to prevent a memory leak.
     */
    public static class RFIDStaticHandler extends Handler {
        Fragment previousFrgmt = null;
        static MainActivity mainActivity;
        static void setMainActivity(MainActivity _mainActivity)
        {
            mainActivity = _mainActivity;
        }

        @Override
        public void handleMessage(Message msg)
        {
            if(msg.obj!=null && msg.obj instanceof RFIDUiDebugMessage) {
                RFIDUiDebugMessage debugMsg = (RFIDUiDebugMessage)msg.obj;
                Log.w(debugMsg.getTag(), debugMsg.getMessage());
                return;
            }

            RFIDViewMessage viewMsg = (RFIDViewMessage) msg.obj;
            if(viewMsg.getIsCredentialsUpdate()) {
                mainActivity.credentials = mainActivity.realm.where(Credentials.class).findFirst();
            }
            else
            {
                int fragmentId = viewMsg.getNextViewId();
                if(fragmentId == RFIDViewMessage.RESETUIID) {
                    mainActivity.initUi();
                    return;
                }

                if (fragmentId != -1) {
                    try {
                        Fragment frgmt = null;
                        // Create either invoice or status fragment or find it by id if already exists
                        if(viewMsg.getIsShowInvoice()) {
                            frgmt = new RFIDUiInvoice();
                        } else {
                            frgmt = new RFIDUiStatus();
                        }

                        if (frgmt != null) {
                            FragmentTransaction transaction = mainActivity.getSupportFragmentManager().beginTransaction();

                            // Replace whatever is in the fragment_container view with this fragment,
                            // and add the transaction to the back stack
                            if(previousFrgmt!=null)
                                transaction.remove(previousFrgmt);
                            transaction.replace(R.id.container, frgmt);
                            transaction.addToBackStack(null);
                            // Commit the transaction
                            transaction.commit();

                            if (fragmentId == R.id.fragment_rfid_invoice) {
                                ((RFIDUiInvoice)frgmt).setMainActivity(mainActivity);
                                ((RFIDUiInvoice)frgmt).setRFIDInvoiceData(viewMsg);
                            }
                            else if(fragmentId == R.id.fragment_rfid_status) {
                                ((RFIDUiStatus)frgmt).setMainActivity(mainActivity);
                                ((RFIDUiStatus)frgmt).setRFIDStatusData(viewMsg);
                            }
                            previousFrgmt = frgmt;
                        }
                    } catch (Exception ex) {
                        // Should never happen but ...
                        Log.w("Fragment ID", "ID not found", ex);
                        mainActivity.displayRFIDErrorMessage();
                    }
                }
            }
        }
    }

    public void displayRFIDErrorMessage()
    {
        String[] params = new String[2];
        params[0] = "Oops!";
        params[1] = "Something went wrong in the RFID process. Sorry! Please try again.";
        RFIDViewMessage viewMsg = new RFIDViewMessage(false, R.id.fragment_rfid_status, params, false);
        Message msg = new Message();
        msg.obj = viewMsg;
        handler.handleMessage(msg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RFIDCardService.setMainActivity(this);
        RFIDStaticHandler.setMainActivity(this);
        handler = new RFIDStaticHandler();

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

        // set unique uuid (per app install)
        if (!sharedPreferencesUtil.hasAppInstallUuid()) {
            sharedPreferencesUtil.setAppInstallUuid(UUID.randomUUID().toString());
        }

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

        // get overlay
        mOverlay = findViewById(R.id.overlay);

        // set up toolbar
        mToolbar = findViewById(R.id.toolbar);
        mToolbarTitle = findViewById(R.id.toolbar_title);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // get wallet seed if it exists
        credentials = realm.where(Credentials.class).findFirst();

        // initialize analytics
        analyticsService.start();
//        if (credentials != null && credentials.getHasAgreedToTracking()) {
//            analyticsService.start();
//        } else if (credentials != null && !credentials.getHasAnsweredAnalyticsQuestion()) {
//            analyticsService.startAnswersOnly(); // for legal
//        } else {
//            analyticsService.stop();
//        }

        if (credentials == null)
        {
            // if we don't have a wallet, start the intro
            mFragmentUtility.clearStack();
            mFragmentUtility.replace(new IntroWelcomeFragment());
        }
        else if (credentials.getHasCompletedLegalAgreements())
        {
            mFragmentUtility.clearStack();
            if (sharedPreferencesUtil.getConfirmedSeedBackedUp()) {
                // go to home screen
                mFragmentUtility.replace(HomeFragment.newInstance());
            } else {
                // go to intro new wallet
                mFragmentUtility.replace(IntroNewWalletFragment.newInstance());
            }
        } else {
            mFragmentUtility.clearStack();
            mFragmentUtility.replace(IntroLegalFragment.newInstance());
        }
    }

    @Subscribe
    public void logOut(Logout logout) {
        analyticsService.track(AnalyticsEvents.LOG_OUT);

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
        sharedPreferencesUtil.setFromNewWallet(false);

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

    @Subscribe
    public void showOverlay(ShowOverlay showOverlay) {
        mOverlay.setVisibility(View.VISIBLE);
        mOverlay.setOnClickListener(view -> {
        });
    }

    @Subscribe
    public void hideOverlay(HideOverlay hideOverlay) {
        mOverlay.setVisibility(View.GONE);
    }

    @Subscribe
    public void seedCreatedWithAnotherWallet(SeedCreatedWithAnotherWallet seedCreatedWithAnotherWallet) {
        realm.executeTransaction(realm -> {
            Credentials credentials = realm.where(Credentials.class).findFirst();
            if (credentials != null) {
                credentials.setSeedIsSecure(true);
            }
        });
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
