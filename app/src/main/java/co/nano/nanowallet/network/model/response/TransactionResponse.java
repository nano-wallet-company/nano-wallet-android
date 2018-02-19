package co.nano.nanowallet.network.model.response;

import com.google.gson.annotations.SerializedName;

import co.nano.nanowallet.network.model.BaseNetworkModel;

/**
 * Error response from service
 */

public class TransactionResponse extends BaseNetworkModel{
    @SerializedName("account")
    private String account;

    @SerializedName("hash")
    private String hash;

    @SerializedName("block")
    private BlockItem block;

    @SerializedName("amount")
    private String amount;

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

    @Override
    public String toString() {
        return "TransactionResponse{" +
                "account='" + account + '\'' +
                ", hash='" + hash + '\'' +
                ", block=" + block +
                ", amount='" + amount + '\'' +
                '}';
    }
}
