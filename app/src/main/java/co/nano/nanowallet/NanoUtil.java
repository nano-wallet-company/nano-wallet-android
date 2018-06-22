package co.nano.nanowallet;

/*
  Utilities for crypto functions
 */

import co.nano.nanowallet.util.SecureRandomUtil;
import org.libsodium.jni.NaCl;
import org.libsodium.jni.Sodium;

import java.math.BigInteger;
import java.security.SecureRandom;

public class NanoUtil {
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private final static String codeArray = "13456789abcdefghijkmnopqrstuwxyz";
    private final static char[] codeCharArray = codeArray.toCharArray();


    /**
     * Generate a new Wallet Seed
     *
     * @return Wallet Seed
     */
    public static String generateSeed() {
        int numchars = 64;
        SecureRandom random = SecureRandomUtil.secureRandom();
        StringBuilder sb = new StringBuilder();
        while (sb.length() < numchars) {
            sb.append(Integer.toHexString(random.nextInt()));
        }
        return sb.toString().substring(0, numchars).toUpperCase();
    }

    /**
     * Convert a wallet seed to private key
     *
     * @param seed Wallet seed
     * @return private key
     */
    public static String seedToPrivate(String seed) {
        Sodium sodium = NaCl.sodium();
        byte[] state = new byte[Sodium.crypto_generichash_statebytes()];
        byte[] key = new byte[Sodium.crypto_generichash_keybytes()];

        byte[] seed_b = NanoUtil.hexToBytes(seed);
        byte[] index_b = {0x00, 0x00, 0x00, 0x00};
        byte[] output = new byte[32];

        Sodium.crypto_generichash_blake2b_init(state, key, 0, 32);
        Sodium.crypto_generichash_blake2b_update(state, seed_b, seed_b.length);
        Sodium.crypto_generichash_blake2b_update(state, index_b, index_b.length);
        Sodium.crypto_generichash_blake2b_final(state, output, output.length);

        return bytesToHex(output);
    }

    /**
     * Convert a private key to a public key
     *
     * @param private_key private key
     * @return public key
     */
    public static String privateToPublic(String private_key) {
        Sodium sodium = NaCl.sodium();
        byte[] public_key_b = new byte[Sodium.crypto_generichash_blake2b_bytes()];
        byte[] private_key_b = hexToBytes(private_key);

        Sodium.crypto_sign_ed25519_sk_to_pk(public_key_b, private_key_b);

        return bytesToHex(public_key_b);
    }

    /**
     * Compute hash to use to generate an open work block
     *
     * @param source         Source address
     * @param representative Representative address
     * @param account        Account address
     * @return Open Hash
     */
    public static String computeOpenHash(String source, String representative, String account) {
        Sodium sodium = NaCl.sodium();
        byte[] state = new byte[Sodium.crypto_generichash_statebytes()];
        byte[] key = new byte[Sodium.crypto_generichash_keybytes()];

        byte[] source_b = hexToBytes(source);
        byte[] representative_b = hexToBytes(representative);
        byte[] account_b = hexToBytes(account);
        byte[] output = new byte[32];

        Sodium.crypto_generichash_blake2b_init(state, key, 0, 32);
        Sodium.crypto_generichash_blake2b_update(state, source_b, source_b.length);
        Sodium.crypto_generichash_blake2b_update(state, representative_b, representative_b.length);
        Sodium.crypto_generichash_blake2b_update(state, account_b, account_b.length);
        Sodium.crypto_generichash_blake2b_final(state, output, output.length);

        return bytesToHex(output);
    }

    /**
     * Compute hash to use to generate a receive work block
     *
     * @param previous Previous transation
     * @param source   Source address
     * @return String of hash
     */
    public static String computeReceiveHash(String previous, String source) {
        Sodium sodium = NaCl.sodium();
        byte[] state = new byte[Sodium.crypto_generichash_statebytes()];
        byte[] key = new byte[Sodium.crypto_generichash_keybytes()];

        byte[] previous_b = hexToBytes(previous);
        byte[] source_b = hexToBytes(source);
        byte[] output = new byte[32];

        Sodium.crypto_generichash_blake2b_init(state, key, 0, 32);
        Sodium.crypto_generichash_blake2b_update(state, previous_b, previous_b.length);
        Sodium.crypto_generichash_blake2b_update(state, source_b, source_b.length);
        Sodium.crypto_generichash_blake2b_final(state, output, output.length);

        return bytesToHex(output);
    }

    /**
     * Compute hash to use to generate a send work block
     *
     * @param previous    Previous transation
     * @param destination Destination address
     * @param balance     Raw NANO balance
     * @return String of hash
     */
    public static String computeSendHash(String previous, String destination, String balance) {
        Sodium sodium = NaCl.sodium();
        byte[] state = new byte[Sodium.crypto_generichash_statebytes()];
        byte[] key = new byte[Sodium.crypto_generichash_keybytes()];

        byte[] previous_b = hexToBytes(previous);
        byte[] destination_b = hexToBytes(destination);
        byte[] balance_b = hexToBytes(balance);
        byte[] output = new byte[32];

        Sodium.crypto_generichash_blake2b_init(state, key, 0, 32);
        Sodium.crypto_generichash_blake2b_update(state, previous_b, previous_b.length);
        Sodium.crypto_generichash_blake2b_update(state, destination_b, destination_b.length);
        Sodium.crypto_generichash_blake2b_update(state, balance_b, balance_b.length);
        Sodium.crypto_generichash_blake2b_final(state, output, output.length);

        return bytesToHex(output);
    }

    /**
     * Compute hash for a universal (state) block
     * @param account This account's xrb_ address.
     * @param previous Previous head block on account; 0 if open block.
     * @param representative Representative xrb_ address.
     * @param balance Resulting balance
     * @param link Multipurpose Field
     * @return String of hash
     */
    public static String computeStateHash(String account,
                                         String previous,
                                         String representative,
                                         String balance,
                                         String link) {
        Sodium sodium = NaCl.sodium();

        byte[] temp = hexToBytes("6");
        byte[] STATE_BLOCK_PREAMBLE = new byte[32];
        System.arraycopy(temp, 0, STATE_BLOCK_PREAMBLE, 32 - temp.length, temp.length);

        byte[] state = new byte[Sodium.crypto_generichash_statebytes()];
        byte[] key = new byte[Sodium.crypto_generichash_keybytes()];

        byte[] account_b = hexToBytes(account);
        //byte[] previous_b = hexToBytes(previous);//String.format(Locale.US, "%032d", new BigInteger(previous)));
        byte[] previous_temp = hexToBytes(previous);
        byte[] previous_b = new byte[32];
        System.arraycopy(previous_temp, 0, previous_b, 32 - previous_temp.length, previous_temp.length);
        byte[] representative_b = hexToBytes(representative);
        byte[] balance_b = hexToBytes(balance);
        byte[] link_b = hexToBytes(link);
        byte[] output = new byte[32];

        Sodium.crypto_generichash_blake2b_init(state, key, 0, 32);
        Sodium.crypto_generichash_blake2b_update(state, STATE_BLOCK_PREAMBLE, STATE_BLOCK_PREAMBLE.length);
        Sodium.crypto_generichash_blake2b_update(state, account_b, account_b.length);
        Sodium.crypto_generichash_blake2b_update(state, previous_b, previous_b.length);
        Sodium.crypto_generichash_blake2b_update(state, representative_b, representative_b.length);
        Sodium.crypto_generichash_blake2b_update(state, balance_b, balance_b.length);
        Sodium.crypto_generichash_blake2b_update(state, link_b, link_b.length);
        Sodium.crypto_generichash_blake2b_final(state, output, output.length);

        return bytesToHex(output);
    }

    /**
     * Sign a message with a private key
     *
     * @param private_key Private Key
     * @param data        Message
     * @return Signed message
     */
    public static String sign(String private_key, String data) {
        Sodium sodium = NaCl.sodium();
        byte[] data_b = hexToBytes(data);
        byte[] private_key_b = hexToBytes(private_key);

        byte[] signature = new byte[Sodium.crypto_sign_bytes()];
        int[] signature_len = new int[1];

        Sodium.crypto_sign_ed25519_detached(signature, signature_len, data_b, data_b.length, private_key_b);
        return bytesToHex(signature);
    }

    /**
     * Convert a Public Key to an Address
     *
     * @param public_key Public Key
     * @return xrb address
     */
    public static String publicToAddress(String public_key) {
        Sodium sodium = NaCl.sodium();
        byte[] bytePublic = NanoUtil.hexStringToByteArray(public_key);
        String encodedAddress = encode(public_key);

        byte[] state = new byte[Sodium.crypto_generichash_statebytes()];
        byte[] key = new byte[Sodium.crypto_generichash_keybytes()];
        byte[] check_b = new byte[5];

        Sodium.crypto_generichash_blake2b_init(state, key, 0, 5);
        Sodium.crypto_generichash_blake2b_update(state, bytePublic, bytePublic.length);
        Sodium.crypto_generichash_blake2b_final(state, check_b, check_b.length);

        reverse(check_b);

        StringBuilder resultAddress = new StringBuilder();
        resultAddress.insert(0, "xrb_");
        resultAddress.append(encodedAddress);
        resultAddress.append(encode(NanoUtil.bytesToHex(check_b)));

        return resultAddress.toString();

    }

    /**
     * Convert an address to a public key
     *
     * @param encoded_address encoded Address
     * @return Public Key
     */
    public static String addressToPublic(String encoded_address) {
        Sodium sodium = NaCl.sodium();
        String data = encoded_address.substring(4, 56);
        byte[] data_b = NanoUtil.hexStringToByteArray(decode(data));

        byte[] state = new byte[Sodium.crypto_generichash_statebytes()];
        byte[] key = new byte[Sodium.crypto_generichash_keybytes()];
        byte[] verify_b = new byte[5];

        Sodium.crypto_generichash_blake2b_init(state, key, 0, 5);
        Sodium.crypto_generichash_blake2b_update(state, data_b, data_b.length);
        Sodium.crypto_generichash_blake2b_final(state, verify_b, verify_b.length);

        reverse(verify_b);

        // left pad byte array with zeros
        StringBuilder pk = new StringBuilder(NanoUtil.bytesToHex(data_b));
        while (pk.length() < 64) {
            pk.insert(0, "0");
        }

        return pk.toString();
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexToBytes(String hex) {
        hex = hex.length() % 2 != 0 ? "0" + hex : hex;

        byte[] b = new byte[hex.length() / 2];

        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(hex.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        // length of string must be divisible by 2
        if (len % 2 > 0) {
            s = "0" + s;
            len = s.length();
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private static void reverse(byte[] array) {
        if (array == null) {
            return;
        }
        int i = 0;
        int j = array.length - 1;
        byte tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

    private static String encode(String hex_data) {
        StringBuilder bits = new StringBuilder();
        bits.insert(0, new BigInteger(hex_data, 16).toString(2));
        while (bits.length() < hex_data.length() * 4) {
            bits.insert(0, '0');
        }

        StringBuilder data = new StringBuilder();
        data.insert(0, bits.toString());
        while (data.length() % 5 != 0) {
            data.insert(0, '0');
        }

        StringBuilder output = new StringBuilder();
        int slice = data.length() / 5;
        for (int this_slice = 0; this_slice < slice; this_slice++) {
            output.append(codeCharArray[Integer.parseInt(data.substring(this_slice * 5, this_slice * 5 + 5), 2)]);
        }
        return output.toString();
    }

    private static String decode(String data) {
        StringBuilder bits = new StringBuilder();
        for (int i = 0; i < data.length(); i++) {
            int index = codeArray.indexOf(data.substring(i, i + 1).charAt(0));
            bits.append(Integer.toBinaryString(0x20 | index).substring(1));
        }
        return new BigInteger(bits.toString(), 2).toString(16);
    }

}
