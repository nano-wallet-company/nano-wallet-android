package co.nano.nanowallet.network.model.request;

import com.google.gson.annotations.SerializedName;

import co.nano.nanowallet.network.model.Actions;
import co.nano.nanowallet.network.model.BaseRequest;

/**
 * Subscribe to websocket server for updates regarding the specified account.
 * First action to take when connecting when app opens or reconnects, IF a wallet already exists
 */

public class ProcessRequest extends BaseRequest {
    @SerializedName("action")
    private String action;

    @SerializedName("block")
    private String block;

    public ProcessRequest() {
        this.action = Actions.PROCESS.toString();
    }

    public ProcessRequest(String block) {
        this.action = Actions.PROCESS.toString();
        this.block = block;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    @Override
    public String toString() {
        return "ProcessRequest{" +
                "action='" + action + '\'' +
                ", block='" + block + '\'' +
                '}';
    }
}
