package co.nano.nanowallet.model;

import android.graphics.Color;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * Address class
 */
public class Address implements Serializable {
    public static final String SHORT_SEPARATOR = "...";
    public static final int SHORT_CHAR_COUNT = 5;

    public static Address fromAddressString(String addressString) {
        String[] split = addressString.split(":");

        if (split.length > 1) {
            Uri uri = Uri.parse(split[1]);
            Account account = Account.fromHumanReadable(uri.getPath());
            String amountString = uri.getQueryParameter("amount");
            RawAmount rawAmount = amountString == null ? null : RawAmount.fromString(amountString);
            return new Address(account, rawAmount);
        } else {
            return new Address(Account.fromHumanReadable(addressString), null);
        }
    }

    private final Account account;
    private final RawAmount rawAmount;

    public Address(Account account, @Nullable RawAmount rawAmount) {
        if (account == null) {
            throw new IllegalArgumentException("account cannot be null");
        }
        this.account = account;
        this.rawAmount = rawAmount;
    }

    public String getShortString() {
        String accountString = getAccount().toHumanReadable();
        int frontStartIndex = 0;
        int frontEndIndex = accountString.indexOf(Account.PREFIX_SEPARATOR) + Account.PREFIX_SEPARATOR.length() + SHORT_CHAR_COUNT;
        int backStartIndex = accountString.length() - SHORT_CHAR_COUNT;
        return accountString.substring(frontStartIndex, frontEndIndex) +
                SHORT_SEPARATOR +
                accountString.substring(backStartIndex);
    }

    public Spannable getColorizedShortSpannable() {
        String shortString = getShortString();
        Spannable spannable = new SpannableString(shortString);
        int frontStartIndex = 0;
        int frontEndIndex = shortString.indexOf(SHORT_SEPARATOR);
        int backStartIndex = frontEndIndex + SHORT_SEPARATOR.length();

        // colorize the string
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#4a90e2")), frontStartIndex, frontEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#e1990e")), backStartIndex, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    @Nonnull
    public Account getAccount() {
        return account;
    }

    @Nullable
    public RawAmount getRawAmount() {
        return rawAmount;
    }
}
