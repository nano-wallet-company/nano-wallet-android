package co.nano.nanowallet.network.model.request.block;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import co.nano.nanowallet.NanoUtil;
import co.nano.nanowallet.network.model.BlockTypes;
import co.nano.nanowallet.util.NumberUtil;

/**
 * Send BlockItem
 */
@JsonPropertyOrder({
        "type",
        "previous",
        "destination",
        "balance",
        "work",
        "signature"
})
public class SendBlock extends Block {
    @JsonProperty("type")
    private String type;

    @JsonProperty("previous")
    private String previous;

    @JsonProperty("destination")
    private String destination;

    @JsonProperty("balance")
    private String balance;

    @JsonProperty("signature")
    private String signature;

    public SendBlock() {
        this.type = BlockTypes.SEND.toString();
    }

    public SendBlock(String private_key, String previous, String destination, String balance) {
        this.type = BlockTypes.SEND.toString();
        this.previous = previous;
        this.destination = destination;
        this.balance = NumberUtil.getRawAsHex(balance);
        String hash = NanoUtil.computeSendHash(previous, NanoUtil.addressToPublic(destination), this.balance);
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

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
