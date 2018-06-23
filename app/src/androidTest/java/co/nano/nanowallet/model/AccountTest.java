package co.nano.nanowallet.model;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

public class AccountTest {
    private static ImmutableMap<String, String> testData = ImmutableMap.of(
            "B0311EA55708D6A53C75CDBF88300259C6D018522FE3D4D0A242E431F9E8B6D0", "xrb_3e3j5tkog48pnny9dmfzj1r16pg8t1e76dz5tmac6iq689wyjfpiij4txtdo",
            "0311B25E0D1E1D7724BBA5BD523954F1DBCFC01CB8671D55ED2D32C7549FB252", "xrb_11rjpbh1t9ixgwkdqbfxcawobwgusz13sg595ocytdbkrxcbzekkcqkc3dn1",
            "E89208DD038FBB269987689621D52292AE9C35941A7484756ECCED92A65093BA", "xrb_3t6k35gi95xu6tergt6p69ck76ogmitsa8mnijtpxm9fkcm736xtoncuohr3"
    );

    @Test
    public void humanReadableEncoding() {
        testData.forEach((hexString, accountString) -> {
            Account accountFromHex = Account.parseHexString(hexString);
            Assert.assertEquals(accountString, accountFromHex.toHumanReadable(Prefix.XRB));

            Account accountFromHumanReadable = Account.parseHumanReadable(accountString);
            Assert.assertEquals(hexString, accountFromHumanReadable.toHexString());
        });
    }
}
