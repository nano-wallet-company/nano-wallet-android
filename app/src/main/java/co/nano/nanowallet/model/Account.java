package co.nano.nanowallet.model;

import co.nano.nanowallet.util.Blake2bUtil;
import com.google.common.io.NanoBaseEncoding;
import com.google.common.primitives.Bytes;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Arrays;

public class Account extends Key {
    public static final String PREFIX_SEPARATOR = "_";

    private static final byte[] BYTES_PADDING = new byte[]{0, 0, 0};
    private static final String STRING_PADDING = "1111";

    public static Account parseHumanReadable(String accountString) {
        int separatorIndex = accountString.indexOf(PREFIX_SEPARATOR);
        if (separatorIndex == -1) {
            throw new IllegalArgumentException("Given account string does not contain separator: " + accountString);
        }
        String encodedData = STRING_PADDING + accountString.substring(separatorIndex + PREFIX_SEPARATOR.length());
        byte[] data = NanoBaseEncoding.get().decode(encodedData);
        for (int i = 0; i < BYTES_PADDING.length; i++) {
            if (data[i] != BYTES_PADDING[i]) {
                throw new IllegalArgumentException("Given account string decodes with invalid padding: " + accountString);
            }
        }

        int hashIndex = BYTES_PADDING.length + Key.BYTES_LENGTH;

        byte[] rawBytes = Arrays.copyOfRange(data, BYTES_PADDING.length, hashIndex);
        byte[] hash = Arrays.copyOfRange(data, hashIndex, data.length);
        Bytes.reverse(hash);
        if (!Blake2bUtil.verify(hash, rawBytes)) {
            throw new IllegalArgumentException("Given account string has invalid hash: " + accountString);
        }
        return new Account(rawBytes);
    }

    public static Account parseHexString(String hexString) {
        return new Account(Key.parseHexString(hexString).rawBytes);
    }

    public Account(byte[] rawBytes) {
        super(rawBytes);
    }

    public String toHumanReadable() {
        return toHumanReadable(Prefix.DEFAULT);
    }

    public String toHumanReadable(Prefix prefix) {
        byte[] hash = Blake2bUtil.hash(5, rawBytes);
        Bytes.reverse(hash);

        StringWriter writer = new StringWriter(64);
        OutputStream encodingStream = NanoBaseEncoding.get().encodingStream(writer);
        try {
            encodingStream.write(BYTES_PADDING);
            encodingStream.write(rawBytes);
            encodingStream.write(hash);
            encodingStream.flush();
        } catch (IOException ex) {
            // should not happen, as we're writing to memory without actual IO
            throw new RuntimeException(ex);
        }

        String encodedString = writer.toString();
        if (!encodedString.startsWith(STRING_PADDING)) {
            throw new IllegalArgumentException("Calculated encoded string has invalid padding: " + encodedString);
        }

        return prefix.getAccountString() + PREFIX_SEPARATOR + encodedString.substring(STRING_PADDING.length());
    }
}
