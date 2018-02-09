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

    public static BigDecimal getRawAsUsableAmount(String raw) {
        BigDecimal amount = new BigDecimal(raw);
        return amount.divide(new BigDecimal(xrbDivider), 32, RoundingMode.FLOOR);
    }

    public static String getRawAsUsableString(String raw) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        numberFormat.setMaximumFractionDigits(6);
        return numberFormat.format(getRawAsUsableAmount(raw));
    }

    public static String getAmountAsUsableString(String amount) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        numberFormat.setMaximumFractionDigits(6);
        return numberFormat.format(amount);
    }

    public static String getRawAsLongerUsableString(String raw) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
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
}
