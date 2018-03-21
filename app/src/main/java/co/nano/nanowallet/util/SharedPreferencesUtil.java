package co.nano.nanowallet.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.UUID;

import co.nano.nanowallet.model.AvailableCurrency;

/**
 * Shared Preferences utility module
 */
public class SharedPreferencesUtil {
    private static final String LOCAL_CURRENCY = "local_currency";
    private static final String APP_INSTALL_UUID = "app_install_uuid";
    private static final String CONFIRMED_SEED_BACKEDUP = "confirmed_seed_backedup";
    private static final String FROM_NEW_WALLET = "from_new_wallet";

    private final SharedPreferences mPrefs;

    public SharedPreferencesUtil(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    private boolean has(String key) {
        return mPrefs.contains(key);
    }

    private String get(String key, String defValue) {
        return mPrefs.getString(key, defValue);
    }

    private boolean get(String key, boolean defValue) {
        return mPrefs.getBoolean(key, defValue);
    }

    private void set(String key, String value) {
        SharedPreferences.Editor editor = mPrefs.edit();

        if (value != null) {
            editor.putString(key, value);
        } else {
            editor.remove(key);
        }

        editor.apply();
    }

    private void set(String key, boolean value) {
        SharedPreferences.Editor editor = mPrefs.edit();

        editor.putBoolean(key, value);

        editor.apply();
    }

    public boolean hasLocalCurrency() {
        return has(LOCAL_CURRENCY);
    }

    public AvailableCurrency getLocalCurrency() {
        return AvailableCurrency.valueOf(get(LOCAL_CURRENCY, AvailableCurrency.USD.toString()));
    }

    public void setLocalCurrency(AvailableCurrency localCurrency) {
        set(LOCAL_CURRENCY, localCurrency.toString());
    }

    public void clearLocalCurrency() {
        set(LOCAL_CURRENCY, null);
    }

    public boolean hasAppInstallUuid() {
        return has(APP_INSTALL_UUID);
    }

    public String getAppInstallUuid() {
        return get(APP_INSTALL_UUID, UUID.randomUUID().toString());
    }

    public void setAppInstallUuid(String appInstallUuid) {
        set(APP_INSTALL_UUID, appInstallUuid);
    }

    public void clearAppInstallUuid() {
        set(APP_INSTALL_UUID, null);
    }

    public boolean hasFromNewWallet() {
        return has(FROM_NEW_WALLET);
    }

    public Boolean getFromNewWallet() {
        return get(FROM_NEW_WALLET, false);
    }

    public void setFromNewWallet(Boolean fromNewWallet) {
        set(FROM_NEW_WALLET, fromNewWallet);
    }

    public void clearFromNewWallet() {
        set(FROM_NEW_WALLET, false);
    }

    public boolean hasConfirmedSeedBackedUp() {
        return has(CONFIRMED_SEED_BACKEDUP);
    }

    public Boolean getConfirmedSeedBackedUp() {
        return get(CONFIRMED_SEED_BACKEDUP, false);
    }

    public void setConfirmedSeedBackedUp(Boolean confirmedSeedBackedUp) {
        set(CONFIRMED_SEED_BACKEDUP, confirmedSeedBackedUp);
    }

    public void clearConfirmedSeedBackedUp() {
        set(CONFIRMED_SEED_BACKEDUP, false);
    }

    public void clearAll() {
        clearLocalCurrency();
        clearConfirmedSeedBackedUp();
    }

}
