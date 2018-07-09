package co.nano.nanowallet.network.model.response;

import com.google.gson.annotations.SerializedName;

import co.nano.nanowallet.network.model.BaseResponse;

public class BlockInfoItem extends BaseResponse {
    @SerializedName("block_account")
    private String blockAccount;

    @SerializedName("amount")
    private String amount;

    @SerializedName("balance")
    private String balance;

    @SerializedName("pending")
    private String pending;

    @SerializedName("source_account")
    private String sourceAccount;

    @SerializedName("contents")
    private String contents;

    public BlockInfoItem() {
    }

    public String getBlockAccount() {
        return blockAccount;
    }

    public void setBlockAccount(String blockAccount) {
        this.blockAccount = blockAccount;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getPending() {
        return pending;
    }

    public void setPending(String pending) {
        this.pending = pending;
    }

    public String getSourceAccount() {
        return sourceAccount;
    }

    public void setSourceAccount(String sourceAccount) {
        this.sourceAccount = sourceAccount;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }
}
