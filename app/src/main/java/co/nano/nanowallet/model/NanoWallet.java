package co.nano.nanowallet.model;

import android.content.Context;

import com.hwangjr.rxbus.annotation.Subscribe;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import co.nano.nanowallet.bus.RxBus;
import co.nano.nanowallet.bus.WalletHistoryUpdate;
import co.nano.nanowallet.bus.WalletPriceUpdate;
import co.nano.nanowallet.bus.WalletSubscribeUpdate;
import co.nano.nanowallet.network.model.response.AccountHistoryResponse;
import co.nano.nanowallet.network.model.response.AccountHistoryResponseItem;
import co.nano.nanowallet.network.model.response.CurrentPriceResponse;
import co.nano.nanowallet.network.model.response.SubscribeResponse;
import co.nano.nanowallet.util.NumberUtil;
import co.nano.nanowallet.util.SharedPreferencesUtil;


/**
 * Nano wallet that holds transactions and current prices
 */
public class NanoWallet {
    BigDecimal accountBalance;
    BigDecimal localCurrencyPrice;
    BigDecimal btcPrice;

    String accountAddress;
    String representativeAddress;
    String frontierBlock;
    String openBlock;

    Integer blockCount;

    private List<AccountHistoryResponseItem> accountHistory;

    @Inject
    SharedPreferencesUtil sharedPreferencesUtil;

    public NanoWallet(Context context) {
        accountBalance = new BigDecimal("0.0");
        RxBus.get().register(this);
    }

    /**
     * Receive account subscribe response
     * @param subscribeResponse
     */
    @Subscribe
    public void receiveCurrentPrice(SubscribeResponse subscribeResponse) {
        frontierBlock = subscribeResponse.getFrontier();
        representativeAddress = subscribeResponse.getRepresentative_block();
        openBlock = subscribeResponse.getOpen_block();
        blockCount = subscribeResponse.getBlock_count();
        accountBalance = new BigDecimal(subscribeResponse.getBalance());
        RxBus.get().post(new WalletSubscribeUpdate());
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

    /**
     * Receive a current price response
     * @param currentPriceResponse
     */
    @Subscribe
    public void receiveCurrentPrice(CurrentPriceResponse currentPriceResponse) {
        localCurrencyPrice = new BigDecimal(currentPriceResponse.getPrice());
        btcPrice = currentPriceResponse.getBtc() != null ? new BigDecimal(currentPriceResponse.getBtc()) : btcPrice;
        RxBus.get().post(new WalletPriceUpdate());
    }

    public List<AccountHistoryResponseItem> getAccountHistory() {
        return accountHistory;
    }

    // old methods
    public String getAccountBalanceNano() {
        return NumberUtil.getRawAsUsableString(accountBalance.toString());
    }

    public String getAccountBalanceLocalCurrency() {
        return localCurrencyPrice != null && accountBalance != null ? formatLocalCurrency(NumberUtil.getRawAsUsableAmount(accountBalance.toString()).multiply(localCurrencyPrice, MathContext.DECIMAL64)) : "0.0";
    }

    public String getAccountBalanceBtc() {
        return btcPrice != null && accountBalance != null ? formatBtc(NumberUtil.getRawAsUsableAmount(accountBalance.toString()).multiply(btcPrice, MathContext.DECIMAL64)) : "0.0";
    }

    private String formatLocalCurrency(BigDecimal amount) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
        return numberFormat.format(Double.valueOf(amount.toString()));
    }

    private String formatBtc(BigDecimal amount) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        numberFormat.setMaximumFractionDigits(8);
        return numberFormat.format(Double.valueOf(amount.toString()));
    }

    public void setAccountBalance(BigDecimal accountBalance) {
        this.accountBalance = accountBalance;
    }

    public void close() {
        RxBus.get().unregister(this);
    }
}
