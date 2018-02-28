package co.nano.nanowallet.network.model.request;

import com.google.gson.annotations.SerializedName;

import co.nano.nanowallet.network.model.Actions;
import co.nano.nanowallet.network.model.BaseRequest;

/**
 * Fetch work for a transaction
 */

public class WorkRequest extends BaseRequest {
    @SerializedName("action")
    private String action;

    @SerializedName("hash")
    private String hash;

    public WorkRequest() {
        this.action = Actions.WORK.toString();
    }

    public WorkRequest(String hash) {
        this.action = Actions.WORK.toString();
        this.hash = hash;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
