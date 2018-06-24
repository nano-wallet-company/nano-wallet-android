package co.nano.nanowallet.model;

import co.nano.nanowallet.NanoUtil;
import co.nano.nanowallet.util.Blake2bUtil;

import java.util.Arrays;

public class Account {
    public static final String PREFIX_SEPARATOR = "_";

    public static Account fromHumanReadable(String humanReadable) {
        int separatorIndex = humanReadable.indexOf(PREFIX_SEPARATOR);
        if (separatorIndex == -1) {
            throw new IllegalArgumentException("Given account string does not contain separator: " + humanReadable);
        }
        String encodedData = humanReadable.substring(separatorIndex + PREFIX_SEPARATOR.length());
        byte[] data = NanoUtil.hexToBytes(NanoUtil.decode(encodedData));

        int hashIndex = Key.BYTES_LENGTH;
        byte[] rawBytes = Arrays.copyOfRange(data, 0, hashIndex);
        byte[] hash = Arrays.copyOfRange(data, hashIndex, data.length);
        NanoUtil.reverse(hash);
        if (!Blake2bUtil.verify(hash, rawBytes)) {
            throw new IllegalArgumentException("Given account string has invalid hash: " + humanReadable);
        }
        return new Account(Key.fromBytes(rawBytes), humanReadable);
    }

    public static Account fromHexString(String hexString) {
        return new Account(Key.fromHexString(hexString), null);
    }

    private final Key publicKey;
    private String humanReadable;

    private Account(Key publicKey, String humanReadable) {
        this.publicKey = publicKey;
        this.humanReadable = humanReadable;
    }

    public Key getPublicKey() {
        return publicKey;
    }

    public String toHexString() {
        return getPublicKey().toHexString();
    }

    public String toHumanReadable() {
        if (humanReadable == null) {
            humanReadable = toHumanReadable(Prefix.DEFAULT);
        }
        return humanReadable;
    }

    public String toHumanReadable(Prefix prefix) {
        if (humanReadable != null && humanReadable.startsWith(prefix.getAccountString())) {
            return humanReadable;
        }
        byte[] publicKeyBytes = getPublicKey().getBytesArrayCopy();

        byte[] hash = Blake2bUtil.hash(5, publicKeyBytes);
        NanoUtil.reverse(hash);

        String fullHexString = NanoUtil.bytesToHex(publicKeyBytes) + NanoUtil.bytesToHex(hash);
        String encodedString = NanoUtil.encode(fullHexString);

        String result = prefix.getAccountString() + PREFIX_SEPARATOR + encodedString;
        if (humanReadable == null) {
            humanReadable = result;
        }
        return result;
    }

    @Override
    public String toString() {
        return toHumanReadable();
    }
}
