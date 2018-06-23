package co.nano.nanowallet.model;

import com.google.common.base.Ascii;
import com.google.common.io.BaseEncoding;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Key {
    public static final int BYTES_LENGTH = 32;
    public static final BaseEncoding HEX_ENCONDING = BaseEncoding.base16().upperCase();

    public static Key parseHexString(String hexString) {
        byte[] bytes = HEX_ENCONDING.decode(Ascii.toUpperCase(hexString));
        return new Key(bytes);
    }

    protected final byte[] rawBytes;

    public Key(byte[] rawBytes) {
        if (rawBytes.length != BYTES_LENGTH) {
            throw new IllegalArgumentException("Keys are expected to be " + BYTES_LENGTH + " bytes long, got " + rawBytes.length);
        }
        this.rawBytes = Arrays.copyOf(rawBytes, rawBytes.length);
    }

    public ByteBuffer getBytes() {
        return ByteBuffer.wrap(rawBytes).asReadOnlyBuffer();
    }

    public String toHexString() {
        return HEX_ENCONDING.encode(rawBytes);
    }
}
