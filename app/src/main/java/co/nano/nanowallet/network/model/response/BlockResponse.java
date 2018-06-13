package co.nano.nanowallet.network.model.response;

import com.google.gson.annotations.SerializedName;

import co.nano.nanowallet.network.model.BaseResponse;

/**
 * BlockItem Item
 */

public class BlockResponse extends BaseResponse {
    @SerializedName("type")
    private String type;

    @SerializedName("account")
    private String account;

    @SerializedName("previous")
    private String previous;

    @SerializedName("representative")
    private String representative;

    @SerializedName("balance")
    private String balance;

    @SerializedName("link")
    private String link;

    @SerializedName("link_as_account")
    private String link_as_account;

    @SerializedName("work")
    private String work;

    @SerializedName("signature")
    private String signature;

    public BlockResponse() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public String getRepresentative() {
        return representative;
    }

    public void setRepresentative(String representative) {
        this.representative = representative;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLink_as_account() {
        return link_as_account;
    }

    public void setLink_as_account(String link_as_account) {
        this.link_as_account = link_as_account;
    }

    public String getWork() {
        return work;
    }

    public void setWork(String work) {
        this.work = work;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
