package co.nano.nanowallet.util;

import org.libsodium.jni.NaCl;
import org.libsodium.jni.Sodium;

import java.util.Arrays;

public class Blake2bUtil {
    private static final byte[] EMPTY_ARRAY = new byte[0];

    static {
        NaCl.sodium();
    }

    public static byte[] hash(int hashBytesLength, byte[] data, byte[]... dataChunks) {
        byte[] state = new byte[Sodium.crypto_generichash_statebytes()];
        Sodium.crypto_generichash_blake2b_init(state, EMPTY_ARRAY, 0, hashBytesLength);

        Sodium.crypto_generichash_blake2b_update(state, data, data.length);
        for (byte[] dataChunk : dataChunks) {
            Sodium.crypto_generichash_blake2b_update(state, dataChunk, dataChunk.length);
        }

        byte[] hashBytes = new byte[hashBytesLength];
        Sodium.crypto_generichash_blake2b_final(state, hashBytes, hashBytesLength);
        return hashBytes;
    }

    public static boolean verify(byte[] hash, byte[] data, byte[]... dataChunks) {
        byte[] newHash = hash(hash.length, data, dataChunks);
        return Arrays.equals(hash, newHash);
    }

    private Blake2bUtil() {
    }
}
