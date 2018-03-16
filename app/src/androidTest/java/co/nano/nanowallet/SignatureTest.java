package co.nano.nanowallet;

import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libsodium.jni.NaCl;
import org.libsodium.jni.Sodium;

/**
 * Test the Nano Utility functions
 */


@RunWith(AndroidJUnit4.class)
public class SignatureTest extends InstrumentationTestCase {
    public SignatureTest() {
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void derivePublicFromPrivate() throws Exception {
        // random key pair
        Sodium sodium = NaCl.sodium();
        byte[] pk = new byte[Sodium.crypto_sign_publickeybytes()];
        byte[] sk = new byte[Sodium.crypto_sign_secretkeybytes()];
        Sodium.crypto_sign_ed25519_keypair(pk, sk);

        assertEquals(NanoUtil.bytesToHex(pk), NanoUtil.privateToPublic(NanoUtil.bytesToHex(sk)));

        // random key pair from raiblocks official node
        String priv = "49FF617E9074857402411B346D92174572EB5DE02CC9469C22E9681D8565E6D5";
        String pub = "6C32F3E6ED921D2D98A3573B665FE7F8A35D510186AA9F1B365D283BBAA93DFB";
        String account = "xrb_1u3kyhmgu6ix7pec8osueshyhy75doai53ocmwfmeqba9gxckhhurc3cokfo";

        assertEquals(pub, NanoUtil.privateToPublic(priv));
    }

    @Test
    public void signingABlock() throws Exception {
        String yourPrivateKey = "1F7B5B5D966DCF95DD401D504A088B81256C09D1196697A2DBF79BCFA4171E2B";
        String oneOfYourBlocksHashes = "E23C078FA2C60AE5F64FC0C432F21650FEC582A4174D415F2CAAEC2457A36844";
        String theSignatureOnThatBlock = "D95854A073F74B02B8BF35B89098A297BEB0C8EED56AE4BBB0BD60A3E2BBA236734CD57AAD02C1C6769369BA9DB2917A11F42F53537A72AD226B7C386A19BD02";

        assertEquals(theSignatureOnThatBlock, NanoUtil.sign(yourPrivateKey, oneOfYourBlocksHashes));
    }

    @Test
    public void verifyABlockSignature() throws Exception {
        String blockHash = "B8C51B22BFE48B358C437BE5ACE3F203BD5938A5231F4F1C177488E879317B5E";
        String account = "xrb_39ymww61tksoddjh1e43mprw5r8uu1318it9z3agm7e6f96kg4ndqg9tuds4";
        String pubKey = NanoUtil.addressToPublic(account);
        String signature = "0E5DC6C6CDBC96A9885B1DDB0E782BC04D9B2DCB107FDD4B8A3027695A3B3947BE8E6F413190AD304B8BC5129A50ECFB8DB918FAA3EEE2856C4449A329325E0A";

        //assertEquals(theSignatureOnThatBlock, NanoUtil.sign(yourPrivateKey, oneOfYourBlocksHashes));
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
}

