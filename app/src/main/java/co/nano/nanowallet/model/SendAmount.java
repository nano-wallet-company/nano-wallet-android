package co.nano.nanowallet.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Sending amount to another user
 */

public class SendAmount {
    private String nanoAmount;
    private String localCurrencyAmount;

    public SendAmount() {
        nanoAmount = "";
        localCurrencyAmount = "";
    }

    public SendAmount(String nanoAmount, String localCurrencyAmount) {
        this.nanoAmount = nanoAmount;
        this.localCurrencyAmount = localCurrencyAmount;
    }

    public String getNanoAmount() {
        return nanoAmount;
    }

    public void setNanoAmount(String nanoAmount) {
        this.nanoAmount = nanoAmount;
        if (nanoAmount.length() > 0) {
            this.localCurrencyAmount =
                    new BigDecimal(nanoAmount)
                            .multiply(new BigDecimal(30))
                            .toString();
        } else {
            this.localCurrencyAmount = "";
        }
    }

    public String getLocalCurrencyAmount() {
        return localCurrencyAmount;
    }

    public String getLocalCurrencyAmountFormatted() {
        if (localCurrencyAmount.length() > 0) {
            return currencyFormat(Float.valueOf(localCurrencyAmount));
        } else {
            return "";
        }
    }

    public void setLocalCurrencyAmount(String localCurrencyAmount) {
        this.localCurrencyAmount = localCurrencyAmount;
        if (localCurrencyAmount.length() > 0) {
            this.nanoAmount = new BigDecimal(localCurrencyAmount)
                    .divide(new BigDecimal(30), RoundingMode.FLOOR)
                    .toString();
        } else {
            this.nanoAmount = "";
        }
    }

    private String currencyFormat(float amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
        format.setCurrency(Currency.getInstance("USD")); // TODO: get saved local currency
        String stringAmount = format.format(amount);
        return stringAmount.substring(1, stringAmount.length()); // drop the currency symbol
    }
}
