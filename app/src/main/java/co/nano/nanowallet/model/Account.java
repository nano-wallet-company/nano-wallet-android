package co.nano.nanowallet.model;

import co.nano.nanowallet.util.Blake2bUtil;
import com.google.common.io.NanoBaseEncoding;
import com.google.common.primitives.Bytes;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Arrays;

public class Account {
    public static final String PREFIX_SEPARATOR = "_";

    public static TypeAdapter<Account> TYPE_ADAPTER = new TypeAdapter<Account>() {
        @Override
        public void write(JsonWriter out, Account value) throws IOException {
            out.value(value.toHumanReadable());
        }

        @Override
        public Account read(JsonReader in) throws IOException {
            return Account.fromHumanReadable(in.nextString());
        }
    };

    private static final byte[] BYTES_PADDING = new byte[]{0, 0, 0};
    private static final String STRING_PADDING = "1111";

    public static Account fromHumanReadable(String humanReadable) {
        int separatorIndex = humanReadable.indexOf(PREFIX_SEPARATOR);
        if (separatorIndex == -1) {
            throw new IllegalArgumentException("Given account string does not contain separator: " + humanReadable);
        }
        String encodedData = STRING_PADDING + humanReadable.substring(separatorIndex + PREFIX_SEPARATOR.length());
        byte[] data = NanoBaseEncoding.get().decode(encodedData);
        for (int i = 0; i < BYTES_PADDING.length; i++) {
            if (data[i] != BYTES_PADDING[i]) {
                throw new IllegalArgumentException("Given account string decodes with invalid padding: " + humanReadable);
            }
        }

        int hashIndex = BYTES_PADDING.length + Key.BYTES_LENGTH;

        byte[] rawBytes = Arrays.copyOfRange(data, BYTES_PADDING.length, hashIndex);
        byte[] hash = Arrays.copyOfRange(data, hashIndex, data.length);
        Bytes.reverse(hash);
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
        Bytes.reverse(hash);

        StringWriter writer = new StringWriter(64);
        OutputStream encodingStream = NanoBaseEncoding.get().encodingStream(writer);
        try {
            encodingStream.write(BYTES_PADDING);
            encodingStream.write(publicKeyBytes);
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

        String result = prefix.getAccountString() + PREFIX_SEPARATOR + encodedString.substring(STRING_PADDING.length());
        if (humanReadable == null) {
            humanReadable = result;
        }
        return result;
    }
}
