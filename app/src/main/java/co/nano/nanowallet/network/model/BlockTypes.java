package co.nano.nanowallet.network.model;

/**
 * Available Websocket Actions
 */

public enum BlockTypes {
    SEND("send"), RECEIVE("receive"),
    CHANGE("change"), OPEN("open"),
    STATE("state");

    String blockTypeName;

    BlockTypes(String blockTypeName) {
        this.blockTypeName = blockTypeName;
    }

    @Override
    public String toString() {
        return blockTypeName;
    }
}
