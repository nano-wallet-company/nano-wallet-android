package co.nano.nanowallet.network.model.request.block;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.gson.annotations.SerializedName;

import co.nano.nanowallet.NanoUtil;
import co.nano.nanowallet.network.model.BlockTypes;

/**
 * Subscribe to websocket server for updates regarding the specified account.
 * First action to take when connecting when app opens or reconnects, IF a wallet already exists
 */
@JsonPropertyOrder({
        "type",
        "source",
        "previous",
        "work",
        "signature"
})
public class ReceiveBlock extends Block {
    @SerializedName("type")
    private String type;

    @SerializedName("previous")
    private String previous;

    @SerializedName("source")
    private String source;

    @SerializedName("signature")
    private String signature;

    private String private_key;

    public ReceiveBlock() {
        this.type = BlockTypes.RECEIVE.toString();
    }

    public ReceiveBlock(String private_key, String previous, String source) {
        this.private_key = private_key;
        this.type = BlockTypes.RECEIVE.toString();
        this.previous = previous;
        this.source = source;
        String hash = NanoUtil.computeReceiveHash(previous, source);
        this.signature = NanoUtil.sign(private_key, hash);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
        String hash = NanoUtil.computeReceiveHash(previous, this.source);
        this.signature = NanoUtil.sign(this.private_key, hash);
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
