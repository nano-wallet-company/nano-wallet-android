package co.nano.nanowallet.network.model.response;

import com.google.gson.annotations.SerializedName;

import co.nano.nanowallet.network.model.BaseResponse;

/**
 * Error response from service
 */

public class TransactionResponse extends BaseResponse {
    @SerializedName("account")
    private String account;

    @SerializedName("hash")
    private String hash;

    @SerializedName("block")
    private BlockItem block;

    @SerializedName("amount")
    private String amount;

    @SerializedName("is_send")
    private String is_send;

    public TransactionResponse() {
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public BlockItem getBlock() {
        return block;
    }

    public void setBlock(BlockItem block) {
        this.block = block;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getIs_send() {
        return is_send;
    }

    public void setIs_send(String is_send) {
        this.is_send = is_send;
    }

    @Override
    public String toString() {
        return "TransactionResponse{" +
                "account='" + account + '\'' +
                ", hash='" + hash + '\'' +
                ", block=" + block +
                ", amount='" + amount + '\'' +
                ", is_send='" + is_send + '\'' +
                '}';
    }
}
