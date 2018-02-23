package co.nano.nanowallet.model;

import android.support.test.InstrumentationRegistry;
import android.support.test.annotation.UiThreadTest;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;

import javax.inject.Inject;

import co.nano.nanowallet.NanoApplication;
import co.nano.nanowallet.di.activity.ActivityModule;
import co.nano.nanowallet.di.activity.DaggerTestActivityComponent;
import co.nano.nanowallet.di.activity.TestActivityComponent;
import co.nano.nanowallet.util.SharedPreferencesUtil;

/**
 * Test the Nano Utility functions
 */


@RunWith(AndroidJUnit4.class)
public class NanoWalletTest extends InstrumentationTestCase {
    private TestActivityComponent testActivityComponent;

    @Inject
    SharedPreferencesUtil sharedPreferencesUtil;

    @Inject
    NanoWallet nanoWallet;

    public NanoWalletTest() {
    }

    @Before
    @UiThreadTest
    public void setUp() throws Exception {
        // build the activity component
        testActivityComponent = DaggerTestActivityComponent
                .builder()
                .applicationComponent(NanoApplication.getApplication(InstrumentationRegistry.getTargetContext().getApplicationContext()).getApplicationComponent())
                .activityModule(new ActivityModule(InstrumentationRegistry.getTargetContext()))
                .build();

        testActivityComponent.inject(this);
    }

    @Test
    @UiThreadTest
    public void setLocalCurrencyAmount() throws Exception {
        testActivityComponent.inject(nanoWallet);
        nanoWallet.setLocalCurrencyPrice(new BigDecimal("12.5266"));
        for (AvailableCurrency currency : AvailableCurrency.values()) {
            // set each potential currency
            sharedPreferencesUtil.setLocalCurrency(currency);
            nanoWallet.setLocalCurrencyAmount("5,555,555.33");
        }
    }


    @After
    public void tearDown() throws Exception {
    }
}

