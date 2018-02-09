package co.nano.nanowallet.network.model.response;

import com.google.gson.annotations.SerializedName;

import co.nano.nanowallet.network.model.BaseNetworkModel;

/**
 * Pushed price data - currently sent every minute to all clients
 */

public class WorkResponse extends BaseNetworkModel {
    @SerializedName("work")
    private String work;

    public WorkResponse() {
    }

    public String getWork() {
        return work;
    }

    public void setWork(String work) {
        this.work = work;
    }
}
