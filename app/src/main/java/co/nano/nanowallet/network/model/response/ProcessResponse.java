package co.nano.nanowallet.network.model.response;

import com.google.gson.annotations.SerializedName;

import co.nano.nanowallet.network.model.BaseNetworkModel;

/**
 * hash response from service
 */

public class ProcessResponse extends BaseNetworkModel{
    @SerializedName("hash")
    private String hash;

    public ProcessResponse() {
    }

    public ProcessResponse(String hash) {
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
