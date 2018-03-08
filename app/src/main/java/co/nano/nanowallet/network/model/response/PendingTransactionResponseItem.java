package co.nano.nanowallet.network.model.response;

import com.google.gson.annotations.SerializedName;

import co.nano.nanowallet.util.NumberUtil;

/**
 * Account History Item
 */

public class PendingTransactionResponseItem {
    // receive block
    @SerializedName("source")
    private String source;

    // raw-value of the transaction
    @SerializedName("amount")
    private String amount;

    // hash of the block, use to get the full block data from server. also reference this for
    // creating new blocks or keeping track of new txns
    @SerializedName("hash")
    private String hash;

    private boolean inProgress;
    private boolean complete;

    public PendingTransactionResponseItem() {
    }

    public PendingTransactionResponseItem(String source, String amount, String hash) {
        this.source = source;
        this.amount = amount;
        this.hash = hash;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getAmount() {
        return amount;
    }

    public String getFormattedAmount() {
        return NumberUtil.getRawAsUsableString(amount);
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }
}
