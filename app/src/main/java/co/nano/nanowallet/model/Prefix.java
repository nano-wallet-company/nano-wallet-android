package co.nano.nanowallet.model;

public enum Prefix {
    XRB("xrb"), NANO("nano");

    public static final Prefix DEFAULT = XRB;

    private final String accountString;

    Prefix(String accountString) {
        this.accountString = accountString;
    }

    public String getAccountString() {
        return accountString;
    }
}
