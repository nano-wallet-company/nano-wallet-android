package co.nano.nanowallet.network;

/**
 * Available Websocket Actions
 */

public enum BlockTypes {
    SEND("send"), RECEIVE("receive"),
    CHANGE("change"), OPEN("open");

    String blockTypeName;

    BlockTypes(String blockTypeName) {
        this.blockTypeName = blockTypeName;
    }

    @Override
    public String toString() {
        return blockTypeName;
    }
}
