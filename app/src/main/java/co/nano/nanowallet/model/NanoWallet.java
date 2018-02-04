package co.nano.nanowallet.model;

import android.content.Context;

import com.hwangjr.rxbus.annotation.Subscribe;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import co.nano.nanowallet.bus.RxBus;
import co.nano.nanowallet.bus.WalletHistoryUpdate;
import co.nano.nanowallet.network.model.response.AccountHistoryResponse;
import co.nano.nanowallet.network.model.response.AccountHistoryResponseItem;
import co.nano.nanowallet.util.SharedPreferencesUtil;


/**
 * Nano wallet that holds transactions and current prices
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


    private Context context;
    private List<AccountHistoryResponseItem> accountHistory;

    @Inject
    SharedPreferencesUtil sharedPreferencesUtil;

    public NanoWallet(Context context) {
        accountBalance = new BigDecimal("0.0");
        RxBus.get().register(this);
    }

    /**
     * Receive a history update
     * @param accountHistoryResponse
     */
    @Subscribe
    public void receiveHistory(AccountHistoryResponse accountHistoryResponse) {
        accountHistory = accountHistoryResponse.getHistory();
        RxBus.get().post(new WalletHistoryUpdate());
    }

    public List<AccountHistoryResponseItem> getAccountHistory() {
        return accountHistory;
    }

    // old methods
    public String getAccountBalanceNano() {
        return formatBalance(accountBalance);
    }

    public String getAccountBalanceUsd() {
        // TODO: Put proper conversion formula in here
        return formatBalance(accountBalance.multiply(new BigDecimal(28), MathContext.DECIMAL64));
    }

    public String getAccountBalanceBtc() {
        // TODO: Put proper conversion formula in here
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

    public void close() {
        RxBus.get().unregister(this);
        context = null;
    }
}
