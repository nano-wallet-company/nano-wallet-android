package co.nano.nanowallet.model;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Address class
 */

public class Address implements Serializable {
    private String value;

    public static final List<Character> VALID_ADDRESS_CHARACTERS = Arrays.asList('a','b','c','d','e','f','g','h','i','j','k','m','n','o','p','q','r','s','t','u','w','x','y','z','1','3','4','5','6','7','8','9','_');

    public Address() {
    }

    public Address(String value) {
        this.value = value;
    }

    public boolean hasXrbAddressFormat() {
        return value.contains("xrb_");
    }

    public boolean hasNanoAddressFormat() {
        return value.contains("nano_");
    }

    public String getShortString() {
        int frontStartIndex = 0;
        int frontEndIndex = hasXrbAddressFormat() ? 9 : 10;
        int backStartIndex = value.length() - 5;
        StringBuilder sb = new StringBuilder();
        return sb.append(value.substring(frontStartIndex, frontEndIndex))
                .append("...")
                .append(value.substring(backStartIndex, value.length())).toString();
    }

    public Spannable getColorizedShortSpannable() {
        Spannable s = new SpannableString(getShortString());
        int frontStartIndex = 0;
        int frontEndIndex = hasXrbAddressFormat() ? 9 : 10;
        int backStartIndex = s.length() - 5;

        // colorize the string
        s.setSpan(new ForegroundColorSpan(Color.parseColor("#4a90e2")), frontStartIndex, frontEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new ForegroundColorSpan(Color.parseColor("#e1990e")), backStartIndex, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return s;
    }

    public String getAddress() {
        return value;
    }

    public String getAddressWithoutPrefix() {
        return value.replace("xrb_", "");
    }

    public boolean isValidAddress() {
        if (getAddress().length() != 64) {
            return false;
        }
        boolean isMatch = true;
        for (int i = 0; i < value.length() && isMatch; i++) {
            char letter = value.toLowerCase().charAt(i);
            if (!VALID_ADDRESS_CHARACTERS.contains(letter)) {
                isMatch = false;
            }
        }
        return isMatch;
    }
}
