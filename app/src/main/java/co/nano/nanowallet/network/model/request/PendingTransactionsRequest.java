package co.nano.nanowallet.network.model.request;

import com.google.gson.annotations.SerializedName;

import co.nano.nanowallet.network.model.Actions;
import co.nano.nanowallet.network.model.BaseNetworkModel;

/**
 * Subscribe to websocket server for updates regarding the specified source.
 * First action to take when connecting when app opens or reconnects, IF a wallet already exists
 */

public class PendingTransactionsRequest extends BaseNetworkModel {
    @SerializedName("action")
    private String action;

    @SerializedName("source")
    private String source;

    @SerializedName("count")
    private String count;

    public PendingTransactionsRequest() {
        this.action = Actions.PENDING.toString();
    }

    public PendingTransactionsRequest(String source, String count) {
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

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }
}
