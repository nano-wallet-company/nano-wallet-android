package co.nano.nanowallet.model;

import co.nano.nanowallet.util.ExceptionHandler;
import io.realm.RealmObject;

/**
 * Wallet seed used to store in Realm
 */

public class Credentials extends RealmObject {
    private String seed;
    private String privateKey;

    public Credentials() {
    }

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        // validate seed length
        if (seed.length() != 32) {
            ExceptionHandler.handle(new Throwable("Invalid Seed Length"));
            return;
        }
        this.seed = seed;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
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
