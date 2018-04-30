package co.nano.nanowallet.model;

import android.content.Context;

import com.hwangjr.rxbus.annotation.Subscribe;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import co.nano.nanowallet.NanoUtil;
import co.nano.nanowallet.bus.RxBus;
import co.nano.nanowallet.bus.SendInvalidAmount;
import co.nano.nanowallet.bus.WalletClear;
import co.nano.nanowallet.bus.WalletHistoryUpdate;
import co.nano.nanowallet.bus.WalletPriceUpdate;
import co.nano.nanowallet.bus.WalletSubscribeUpdate;
import co.nano.nanowallet.network.model.response.AccountHistoryResponse;
import co.nano.nanowallet.network.model.response.AccountHistoryResponseItem;
import co.nano.nanowallet.network.model.response.CurrentPriceResponse;
import co.nano.nanowallet.network.model.response.SubscribeResponse;
import co.nano.nanowallet.ui.common.ActivityWithComponent;
import co.nano.nanowallet.util.ExceptionHandler;
import co.nano.nanowallet.util.NumberUtil;
import co.nano.nanowallet.util.SharedPreferencesUtil;
import io.realm.Realm;


/**
 * Nano wallet that holds transactions and current prices
 */
public class NanoWallet {
    private BigDecimal accountBalance;
    private BigDecimal localCurrencyPrice;
    private BigDecimal btcPrice;

    private String representativeAddress;
    private String frontierBlock;
    private String openBlock;

    private Integer blockCount;
    private String uuid;

    private List<AccountHistoryResponseItem> accountHistory;

    // for sending
    private String sendNanoAmount;
    private String sendLocalCurrencyAmount;
    private String publicKey;

    private static final int MAX_NANO_DISPLAY_LENGTH = 10;

    @Inject
    SharedPreferencesUtil sharedPreferencesUtil;

    @Inject
    Realm realm;

    public NanoWallet(Context context) {
        // init dependency injection
        if (context instanceof ActivityWithComponent) {
            ((ActivityWithComponent) context).getActivityComponent().inject(this);
        }

        clear();
        RxBus.get().register(this);

        if (realm != null && !realm.isClosed()) {
            Credentials credentials = realm.where(Credentials.class).findFirst();
            if (credentials != null) {
                publicKey = credentials.getPublicKey();
                uuid = credentials.getUuid();
            }
        }
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getOpenBlock() {
        return openBlock;
    }

    public void setOpenBlock(String openBlock) {
        this.openBlock = openBlock;
    }

    public Integer getBlockCount() {
        return blockCount;
    }

    public void setBlockCount(Integer blockCount) {
        this.blockCount = blockCount;
    }

    public BigDecimal getLocalCurrencyPrice() {
        return localCurrencyPrice;
    }

    public void setLocalCurrencyPrice(BigDecimal localCurrencyPrice) {
        this.localCurrencyPrice = localCurrencyPrice;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public List<AccountHistoryResponseItem> getAccountHistory() {
        return accountHistory;
    }

    public String getAccountBalanceNano() {
        return NumberUtil.getRawAsUsableString(accountBalance.toString());
    }

    public BigDecimal getAccountBalanceNanoRaw() {
        return accountBalance;
    }

    public String getLongerAccountBalanceNano() {
        return NumberUtil.getRawAsLongerUsableString(accountBalance.toString());
    }

    public String getAccountBalanceLocalCurrency() {
        return localCurrencyPrice != null && accountBalance != null ? formatLocalCurrency(NumberUtil.getRawAsUsableAmount(accountBalance.toString()).multiply(localCurrencyPrice, MathContext.DECIMAL64)) : "0.0";
    }

    public String getAccountBalanceBtc() {
        return btcPrice != null && accountBalance != null ? formatBtc(NumberUtil.getRawAsUsableAmount(accountBalance.toString()).multiply(btcPrice, MathContext.DECIMAL64)) : "0.0";
    }

    private String formatLocalCurrency(BigDecimal amount) {
        return currencyFormat(amount);
    }

    private String formatBtc(BigDecimal amount) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        numberFormat.setMaximumFractionDigits(8);
        return numberFormat.format(Double.valueOf(amount.toString()));
    }

    public void setAccountBalance(BigDecimal accountBalance) {
        this.accountBalance = accountBalance;
    }

    public AvailableCurrency getLocalCurrency() {
        return sharedPreferencesUtil.getLocalCurrency();
    }

    public void clearSendAmounts() {
        sendNanoAmount = "";
        sendLocalCurrencyAmount = "";
    }

    /**
     * Get Nano amount entered
     *
     * @return String
     */
    public String getSendNanoAmount() {
        return sendNanoAmount;
    }

    /**
     * Return the currency formatted local currency amount as a string
     *
     * @return Formatted Nano amount
     */
    public String getSendNanoAmountFormatted() {
        if (sendNanoAmount.length() > 0) {
            return nanoFormat(sendNanoAmount);
        } else {
            return "";
        }
    }

    /**
     * Remove any non-currency characters
     *
     * @param amount Unsanitized string
     * @return Santized string with everything but digits, decimal points, and commas removed
     */
    public String sanitize(String amount) {
        return amount.replaceAll("[^\\d.,]", "");
    }

    /**
     * Remove all but digits and decimals
     *
     * @param amount Unsanitized string
     * @return Santized string with everything but digits and decimal points removed
     */
    public String sanitizeNoCommas(String amount) {
        return amount.replaceAll("[^\\d.]", "");
    }


    /**
     * Set Nano amount which will also set the local currency amount
     *
     * @param nanoAmount String Nano Amount from input
     */
    public void setSendNanoAmount(String nanoAmount) {
        this.sendNanoAmount = sanitize(nanoAmount);
        if (nanoAmount.length() > 0) {
            if (this.sendNanoAmount.equals(".")) {
                this.sendNanoAmount = "0.";
            }
            if (this.sendNanoAmount.equals("00")) {
                this.sendNanoAmount = "0";
            }

            // keep decimal length at 10 total
            String[] split = this.sendNanoAmount.split("\\.");
            if (split.length > 1) {
                String whole = split[0];
                String decimal = split[1];
                decimal = decimal.substring(0, Math.min(decimal.length(), MAX_NANO_DISPLAY_LENGTH));
                this.sendNanoAmount = whole + "." + decimal;
            }

            this.sendLocalCurrencyAmount = convertNanoToLocalCurrency(this.sendNanoAmount);
        } else {
            this.sendLocalCurrencyAmount = "";
        }
        validateSendAmount();
    }

    /**
     * Convert a Nano string amount to a local currency String
     *
     * @param amount String amount of Nano
     * @return String local currency amount
     */
    private String convertNanoToLocalCurrency(String amount) {
        if (amount.equals("0.")) {
            return amount;
        } else {
            return localCurrencyPrice != null ?
                    formatLocalCurrency(new BigDecimal(sanitizeNoCommas(amount))
                            .multiply(localCurrencyPrice, MathContext.DECIMAL64)) : "0.0";
        }
    }

    /**
     * Return the  local currency amount as a string
     *
     * @return Local currency amount
     */
    public String getLocalCurrencyAmount() {
        return sendLocalCurrencyAmount;
    }

    /**
     * Return the currency formatted local currency amount as a string
     *
     * @return Formatted local currency amount
     */
    public String getSendLocalCurrencyAmountFormatted() {
        if (sendLocalCurrencyAmount.length() > 0) {
            try {
                DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(getLocalCurrency().getLocale());
                df.setParseBigDecimal(true);
                BigDecimal bd = (BigDecimal) df.parseObject(sanitize(sendLocalCurrencyAmount));
                return currencyFormat(bd);
            } catch (ParseException e) {
                ExceptionHandler.handle(e);
            }
            return "";
        } else {
            return "";
        }
    }

    /**
     * Set the local currency amount and also update the nano amount to match
     *
     * @param localCurrencyAmount String of local currency amount from input
     */
    public void setLocalCurrencyAmount(String localCurrencyAmount) {
        this.sendLocalCurrencyAmount = sanitize(localCurrencyAmount);
        if (localCurrencyAmount.length() > 0) {
            if (localCurrencyAmount.equals(".")) {
                this.sendLocalCurrencyAmount = "0.";
            }

            // keep decimal length at 10 total
            String regex = getDecimalSeparator().equals(".") ? "\\." : ",";
            String[] split = this.sendLocalCurrencyAmount.split(regex);
            if (split.length > 1) {
                String whole = split[0];
                String decimal = split[1];
                decimal = decimal.substring(0, Math.min(decimal.length(), 2));
                this.sendLocalCurrencyAmount = whole + getDecimalSeparator() + decimal;
            }

            this.sendNanoAmount = convertLocalCurrencyToNano(this.sendLocalCurrencyAmount);
        } else {
            this.sendNanoAmount = "";
        }
        validateSendAmount();
    }

    /**
     * Convert a local currency string to a Nano string
     *
     * @param amount Local currency amount from input
     * @return String of Nano converted amount
     */
    private String convertLocalCurrencyToNano(String amount) {
        BigDecimal amountBigDecimal;
        try {
            amountBigDecimal = new BigDecimal(sanitizeNoCommas(amount));
        } catch(NumberFormatException e) {
            return amount;
        }

        if (amount.length() == 0 || amountBigDecimal.compareTo(new BigDecimal(0)) == 0) {
            return amount;
        } else {
            try {
                DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(getLocalCurrency().getLocale());
                df.setParseBigDecimal(true);
                BigDecimal bd = (BigDecimal) df.parseObject(sanitize(amount));
                return localCurrencyPrice != null ? bd
                        .divide(localCurrencyPrice, 10, BigDecimal.ROUND_HALF_UP).toString() : "0.0";
            } catch (ParseException e) {
                ExceptionHandler.handle(e);
            }
            return "";
        }
    }

    /**
     * Convert local currency to properly formatted string for the currency
     */
    private String currencyFormat(BigDecimal amount) {
        return NumberFormat.getCurrencyInstance(getLocalCurrency().getLocale()).format(amount);
    }

    /**
     * Convert local currency to properly formatted string for the currency
     */
    private String nanoFormat(String amount) {
        BigDecimal amountBigDecimal;
        try {
            amountBigDecimal = new BigDecimal(sanitizeNoCommas(amount));
        } catch(NumberFormatException e) {
            return amount;
        }

        if (amountBigDecimal.compareTo(new BigDecimal(0)) == 0) {
            return amount;
        } else {
            String decimal;
            String whole;
            String[] split = amount.split("\\.");
            if (split.length > 1) {
                // keep decimal length at 10 total
                whole = split[0];
                decimal = split[1];
                decimal = decimal.substring(0, Math.min(decimal.length(), MAX_NANO_DISPLAY_LENGTH));

                // add commas to the whole amount
                if (whole.length() > 0) {
                    DecimalFormat df = new DecimalFormat("#,###");
                    whole = df.format(new BigDecimal(sanitizeNoCommas(whole)));
                }

                amount = whole + "." + decimal;
            } else if (split.length == 1) {
                // no decimals yet, so just add commas
                DecimalFormat df = new DecimalFormat("#,###");
                amount = df.format(new BigDecimal(sanitizeNoCommas(amount)));
            }
            return amount;
        }
    }

    /**
     * Validate that the requested send amount is not greater than the account balance
     */

    private void validateSendAmount() {
        try {
            if (new BigDecimal(sanitizeNoCommas(sendNanoAmount))
                    .compareTo(new BigDecimal(sanitizeNoCommas(NumberUtil.getRawAsLongerUsableString(accountBalance.toString())))) > 0) {
                RxBus.get().post(new SendInvalidAmount());
            }
        } catch (NumberFormatException e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Get the decimal separator for the selected currency
     *
     * @return decimal separator (i.e. . or ,)
     */
    public String getDecimalSeparator() {
        NumberFormat nf = NumberFormat.getInstance(getLocalCurrency().getLocale());
        if (nf instanceof DecimalFormat) {
            DecimalFormatSymbols sym = ((DecimalFormat) nf).getDecimalFormatSymbols();
            return Character.toString(sym.getDecimalSeparator());
        }
        return ".";
    }

    public void close() {
        RxBus.get().unregister(this);
    }

    public void clear() {
        accountBalance = new BigDecimal("0.0");
        localCurrencyPrice = null;
        btcPrice = null;

        representativeAddress = null;
        frontierBlock = null;
        openBlock = null;

        blockCount = null;

        accountHistory = new ArrayList<>();

        // for sending
        sendNanoAmount = "";
        sendLocalCurrencyAmount = "";
    }

    public String getFrontierBlock() {
        if (frontierBlock == null) {
            return publicKey;
        } else {
            return frontierBlock;
        }
    }

    public void setFrontierBlock(String frontierBlock) {
        this.frontierBlock = frontierBlock;
    }

    public String getRepresentative() {
        return representativeAddress != null ? NanoUtil.publicToAddress(representativeAddress) : PreconfiguredRepresentatives.getRepresentative();
    }


    /* Bus Listeners */

    /**
     * Receive account subscribe response
     *
     * @param subscribeResponse Subscribe response
     */
    @Subscribe
    public void receiveSubscribe(SubscribeResponse subscribeResponse) {
        frontierBlock = subscribeResponse.getFrontier();
        representativeAddress = subscribeResponse.getRepresentative_block();
        openBlock = subscribeResponse.getOpen_block();
        blockCount = subscribeResponse.getBlock_count();
        if (subscribeResponse.getUuid() != null) {
            uuid = subscribeResponse.getUuid();
            if (realm != null && !realm.isClosed()) {
                realm.executeTransaction(realm -> {
                    Credentials credentials = realm.where(Credentials.class).findFirst();
                    if (credentials != null) {
                        credentials.setUuid(uuid);
                        realm.insertOrUpdate(credentials);
                    }
                });
            }
        }
        accountBalance = new BigDecimal(subscribeResponse.getBalance() != null ? subscribeResponse.getBalance() : "0.0");
        localCurrencyPrice = new BigDecimal(subscribeResponse.getPrice());
        btcPrice = new BigDecimal(subscribeResponse.getBtc());
        RxBus.get().post(new WalletSubscribeUpdate());
    }

    /**
     * Receive a history update
     *
     * @param accountHistoryResponse Account history response
     */
    @Subscribe
    public void receiveHistory(AccountHistoryResponse accountHistoryResponse) {
        accountHistory = accountHistoryResponse.getHistory();
        RxBus.get().post(new WalletHistoryUpdate());
    }

    /**
     * Receive a current price response
     *
     * @param currentPriceResponse Current price response
     */
    @Subscribe
    public void receiveCurrentPrice(CurrentPriceResponse currentPriceResponse) {
        if (currentPriceResponse.getCurrency().equals("btc")) {
            // we made a btc price request
            btcPrice = new BigDecimal(currentPriceResponse.getPrice());
        } else {
            // local currency price
            localCurrencyPrice = new BigDecimal(currentPriceResponse.getPrice());
        }
        if (currentPriceResponse.getBtc() != null) {
            btcPrice = new BigDecimal(currentPriceResponse.getBtc());
        }
        RxBus.get().post(new WalletPriceUpdate());
    }

    /**
     * Receive clear wallet
     *
     * @param walletClear Wallet Clear event
     */
    @Subscribe
    public void receiveClear(WalletClear walletClear) {
        clear();
    }
}
