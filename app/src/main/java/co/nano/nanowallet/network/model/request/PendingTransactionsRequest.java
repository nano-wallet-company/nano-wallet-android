package co.nano.nanowallet.network.model.request;

import com.google.gson.annotations.SerializedName;

import co.nano.nanowallet.network.model.Actions;

/**
 * Retrieve pending transactions
 */

public class PendingTransactionsRequest {
    @SerializedName("action")
    private String action;

    @SerializedName("source")
    private String source;

    @SerializedName("count")
    private Integer count;

    public PendingTransactionsRequest() {
        this.action = Actions.PENDING.toString();
    }

    public PendingTransactionsRequest(String source, Integer count) {
        this.action = Actions.PENDING.toString();
        this.source = source;
        this.count = count;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
