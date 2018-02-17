package co.nano.nanowallet;

import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libsodium.jni.NaCl;
import org.libsodium.jni.Sodium;

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

    @Test
    public void testSign() throws Exception {
        String signature = "CE9B22F06E01EAE0D003E87A513F389F2B6B5165F1A309E0616A59F6CC385255C6433C9DDBA1208D3BF9041C580050CDD86B13CCA197455C0DF83FDE624F7600";
        String blockHash = NanoUtil.computeSendHash(
                "8434FDE3B9C7535A72048B7CA111DF6E1759C73AD7E16FD6D9C562B7C5D90581",
                NanoUtil.addressToPublic("xrb_3ugkt5gexef4ffotr839bwfrfux6pp1g7k8cbtk8ocnq99yc3pfdn11cr1ft"),
                "00000000000000000000000000000000"
        );
        byte[] blockhash_b = NanoUtil.hexToBytes(blockHash);
        String sig = NanoUtil.sign(NanoUtil.seedToPrivate("133637C920D51BFD095086128A2DFBF33AA173F0641A8DEE2F6826F87A23760B"), blockHash);

        Sodium sodium = NaCl.sodium();
//        assertEquals(0, Sodium.crypto_sign_verify_detached(
//                NanoUtil.hexToBytes(sig),
//                blockhash_b,
//                blockhash_b.length,
//                NanoUtil.hexToBytes(
//                        NanoUtil.privateToPublic(
//                                NanoUtil.seedToPrivate("133637C920D51BFD095086128A2DFBF33AA173F0641A8DEE2F6826F87A23760B"))
//                )
//        ));

        assertEquals(signature, sig);
    }

    @After
    public void tearDown() throws Exception {
    }
}

