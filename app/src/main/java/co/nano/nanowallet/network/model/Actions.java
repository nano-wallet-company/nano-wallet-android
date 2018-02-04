package co.nano.nanowallet.network.model;

/**
 * Available Websocket Actions
 */

public enum Actions {
    SUBSCRIBE("account_subscribe"), HISTORY("account_history"),
    PENDING("pending"), PRICE("price_data");

    String actionName;

    Actions(String actionName) {
        this.actionName = actionName;
    }

    @Override
    public String toString() {
        return actionName;
    }
}
