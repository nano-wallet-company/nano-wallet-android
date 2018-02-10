package co.nano.nanowallet.network.model.request;

import com.google.gson.annotations.SerializedName;

import java.math.BigInteger;

import co.nano.nanowallet.NanoUtil;
import co.nano.nanowallet.network.model.BaseNetworkModel;
import co.nano.nanowallet.network.model.BlockTypes;

/**
 * Send Block
 */

public class SendBlock extends BaseNetworkModel {
    @SerializedName("type")
    private String type;

    @SerializedName("previous")
    private String previous;

    @SerializedName("destination")
    private String destination;

    @SerializedName("balance")
    private String balance;

    @SerializedName("work")
    private String work;

    @SerializedName("signature")
    private String signature;

    public SendBlock() {
        this.type = BlockTypes.SEND.toString();
    }

    public SendBlock(String private_key, String public_key, String previous, String destination, String balance, String work) {
        this.type = BlockTypes.SEND.toString();
        this.previous = previous;
        this.destination = destination;
        this.balance = new BigInteger(balance).toString(16); // balance in hex
        this.work = work;
        String hash = NanoUtil.computeSendHash(previous, destination, this.balance);
        this.signature = NanoUtil.sign(private_key, public_key, hash);
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
