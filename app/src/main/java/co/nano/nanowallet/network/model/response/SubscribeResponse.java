package co.nano.nanowallet.network.model.response;

import com.google.gson.annotations.SerializedName;

import co.nano.nanowallet.network.model.BaseResponse;

/**
 * Response from subscribe request
 */

public class SubscribeResponse extends BaseResponse {
    // The frontier is the latest block for the account-chain
    @SerializedName("frontier")
    private String frontier;

    // The open block is the very first block for the account-chain
    @SerializedName("open_block")
    private String open_block;

    // If the user has changed representatives with a change block, this will be the hash. otherwise it will match the open block
    @SerializedName("representative_block")
    private String representative_block;

    // Balance in raw units. 1 XRB = 1000000000000000000000000000000 raw units
    @SerializedName("balance")
    private String balance;

    // node-specific timestamp. Dont use this, I will be implementing timestamps in the very near future, keep timestamps/dates blank for now.
    @SerializedName("modified_timestamp")
    private String modified_timestamp;

    // number of blocks in the account chain. Store this and use it to reduce data requests on subsequent connections. see account_history for more
    @SerializedName("block_count")
    private Integer block_count;

    // if there are any pending blocks, this will be the total amount pending deposit
    @SerializedName("pending")
    private String pending;

    // log this for debugging, the socket server generates a unique uuid for each connection.
    @SerializedName("uuid")
    private String uuid;

    @SerializedName("price")
    private String price;

    @SerializedName("btc")
    private String btc;

    public SubscribeResponse() {
    }



    public String getFrontier() {
        return frontier;
    }

    public void setFrontier(String frontier) {
        this.frontier = frontier;
    }

    public String getOpen_block() {
        return open_block;
    }

    public void setOpen_block(String open_block) {
        this.open_block = open_block;
    }

    public String getRepresentative_block() {
        return representative_block;
    }

    public void setRepresentative_block(String representative_block) {
        this.representative_block = representative_block;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getModified_timestamp() {
        return modified_timestamp;
    }

    public void setModified_timestamp(String modified_timestamp) {
        this.modified_timestamp = modified_timestamp;
    }

    public Integer getBlock_count() {
        return block_count;
    }

    public void setBlock_count(Integer block_count) {
        this.block_count = block_count;
    }

    public String getPending() {
        return pending;
    }

    public void setPending(String pending) {
        this.pending = pending;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getBtc() {
        return btc;
    }

    public void setBtc(String btc) {
        this.btc = btc;
    }
}
