package co.nano.nanowallet;

/**
 * Created by admin on 12/24/2017.
 */

import java.math.BigInteger;
import java.util.Random;

import co.nano.nanowallet.util.Blake2b;
import co.nano.nanowallet.util.ED25519;


//import blake2bjava.Blake2bHasher;

public class NanoUtil {
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private final static String codeArray = "13456789abcdefghijkmnopqrstuwxyz";
    private final static char[] codeCharArray = codeArray.toCharArray();

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

    public static String generateSeed() {
        int numchars = 64;
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        while (sb.length() < numchars) {
            sb.append(Integer.toHexString(random.nextInt()));
        }
        return sb.toString().substring(0, numchars).toUpperCase();
    }

    public static String seedToPrivate(String seed) {
        byte[] seed_b = hexToBytes(seed);
        byte[] index_b = {0x00, 0x00, 0x00, 0x00};

        final Blake2b blake = Blake2b.Digest.newInstance(32);
        blake.update(seed_b);
        blake.update(index_b);
        return bytesToHex(blake.digest());
    }

    public static String privateToPublic(String private_key) {
        return bytesToHex(ED25519.publickey(hexToBytes(private_key)));
    }

    /**
     * Compute a hash to use with receive blocks
     * @param previous
     * @param source
     * @return
     */
    public static String computeReceiveHash(String previous, String source) {
        byte[] previous_b = hexToBytes(previous);
        byte[] source_b = hexToBytes(source);

        final Blake2b blake = Blake2b.Digest.newInstance(32);
        blake.update(previous_b);
        blake.update(source_b);

        return bytesToHex(blake.digest());
    }

    /**
     * Compute hash to use to generate a send work block
     * @param previous Previous transation
     * @param destination Destination address
     * @param balance Raw NANO balance
     * @return String of hash
     */
    public static String computeSendHash(String previous, String destination, String balance) {
        byte[] previous_b = hexToBytes(previous);
        byte[] destination_b = hexStringToByteArray(destination);
        byte[] balance_b = hexToBytes(balance);

        final Blake2b blake = Blake2b.Digest.newInstance(32);
        blake.update(previous_b);
        blake.update(destination_b);
        blake.update(balance_b);

        return bytesToHex(blake.digest());
    }

    public static String sign(String private_key, String public_key, String data) {

        return bytesToHex(ED25519.signature(hexToBytes(data), hexToBytes(public_key), hexToBytes(private_key)));
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static void reverse(byte[] array) {
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

    public static String publicToAddress(String public_key) {
        //8933B4083FE0E42A97FF0B7E16B9B2CEF93D31318700B328D6CF6CE931BBF8D4 > xrb_34bmpi65zr967cdzy4uy4twu7mqs9nrm53r1penffmuex6ruqy8nxp7ms1h1
        //xrb_ 34bmpi65zr967cdzy4uy4twu7mqs9nrm53r1penffmuex6ruqy8n xp7ms1h1
        byte[] bytePublic = new byte[32];
        bytePublic = NanoUtil.hexStringToByteArray(public_key);
        String encodedAddress = encode(public_key);

        final Blake2b blake = Blake2b.Digest.newInstance(5);
        blake.update(bytePublic);
        byte[] check_b = blake.digest();

        reverse(check_b);

        StringBuilder resultAddress = new StringBuilder();
        resultAddress.insert(0, "xrb_");
        resultAddress.append(encodedAddress);
        resultAddress.append(encode(NanoUtil.bytesToHex(check_b)));

        return resultAddress.toString();

    }

    public static String addressToPublic(String encoded_address) {
        //xrb_34bmpi65zr967cdzy4uy4twu7mqs9nrm53r1penffmuex6ruqy8nxp7ms1h1 > 8933B4083FE0E42A97FF0B7E16B9B2CEF93D31318700B328D6CF6CE931BBF8D4
        String data = encoded_address.substring(4, 56);
        String checksum = encoded_address.substring(56, encoded_address.length());
        //Log.i("addressToPublic", "data "+data);
        //Log.i("addressToPublic", "checksum "+checksum);

        byte[] data_b = NanoUtil.hexStringToByteArray(decode(data));
        byte[] checksum_b = NanoUtil.hexStringToByteArray(decode(checksum));

        final Blake2b blake = Blake2b.Digest.newInstance(5);
        blake.update(data_b);
        byte[] verify_b = blake.digest();
        reverse(verify_b);

        //Log.i("addressToPublic", "data_b "+NanoUtil.bytesToHex(data_b));
        //Log.i("addressToPublic", "checksum_b "+NanoUtil.bytesToHex(checksum_b));
        //Log.i("addressToPublic", "verify_b "+NanoUtil.bytesToHex(verify_b));

        return NanoUtil.bytesToHex(data_b);
    }

    public static String encode(String hex_data) {
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

        //System.out.println(data.toString());
        StringBuilder output = new StringBuilder();
        int slice = data.length() / 5;
        for (int this_slice = 0; this_slice < slice; this_slice++) {
            //System.out.println(data.substring(this_slice*5,this_slice*5+5));
            //String binval = data.substring(this_slice*5,this_slice*5+5).toString();
            //int i = Integer.parseInt(binval,2);
            //System.out.println(codeArray[i]);
            output.append(codeCharArray[Integer.parseInt(data.substring(this_slice * 5, this_slice * 5 + 5).toString(), 2)]);
        }
        return output.toString();
    }

    public static String decode(String data) {
        StringBuilder bits = new StringBuilder();
        for (int i = 0; i < data.length(); i++) {
            int index = codeArray.indexOf(data.substring(i, i + 1).charAt(0));
            //System.out.println(data.substring(i,i+1).charAt(0));
            //System.out.println(Integer.toBinaryString(0x20 | index).substring(1));
            bits.append(Integer.toBinaryString(0x20 | index).substring(1));
        }
        //System.out.println(bits.toString());
        return new BigInteger(bits.toString(), 2).toString(16);
    }

    //public static encodeToBase32Custom(byte[] data)
    //{
    //
    //}


}