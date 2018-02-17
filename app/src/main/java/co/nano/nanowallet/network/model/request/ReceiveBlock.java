package co.nano.nanowallet.network.model.request;

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
public class ReceiveBlock {
    @SerializedName("type")
    private String type;

    @SerializedName("previous")
    private String previous;

    @SerializedName("source")
    private String source;

    @SerializedName("work")
    private String work;

    @SerializedName("signature")
    private String signature;

    public ReceiveBlock() {
        this.type = BlockTypes.RECEIVE.toString();
    }

    public ReceiveBlock(String private_key, String previous, String source, String work) {
        this.type = BlockTypes.RECEIVE.toString();
        this.previous = previous;
        this.source = source;
        this.work = work;
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
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getWork() {
        return work;
    }

    public void setWork(String work) {
        this.work = work;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
