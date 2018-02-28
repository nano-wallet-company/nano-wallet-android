package co.nano.nanowallet.network.model.request;

import com.google.gson.annotations.SerializedName;

import co.nano.nanowallet.network.model.Actions;
import co.nano.nanowallet.network.model.BaseRequest;

/**
 * Retrieve pending transactions
 */

public class PendingTransactionsRequest extends BaseRequest {
    @SerializedName("action")
    private String action;

    @SerializedName("account")
    private String account;

    @SerializedName("source")
    private Boolean source;

    @SerializedName("count")
    private Integer count;

    public PendingTransactionsRequest() {
        this.action = Actions.PENDING.toString();
    }

    public PendingTransactionsRequest(String account, Boolean source, Integer count) {
        this.action = Actions.PENDING.toString();
        this.account = account;
        this.source = source;
        this.count = count;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Boolean getSource() {
        return source;
    }

    public void setSource(Boolean source) {
        this.source = source;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
