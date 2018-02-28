package co.nano.nanowallet.network.model.request.block;

import com.google.gson.annotations.SerializedName;

/**
 * Base block
 */

public class Block {
    @SerializedName("work")
    private String work;

    public String getWork() {
        return work;
    }

    public void setWork(String work) {
        this.work = work;
    }
}
