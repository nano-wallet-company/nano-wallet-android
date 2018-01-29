package co.nano.nanowallet.model;

import java.util.Arrays;
import java.util.List;

/**
 * Address class
 */

public class Address {
    private String value;
    public static final List<Character> VALID_SEED_CHARACTERS = Arrays.asList('a','b','c','d','e','f','0','1','2','3','4','5','6','7','8','9');
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

    public String getLongAddress() {
        return value;
    }

    public static boolean isValidSeed(String seed) {
        if (seed.length() != 64) {
            return false;
        }
        boolean isMatch = true;
        for (int i = 0; i < seed.length() && isMatch; i++) {
            char letter = seed.toLowerCase().charAt(i);
            if (!VALID_SEED_CHARACTERS.contains(letter)) {
                isMatch = false;
            }
        }
        return isMatch;
    }

    public static boolean isValidAddress(String seed) {
        boolean isMatch = true;
        for (int i = 0; i < seed.length() && isMatch; i++) {
            char letter = seed.toLowerCase().charAt(i);
            if (!VALID_ADDRESS_CHARACTERS.contains(letter)) {
                isMatch = false;
            }
        }
        return isMatch;
    }
}
