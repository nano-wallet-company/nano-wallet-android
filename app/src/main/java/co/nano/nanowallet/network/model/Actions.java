package co.nano.nanowallet.network.model;

/**
 * Available Websocket Actions
 */

public enum Actions {
    CHECK("account_check"), SUBSCRIBE("account_subscribe"), HISTORY("account_history"),
    PENDING("pending"), PRICE("price_data"), PROCESS("process"),
    WORK("work_generate"), ERROR("error");

    String actionName;

    Actions(String actionName) {
        this.actionName = actionName;
    }

    @Override
    public String toString() {
        return actionName;
    }
}
