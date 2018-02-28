package co.nano.nanowallet.network.model.request;

import com.google.gson.annotations.SerializedName;

import co.nano.nanowallet.network.model.Actions;
import co.nano.nanowallet.network.model.BaseRequest;

/**
 * Retrieve account history
 */

public class AccountHistoryRequest extends BaseRequest {
    @SerializedName("action")
    private String action;

    @SerializedName("account")
    private String account;

    // the first time you connect, you will want to fetch all transactions, use "block_count"
    // value from when you subscribed, else the difference between the last block_count and
    // the current one, to save on data with the server
    @SerializedName("count")
    private Integer count;

    public AccountHistoryRequest() {
        this.action = Actions.HISTORY.toString();
    }

    public AccountHistoryRequest(String account, Integer count) {
        this.action = Actions.HISTORY.toString();
        this.account = account;
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

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
