package co.nano.nanowallet;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by admin on 12/22/2017.
 */




public class NanoWallet {
    public static final BigInteger baseOfDivider = new BigInteger("10");
    public static final BigInteger xrbDivider = baseOfDivider.pow(30);
    public static final BigInteger nanoDivider = baseOfDivider.pow(24);

    String accountAddress;
    String representativeAddress;
    String frontierBlock;
    String openBlock;
    BigDecimal accountBalance;
    Integer blockCount;

    String seed;
    Integer seedIndex;
    String privateKey;

    public String getAccountBalanceNano() {
        return formatBalance(accountBalance);
    }

    public String getAccountBalanceUsd() {
        // TODO: Put proper conversion forumla in here
        return formatBalance(accountBalance.multiply(new BigDecimal(28), MathContext.DECIMAL64));
    }

    public String getAccountBalanceBtc() {
        // TODO: Put proper conversion forumla in here
        return formatBalance(accountBalance.divide(new BigDecimal(175), RoundingMode.FLOOR));
    }

    private String formatBalance(BigDecimal amount) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        numberFormat.setMinimumFractionDigits(2);
        return numberFormat.format(Double.valueOf(amount.toString()));
    }

    public void setAccountBalance(BigDecimal accountBalance) {
        this.accountBalance = accountBalance;
    }
}
