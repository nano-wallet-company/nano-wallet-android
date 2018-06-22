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
    private String uuid;
    private String pin;
    private Boolean hasCompletedLegalAgreements;
    private Boolean hasAgreedToTracking;
    private Boolean hasAnsweredAnalyticsQuestion;
    private Boolean seedIsSecure;
    private String newlyGeneratedSeed;
    private Boolean hasSentToNewSeed;

    public static final List<Character> VALID_SEED_CHARACTERS = Arrays.asList('a', 'b', 'c', 'd', 'e', 'f', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9');

    public Credentials() {
    }

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        // validate seed length
        if (!isValidSeed(seed)) {
            ExceptionHandler.handle(new Exception("Invalid Seed: " + seed));
            return;
        }

        this.seedIsSecure = true;
        this.seed = seed;
        this.privateKey = NanoUtil.seedToPrivate(seed);
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public Boolean getHasCompletedLegalAgreements() {
        return hasCompletedLegalAgreements == null ? false : hasCompletedLegalAgreements;
    }

    public void setHasCompletedLegalAgreements(Boolean hasCompletedLegalAgreements) {
        this.hasCompletedLegalAgreements = hasCompletedLegalAgreements;
    }

    public Boolean getHasAgreedToTracking() {
        return hasAgreedToTracking == null ? false : hasAgreedToTracking;
    }

    public void setHasAgreedToTracking(Boolean hasAgreedToTracking) {
        this.hasAgreedToTracking = hasAgreedToTracking;
    }

    public Boolean getHasAnsweredAnalyticsQuestion() {
        return hasAnsweredAnalyticsQuestion == null ? false : hasAnsweredAnalyticsQuestion;
    }

    public void setHasAnsweredAnalyticsQuestion(Boolean hasAnsweredAnalyticsQuestion) {
        this.hasAnsweredAnalyticsQuestion = hasAnsweredAnalyticsQuestion;
    }

    public Boolean getSeedIsSecure() {
        return seedIsSecure == null ? false : seedIsSecure;
    }

    public void setSeedIsSecure(Boolean seedIsSecure) {
        this.seedIsSecure = seedIsSecure;
    }

    public String getNewlyGeneratedSeed() {
        return newlyGeneratedSeed;
    }

    public void setNewlyGeneratedSeed(String newlyGeneratedSeed) {
        this.newlyGeneratedSeed = newlyGeneratedSeed;
    }

    public Boolean getHasSentToNewSeed() {
        return hasSentToNewSeed == null ? false : hasSentToNewSeed;
    }

    public void setHasSentToNewSeed(Boolean hasSentToNewSeed) {
        this.hasSentToNewSeed = hasSentToNewSeed;
    }

    // Generated fields

    public String getPublicKey() {
        if (this.privateKey != null) {
            return NanoUtil.privateToPublic(this.privateKey);
        } else {
            return null;
        }
    }

    public Address getAddress() {
        String publicKey = getPublicKey();
        if (publicKey != null) {
            return new Address(NanoUtil.publicToAddress(publicKey));
        } else {
            return null;
        }
    }

    public String getAddressString() {
        String publicKey = getPublicKey();
        if (publicKey != null) {
            return NanoUtil.publicToAddress(publicKey);
        } else {
            return null;
        }
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
        if (privateKey != null ? !privateKey.equals(that.privateKey) : that.privateKey != null)
            return false;
        if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) return false;
        if (pin != null ? !pin.equals(that.pin) : that.pin != null) return false;
        if (hasCompletedLegalAgreements != null ? !hasCompletedLegalAgreements.equals(that.hasCompletedLegalAgreements) : that.hasCompletedLegalAgreements != null) return false;
        if (seedIsSecure != null ? !seedIsSecure.equals(that.seedIsSecure) : that.seedIsSecure != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = seed != null ? seed.hashCode() : 0;
        result = 31 * result + (privateKey != null ? privateKey.hashCode() : 0);
        result = 31 * result + (uuid != null ? uuid.hashCode() : 0);
        result = 31 * result + (pin != null ? pin.hashCode() : 0);
        result = 31 * result + (hasCompletedLegalAgreements != null ? hasCompletedLegalAgreements.hashCode() : 0);
        result = 31 * result + (seedIsSecure != null ? seedIsSecure.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Credentials{" +
                "seed='" + seed + '\'' +
                ", privateKey='" + privateKey + '\'' +
                ", uuid='" + uuid + '\'' +
                ", pin='" + pin + '\'' +
                ", hasCompletedLegalAgreements=" + hasCompletedLegalAgreements +
                ", seedIsSecure=" + seedIsSecure +
                '}';
    }
}
