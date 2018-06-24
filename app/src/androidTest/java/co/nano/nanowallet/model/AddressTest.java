package co.nano.nanowallet.model;

import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;
import co.nano.nanowallet.di.activity.TestActivityComponent;
import co.nano.nanowallet.util.SharedPreferencesUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

/**
 * Test the Nano Utility functions
 */


@RunWith(AndroidJUnit4.class)
public class AddressTest extends InstrumentationTestCase {
    private TestActivityComponent testActivityComponent;

    @Inject
    SharedPreferencesUtil sharedPreferencesUtil;

    @Inject
    NanoWallet nanoWallet;

    public AddressTest() {
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testBasicAddress() throws Exception {
        String addressString = "xrb_3wm37qz19zhei7nzscjcopbrbnnachs4p1gnwo5oroi3qonw6inwgoeuufdp";
        Address address = Address.fromAddressString(addressString);
        assertEquals("xrb_3wm37qz19zhei7nzscjcopbrbnnachs4p1gnwo5oroi3qonw6inwgoeuufdp", address.getAccount().toHumanReadable(Prefix.XRB));
        assertNull(address.getRawAmount());
    }

    @Test
    public void testAddressParsing() throws Exception {
        String addressString = "xrb:xrb_3wm37qz19zhei7nzscjcopbrbnnachs4p1gnwo5oroi3qonw6inwgoeuufdp?amount=10&label=Developers%20Fund&message=Donate%20Now";
        Address address = Address.fromAddressString(addressString);
        assertEquals("xrb_3wm37qz19zhei7nzscjcopbrbnnachs4p1gnwo5oroi3qonw6inwgoeuufdp", address.getAccount().toHumanReadable(Prefix.XRB));
        assertNotNull(address.getRawAmount());
        assertEquals("10", address.getRawAmount().toRawAmountString());
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
