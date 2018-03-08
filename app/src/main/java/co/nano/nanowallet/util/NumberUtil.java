package co.nano.nanowallet.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility for formatting Nano amounts
 */

public class NumberUtil {
    public static final BigInteger baseOfDivider = new BigInteger("10");
    public static final BigInteger xrbDivider = baseOfDivider.pow(30);
    public static final BigInteger nanoDivider = baseOfDivider.pow(24);

    /**
     * Go from 32-bit raw NANO to user readable big decimal
     *
     * @param raw 1000000000000000000000000000000
     * @return BigDecimal value 1.000000000000000000000000000000
     */
    public static BigDecimal getRawAsUsableAmount(String raw) {
        BigDecimal amount = new BigDecimal(raw);
        return amount.divide(new BigDecimal(xrbDivider), 32, RoundingMode.FLOOR);
    }

    /**
     * Go from 32-bit raw NANO to user readable String with up to 6 decimal places
     *
     * @param raw 1000000000000000000000000000000
     * @return String 1
     */
    public static String getRawAsUsableString(String raw) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        numberFormat.setMaximumFractionDigits(6);
        return numberFormat.format(getRawAsUsableAmount(raw));
    }

    /**
     * Go from readable NANO String to a raw BigDecimal
     *
     * @param amount 1
     * @return BigInteger 1000000000000000000000000000000
     */
    public static BigInteger getAmountAsRawBigInteger(String amount) {
        try {
            BigDecimal raw = new BigDecimal(amount);
            return raw.multiply(new BigDecimal(xrbDivider.toString())).toBigInteger();
        } catch(Exception e) {
            ExceptionHandler.handle(e);
        }
        return new BigInteger("0");
    }

    public static String getAmountAsUsableString(String amount) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        numberFormat.setMaximumFractionDigits(6);
        return numberFormat.format(amount);
    }

    public static String getRawAsLongerUsableString(String raw) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        numberFormat.setMaximumFractionDigits(10);
        return numberFormat.format(Double.valueOf(getRawAsUsableAmount(raw).toString()));
    }

    public static String getAmountAsLongerUsableString(String amount) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        numberFormat.setMaximumFractionDigits(10);
        return numberFormat.format(amount);
    }

    public static Double getRawAsDouble(String raw) {
        return getRawAsUsableAmount(raw).doubleValue();
    }

    public static BigInteger getAsRawValue(String value) {
        return new BigInteger(new BigDecimal(value).multiply(new BigDecimal(xrbDivider)).toString());
    }

    public static String getAsRawString(String value) {
        return getAsRawValue(value).toString();
    }

    public static String getRawAsHex(String raw) {
        // convert to hex
        String hex = new BigInteger(raw).toString(16);

        // left-pad with zeros to be 32 length
        StringBuilder sb = new StringBuilder();
        for (int toPrepend = 32 - hex.length(); toPrepend > 0; toPrepend--) {
            sb.append('0');
        }
        sb.append(hex);
        return sb.toString().toUpperCase();
    }
}
