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

import com.hwangjr.rxbus.annotation.Subscribe;

import javax.inject.Inject;

import co.nano.nanowallet.bus.Logout;
import co.nano.nanowallet.bus.RxBus;
import co.nano.nanowallet.di.activity.ActivityComponent;
import co.nano.nanowallet.di.activity.DaggerActivityComponent;
import co.nano.nanowallet.model.Credentials;
import co.nano.nanowallet.ui.common.ActivityWithComponent;
import co.nano.nanowallet.ui.common.FragmentUtility;
import co.nano.nanowallet.ui.common.WindowControl;
import co.nano.nanowallet.ui.home.HomeFragment;
import co.nano.nanowallet.ui.intro.IntroWelcomeFragment;
import co.nano.nanowallet.websocket.RxWebSocket;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements WindowControl, ActivityWithComponent {
    private FragmentUtility mFragmentUtility;
    private Toolbar mToolbar;
    private TextView mToolbarTitle;
    protected ActivityComponent mActivityComponent;
    private RxWebSocket rxWebSocket;

    @Inject
    Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // build the activity component
        mActivityComponent = DaggerActivityComponent
                .builder()
                .applicationComponent(NanoApplication.getApplication(this).getApplicationComponent())
                .build();

        // perform dagger injections
        mActivityComponent.inject(this);

        // subscribe to bus
        RxBus.get().register(this);

        initUi();
    }

    @Override
    protected void onDestroy() {
        // unregister from bus
        RxBus.get().unregister(this);
        realm.close();

        super.onDestroy();
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
        Credentials credentials = null;
        credentials = realm.where(Credentials.class).findFirst();

        if (credentials == null) {
            // if we dont have a wallet, start the intro
            mFragmentUtility.replace(new IntroWelcomeFragment());
        } else {
            // if we do have a wallet, load and configure
            mFragmentUtility.replace(new HomeFragment());
        }

        connectWebSocket();
    }

    private void connectWebSocket() {
        rxWebSocket = new RxWebSocket("wss://raicast.lightrai.com:443");

        rxWebSocket.onOpen()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(socketOpenEvent -> {
                    Timber.i("Opened");
                    rxWebSocket.sendMessage("{\"action\":\"account_subscribe\",\"account\":\"xrb_3t6k35gi95xu6tergt6p69ck76ogmitsa8mnijtpxm9fkcm736xtoncuohr3\"}");
                    rxWebSocket.sendMessage("{\"action\":\"block_count\"}");
                    rxWebSocket.sendMessage("{\"action\":\"price_data\",\"currency\":\"usd\"}");
                }, Throwable::printStackTrace);

        rxWebSocket.onClosed()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(socketClosedEvent -> {
                    Timber.i("Closed: " + socketClosedEvent.getReason());
                }, Throwable::printStackTrace);

        rxWebSocket.onClosing()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(socketClosingEvent -> {
                    Timber.i("Closing: " + socketClosingEvent.getReason());
                }, Throwable::printStackTrace);

        rxWebSocket.onTextMessage()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(socketMessageEvent -> {
                    Timber.i(socketMessageEvent.getText());
                }, Throwable::printStackTrace);

        rxWebSocket.onBinaryMessage()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(socketMessageEvent -> {
                    Timber.i(socketMessageEvent.getText());
                }, Throwable::printStackTrace);

        rxWebSocket.onFailure()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(socketFailureEvent -> {
                    Timber.i("Error: " + socketFailureEvent.getException().getMessage());
                }, Throwable::printStackTrace);

        rxWebSocket.connect();
    }

    @Subscribe
    public void logOut(Logout logout) {
        // delete user seed data before logging out
        final RealmResults<Credentials> results = realm.where(Credentials.class).findAll();
        realm.executeTransaction(realm1 -> {
            results.deleteAllFromRealm();
        });
        realm.close();

        // go back to welcome fragment
        getFragmentUtility().replace(new IntroWelcomeFragment(), FragmentUtility.Animation.CROSSFADE);
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
     * @param visible
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
     * @param title
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
     * @param drawable
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
                    .build();
        }
        return mActivityComponent;
    }
}
