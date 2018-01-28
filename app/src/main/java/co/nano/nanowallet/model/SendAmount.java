package co.nano.nanowallet.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Holds display amounts to send to another user
 */

public class SendAmount {
    private String nanoAmount;
    private String localCurrencyAmount;

    /**
     * Default Constructor
     */
    public SendAmount() {
        nanoAmount = "";
        localCurrencyAmount = "";
    }

    /**
     * Set String representations of nanoAmount and localCurrencyAmount
     * @param nanoAmount
     * @param localCurrencyAmount
     */
    public SendAmount(String nanoAmount, String localCurrencyAmount) {
        this.nanoAmount = nanoAmount;
        this.localCurrencyAmount = localCurrencyAmount;
    }

    /**
     * Get Nano amount entered
     * @return String
     */
    public String getNanoAmount() {
        return nanoAmount;
    }


    /**
     * Set Nano amount which will also set the local currency amount
     * @param nanoAmount String Nano Amount from input
     */
    public void setNanoAmount(String nanoAmount) {
        this.nanoAmount = nanoAmount;
        if (nanoAmount.length() > 0) {
            if (nanoAmount.equals(".")) {
                this.nanoAmount = "0.";
            }
            this.localCurrencyAmount = convertNanoToLocalCurrency(this.nanoAmount);
        } else {
            this.localCurrencyAmount = "";
        }
    }

    /**
     * Convert a Nano string amount to a local currency String
     * @param amount String amount of Nano
     * @return String local currency amount
     */
    private String convertNanoToLocalCurrency(String amount) {
        if (amount.equals("0.")) {
            return amount;
        } else {
            // TODO: Add real conversion
            return new BigDecimal(amount)
                    .multiply(new BigDecimal(30))
                    .toString();
        }
    }

    /**
     * Return the  local currency amount as a string
     * @return
     */
    public String getLocalCurrencyAmount() {
        return localCurrencyAmount;
    }

    /**
     * Return the currency formatted local currency amount as a string
     * @return
     */
    public String getLocalCurrencyAmountFormatted() {
        if (localCurrencyAmount.length() > 0) {
            return currencyFormat(Float.valueOf(localCurrencyAmount));
        } else {
            return "";
        }
    }

    /**
     * Set the local currency amount and also update the nano amount to match
     * @param localCurrencyAmount String of local currency amount from input
     */
    public void setLocalCurrencyAmount(String localCurrencyAmount) {
        this.localCurrencyAmount = localCurrencyAmount;
        if (localCurrencyAmount.length() > 0) {
            if (localCurrencyAmount.equals(".")) {
                this.localCurrencyAmount = "0.";
            }
            this.nanoAmount = convertLocalCurrencyToNano(this.localCurrencyAmount);
        } else {
            this.nanoAmount = "";
        }
    }

    /**
     * Convert a local currency string to a Nano string
     * @param amount Local currency amount from input
     * @return String of Nano converted amount
     */
    private String convertLocalCurrencyToNano(String amount) {
        if (amount.equals("0.")) {
            return amount;
        } else {
            // TODO: implement actual conversion
            return new BigDecimal(amount)
                    .divide(new BigDecimal(30), RoundingMode.FLOOR)
                    .toString();
        }
    }

    /**
     * Convert local currency to properly formatted string for the currency
     */
    private String currencyFormat(float amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
        format.setCurrency(Currency.getInstance("USD")); // TODO: get saved local currency
        String stringAmount = format.format(amount);
        return stringAmount.substring(1, stringAmount.length()); // drop the currency symbol
    }
}
