package co.nano.nanowallet.model;

import java.util.Date;

/**
 * Temp class to use for transactions to show on the home screen
 */

public class Transaction {

    private float amount;
    private Date timestamp;
    private String address;
    private boolean isSend;

    public Transaction() {
    }

    public Transaction(float amount, Date timestamp, String address, boolean isSend) {
        this.amount = amount;
        this.timestamp = timestamp;
        this.address = address;
        this.isSend = isSend;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getAddress() {
        return address;
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

        if (Float.compare(that.amount, amount) != 0) return false;
        if (isSend != that.isSend) return false;
        if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null)
            return false;
        return address != null ? address.equals(that.address) : that.address == null;
    }

    @Override
    public int hashCode() {
        int result = (amount != +0.0f ? Float.floatToIntBits(amount) : 0);
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
