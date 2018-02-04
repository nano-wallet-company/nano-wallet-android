package co.nano.nanowallet.network.model.response;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import com.google.gson.annotations.SerializedName;

import co.nano.nanowallet.network.model.BlockTypes;
import co.nano.nanowallet.util.NumberUtil;

/**
 * Account History Item
 */

public class AccountHistoryResponseItem {
    // valid block types: send, receive, change, open. this command only returns
    // send and receive data. if last txn is 'change', it wont be shown here
    @SerializedName("type")
    private String type;

    // for a send block, this is the destination account
    @SerializedName("account")
    private String account;

    // raw-value of the transaction
    @SerializedName("amount")
    private String amount;

    // hash of the block, use to get the full block data from server. also reference this for
    // creating new blocks or keeping track of new txns
    @SerializedName("hash")
    private String hash;

    public AccountHistoryResponseItem() {
    }

    public AccountHistoryResponseItem(String type, String account, String amount, String hash) {
        this.type = type;
        this.account = account;
        this.amount = amount;
        this.hash = hash;
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

    public Spannable getAddressShort() {
        StringBuilder sb = new StringBuilder();
        // take first 5 characters
        // then an ellipsis
        // then the last 5 characters of the address
        sb.append(account.substring(0, 5));
        sb.append(" … ");
        sb.append(account.substring(account.length() - 5, account.length()));
        Spannable s = new SpannableString(sb.toString());

        // colorize the string
        s.setSpan(new ForegroundColorSpan(Color.parseColor("#4a90e2")), 0, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new ForegroundColorSpan(Color.parseColor("#e1990e")), 8, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return s;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAmount() {
        return amount;
    }

    public String getFormattedAmount() {
        return NumberUtil.getRawAsUsableString(amount);
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public boolean isSend() {
        return this.type.equals(BlockTypes.SEND.toString());
    }
}
