package co.nano.nanowallet;

import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import timber.log.Timber;

/**
 * Test the Nano Utility functions
 */


@RunWith(AndroidJUnit4.class)
public class LibSodiumTest extends InstrumentationTestCase {
    private String seed;
    private String privateKey;
    private String publicKey;
    private String address;

    public LibSodiumTest() {
    }

    @Before
    public void setUp() throws Exception {
        seed = "387151e6ea2a42eead77f26b1c0fc4c485df4e78902ada848ff97fc5dce85e81";
        privateKey = "C5469190B25E850CED298E57723258F716A4E1956AC2BC60DA023300476D1212";
        publicKey = "9D473FD0CAD0D43DD79B9FDCAC6FED51EDE7E78279A84142290487CF864B8B8F";
        address = "xrb_39c99zaeon8n9qdsq9ywojqytnhfwzmr6yfaa734k369sy56q4whb1iu45sg";
    }

    @Test
    public void seedToPrivate() throws Exception {
        long lStartTime = System.nanoTime();

        String privateKey = NanoUtil.seedToPrivate(seed);

        long lEndTime = System.nanoTime();
        long output = lEndTime - lStartTime;
        Timber.d("Seed to Private: " + output / 1000000);

        assertEquals(privateKey, this.privateKey);
    }

    @Test
    public void privateToPublic() throws Exception {
        long lStartTime = System.nanoTime();

        String publicKey = NanoUtil.privateToPublic(privateKey);

        long lEndTime = System.nanoTime();
        long output = lEndTime - lStartTime;
        Timber.d("Private to Public: " + output / 1000000);

        assertEquals(publicKey, this.publicKey);
    }

    @Test
    public void publicToAddress() throws Exception {
        long lStartTime = System.nanoTime();

        String address = NanoUtil.publicToAddress(publicKey);

        long lEndTime = System.nanoTime();
        long output = lEndTime - lStartTime;
        Timber.d("Public to Address: " + output / 1000000);

        assertEquals(address, this.address);
    }

    @Test
    public void addressToPublic() throws Exception {
        long lStartTime = System.nanoTime();

        String publicKey = NanoUtil.addressToPublic(this.address);

        long lEndTime = System.nanoTime();
        long output = lEndTime - lStartTime;
        Timber.d("Public to Address: " + output / 1000000);

        assertEquals(publicKey, this.publicKey);
    }

//    @Test
//    public void testSign() throws Exception {
//        String privateKey = "24888E558253E2BE282888874416101C1C42CD702F35F6544A8750768D3347EC";
//        String signature = "F288B486A48E752D6F3902265BE8BCB9AB80B8E2E0DCB1B92EBF96C178C788FCBB1388B26A572958E153ED082E1B88CE349D431CEBCFB5C9662BCFF53F03A10B";
//        String blockHash = NanoUtil.computeSendHash(
//                "F288B486A48E752D6F3902265BE8BCB9AB80B8E2E0DCB1B92EBF96C178C788FCBB1388B26A572958E153ED082E1B88CE349D431CEBCFB5C9662BCFF53F03A10B",
//                "xrb_3954bgqr76boy8k9i8twur5mtt7ra4g8xq9o6ynqxjzo5fxkt5w3mcx4io17",
//                "0000018AE0E06FC3E4B96BDD74000000"
//        );
//
//        String sig = NanoUtil.sign(privateKey, blockHash);
//        assertEquals(sig, signature);
//    }

    @After
    public void tearDown() throws Exception {
    }
}

