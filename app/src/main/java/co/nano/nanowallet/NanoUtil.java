package co.nano.nanowallet;

/**
 * Created by admin on 12/24/2017.
 */

import org.libsodium.jni.NaCl;
//bimport org.libsodium.jni.crypto

import java.math.BigInteger;

//import blake2bjava.Blake2bHasher;

class NanoUtil {
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private final static String codeArray = "13456789abcdefghijkmnopqrstuwxyz";
    private final static char[] codeCharArray = codeArray.toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexToBytes(String hex) {
        hex = hex.length()%2 != 0?"0"+hex:hex;

        byte[] b = new byte[hex.length() / 2];

        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(hex.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

    public static String seedToPrivate(String seed) {
        //Blake2bConfig config = new Blake2bConfig();
        //config.setOutputSizeInBits(256);
        //Blake2bHasher digest = new Blake2bHasher(null);
        byte[] seed_b = hexToBytes(seed);
        byte[] index_b = {0x00, 0x00, 0x00, 0x00};

        //digest.Update(seed_b, 0, seed_b.length);
        //digest.Update(index_b,0,index_b.length);

        byte[] state = new byte[361];
        //libs = NaCl.sodium();
        NaCl.sodium().crypto_generichash_blake2b_init(state, new byte[0], 0, 32 );
        NaCl.sodium().crypto_generichash_blake2b_update(state, seed_b, seed_b.length);
        NaCl.sodium().crypto_generichash_blake2b_update(state, index_b, index_b.length);
        byte[] finish = new byte[32];
        NaCl.sodium().crypto_generichash_blake2b_final(state, finish, finish.length);
        return bytesToHex(finish);
    }

    public static String privateToPublic(String private_key) {
        byte[] public_b = new byte[32];
        byte[] private_b = NanoUtil.hexToBytes(private_key);
        public_b = ED25519.publicKey(private_b);
        return bytesToHex(public_b);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
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
        byte[] bytePublic = NanoUtil.hexStringToByteArray(public_key);
        String encodedAddress = encode(public_key);
        NanoUtil.reverse(bytePublic);

        //Blake2bConfig config = new Blake2bConfig();
        //config.setOutputSizeInBits(40);
        //Blake2bHasher context = new Blake2bHasher(config);
        //context.Update(bytePublic,0,bytePublic.length);
        //byte[] check_b = new byte[5];
        //check_b = context.Finish();

        byte[] check_b = new byte[5];
        NaCl.sodium().crypto_generichash_blake2b(check_b, 5, bytePublic, bytePublic.length, new byte[0], 0);

        StringBuilder resultAddress = new StringBuilder();

        resultAddress.insert(0,"xrb_");
        resultAddress.append(encodedAddress);
        resultAddress.append(encode(NanoUtil.bytesToHex(check_b)));

        return resultAddress.toString();

    }

    public static String encode(String hex_data) {
        String bits = new BigInteger(hex_data, 16).toString(2);

        StringBuilder data = new StringBuilder();
        data.insert(0,bits);
        while (data.length() % 5 != 0)
        {
            data.insert(0,'0');
        }

        //System.out.println(data.toString());
        StringBuilder output = new StringBuilder();
        int slice = data.length() / 5;
        for (int this_slice = 0; this_slice < slice; this_slice++) {
            //System.out.println(data.substring(this_slice*5,this_slice*5+5));
            //String binval = data.substring(this_slice*5,this_slice*5+5).toString();
            //int i = Integer.parseInt(binval,2);
            //System.out.println(codeArray[i]);
            output.append(codeCharArray[Integer.parseInt(data.substring(this_slice*5,this_slice*5+5).toString(),2)]);
        }
        return output.toString();
    }

    public static String decode(String data) {
        StringBuilder bits = new StringBuilder();
        for (int i=0; i<data.length(); i++){
            int index = codeArray.indexOf(data.substring(i,i+1).charAt(0));
            //System.out.println(data.substring(i,i+1).charAt(0));
            //System.out.println(Integer.toBinaryString(0x20 | index).substring(1));
            bits.append(Integer.toBinaryString(0x20 | index).substring(1));
        }
        //System.out.println(bits.toString());
        return new BigInteger(bits.toString(),2).toString(16);
    }

    //public static encodeToBase32Custom(byte[] data)
    //{
    //
    //}



}