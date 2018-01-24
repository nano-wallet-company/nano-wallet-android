package co.nano.nanowallet.model;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Temp class to use for transactions to show on the home screen
 */

public class Transaction {

    private BigDecimal amount;
    private Date timestamp;
    private String address;
    private boolean isSend;

    public Transaction() {
    }

    public Transaction(BigDecimal amount, Date timestamp, String address, boolean isSend) {
        this.amount = amount;
        this.timestamp = timestamp;
        this.address = address;
        this.isSend = isSend;
    }

    public String getAmount() {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        return numberFormat.format(Double.valueOf(amount.toString()));
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /**
     * Get relative time
     * @return
     */
    public String getTimestamp() {
        long now = System.currentTimeMillis();
        return DateUtils.getRelativeTimeSpanString(
                timestamp.getTime(),
                now,
                DateUtils.SECOND_IN_MILLIS).toString();
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Spannable getAddress() {
        StringBuilder sb = new StringBuilder();
        // take first 5 characters
        // then an ellipsis
        // then the last 5 characters of the address
        sb.append(address.substring(0, 5));
        sb.append(" … ");
        sb.append(address.substring(address.length() - 5, address.length()));
        Spannable s = new SpannableString(sb.toString());
        s.setSpan(new ForegroundColorSpan(Color.parseColor("#4a90e2")), 0, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new ForegroundColorSpan(Color.parseColor("#e1990e")), 8, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return s;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isSend() {
        return isSend;
    }

    public void setSend(boolean send) {
        isSend = send;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transaction that = (Transaction) o;

        if (isSend != that.isSend) return false;
        if (amount != null ? !amount.equals(that.amount) : that.amount != null) return false;
        if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null)
            return false;
        return address != null ? address.equals(that.address) : that.address == null;
    }

    @Override
    public int hashCode() {
        int result = amount != null ? amount.hashCode() : 0;
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (isSend ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "amount=" + amount +
                ", timestamp=" + timestamp +
                ", address='" + address + '\'' +
                ", isSend=" + isSend +
                '}';
    }
}
