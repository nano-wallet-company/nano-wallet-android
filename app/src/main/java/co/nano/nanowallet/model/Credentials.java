package co.nano.nanowallet.model;

import java.util.Arrays;
import java.util.List;

import co.nano.nanowallet.NanoUtil;
import co.nano.nanowallet.util.ExceptionHandler;
import io.realm.RealmObject;

/**
 * Wallet seed used to store in Realm
 */

public class Credentials extends RealmObject {
    private String seed;
    private String privateKey;
    private String publicKey;

    public static final List<Character> VALID_SEED_CHARACTERS = Arrays.asList('a','b','c','d','e','f','0','1','2','3','4','5','6','7','8','9');

    public Credentials() {
    }

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        // validate seed length
        if (!isValidSeed(seed)) {
            ExceptionHandler.handle(new Throwable("Invalid Seed: " + seed));
            return;
        }

        this.seed = seed;
        this.privateKey = NanoUtil.seedToPrivateNaCl(seed);
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public Address getAddress() {
        return new Address(NanoUtil.privateToPublicNaCl(this.privateKey));
    }

    public String getAddressString() {
        if (this.publicKey == null) {
            return "";
        }
        return NanoUtil.publicToAddress(publicKey);
    }

    public static boolean isValidSeed(String seed) {
        if (seed.length() != 64) {
            return false;
        }
        boolean isMatch = true;
        for (int i = 0; i < seed.length() && isMatch; i++) {
            char letter = seed.toLowerCase().charAt(i);
            if (!VALID_SEED_CHARACTERS.contains(letter)) {
                isMatch = false;
            }
        }
        return isMatch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Credentials that = (Credentials) o;

        if (seed != null ? !seed.equals(that.seed) : that.seed != null) return false;
        return privateKey != null ? privateKey.equals(that.privateKey) : that.privateKey == null;
    }

    @Override
    public int hashCode() {
        int result = seed != null ? seed.hashCode() : 0;
        result = 31 * result + (privateKey != null ? privateKey.hashCode() : 0);
        return result;
    }
}
