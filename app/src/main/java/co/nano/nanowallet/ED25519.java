package co.nano.nanowallet;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.Security;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import blake2bjava.Blake2b;
import blake2bjava.Blake2bConfig;
import blake2bjava.Blake2bCore;
import blake2bjava.Blake2bHasher;

//import com.rfksystems.blake2b.security.*;

//import static javafx.scene.input.KeyCode.L;
//import static javafx.scene.input.KeyCode.Q;

/* Written by k3d3
 * Released to the public domain
 *
 * Modifications and speed improvements by Mick Michalski
 */

public class ED25519 {
    static final int b = 256;
    static final BigInteger q = new BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819949"); // 2^255-19 (the curve)
    static final BigInteger qm2 = new BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819947");
    //static final BigInteger qp3 = new BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819952");
    static final BigInteger qp3div8 = new BigInteger("7237005577332262213973186563042994240829374041602535252466099000494570602494"); // qp3 / 8
    static final BigInteger l = new BigInteger("7237005577332262213973186563042994240857116359379907606001950938285454250989");
    static final BigInteger d = new BigInteger("-4513249062541557337682894930092624173785641285191125241628941591882900924598840740");
    static final BigInteger I = new BigInteger("19681161376707505956807079304988542015446066515923890162744021073123829784752");
    //	static final BigInteger By = new BigInteger("46316835694926478169428394003475163141307993866256225615783033603165251855960");
//	static final BigInteger Bx = new BigInteger("15112221349535400772501151409588531511454012693041857206046113283949847762202");
//	static final BigInteger[] B = {Bx.mod(q),By.mod(q)};
    static final BigInteger[] B = {
            new BigInteger("15112221349535400772501151409588531511454012693041857206046113283949847762202"),
            new BigInteger("46316835694926478169428394003475163141307993866256225615783033603165251855960")
    };
    static final BigInteger un = new BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819967");
    static final BigInteger two = BigInteger.valueOf(2);
//	static final BigInteger eight = BigInteger.valueOf(8);

    /** Generate a public key from a secret key
     *
     * @param secretKey The secret key to generate a public key from
     *
     * @return The public key for this secret key
     */
    static public byte[] publicKey(byte[] secretKey) {
        byte[] h = hash(secretKey);
        BigInteger a = two.pow(b-2);
        for (int i=3;i<(b-2);i++) {
            BigInteger apart = two.pow(i).multiply(BigInteger.valueOf(bit(h,i)));
            a = a.add(apart);
        }
        BigInteger[] A = scalarMult(B,a);
        return encodePoint(A);
    }

    /** Sign a message
     *
     * @param message The message to sign
     * @param secretKey The secret key to sign with
     * @param publicKey The public key paired with the secret key
     *
     * @return The signature for the message
     */
    static public byte[] sign(byte[] message, byte[] secretKey, byte[] publicKey) {
        byte[] h = hash(secretKey);
        BigInteger a = two.pow(b-2);
        for (int i=3;i<(b-2);i++) {
            a = a.add(two.pow(i).multiply(BigInteger.valueOf(bit(h,i))));
        }
        ByteBuffer rsub = ByteBuffer.allocate((b/8)+message.length);
        rsub.put(h, b/8, b/4-b/8).put(message);
        BigInteger r = hint(rsub.array());
        BigInteger[] R = scalarMult(B,r);
        ByteBuffer Stemp = ByteBuffer.allocate(32+publicKey.length+message.length);
        Stemp.put(encodePoint(R)).put(publicKey).put(message);
        BigInteger S = r.add(hint(Stemp.array()).multiply(a)).mod(l);
        ByteBuffer out = ByteBuffer.allocate(64);
        out.put(encodePoint(R)).put(encodeInt(S));
        return out.array();
    }

    /** Check that an ED25519 signature is valid.
     *
     * @param signature The message signature
     * @param message The message that was signed
     * @param publicKey The public part of the key that was used to sign the message
     *
     * @return True if the message and public key match the signature, otherwise false
     */
    static public boolean checkValid(byte[] signature, byte[] message, byte[] publicKey) {
        if (signature.length != 64) return false;
        if (publicKey.length != 32) return false;
        byte[] Rbyte = new byte[32];
        System.arraycopy(signature, 0, Rbyte, 0, 32);
        BigInteger[] R;
        BigInteger[] A;
        try {
            R = decodePoint(Rbyte);
            A = decodePoint(publicKey);
        } catch (Exception e) {
            return false;
        }
        byte[] Sbyte = new byte[32];
        System.arraycopy(signature, 32, Sbyte, 0, 32);
        BigInteger S = decodeInt(Sbyte);
        ByteBuffer Stemp = ByteBuffer.allocate(32+publicKey.length+message.length);
        Stemp.put(encodePoint(R)).put(publicKey).put(message);
        BigInteger h = hint(Stemp.array());
        BigInteger[] ra = scalarMult(B,S);
        BigInteger[] rb = edwards(R, scalarMult(A,h));
        return !(!ra[0].equals(rb[0]) || !ra[1].equals(rb[1])); // Constant time comparison
    }

    /** Create a SHA-512 hash.
     *
     * @param message The message to hash
     * @return The 512 bit hashed value
     */
    static public byte[] hash(byte[] message) {
        //Blake2bConfig config = new Blake2bConfig();
        //config.setOutputSizeInBits(256);
        //Blake2bHasher context = new Blake2bHasher(null);
        //context.Update(message,0,message.length);
        //return context.Finish();
        return Blake2b.computeHash(message);
    }

    static private BigInteger xRecover(BigInteger y) {
        BigInteger y2 = y.multiply(y);
        BigInteger xx = (y2.subtract(BigInteger.ONE)).multiply(d.multiply(y2).add(BigInteger.ONE).modPow(qm2, q));
        BigInteger x = xx.modPow(qp3div8, q);
        if (!x.multiply(x).subtract(xx).mod(q).equals(BigInteger.ZERO)) x = (x.multiply(I).mod(q));
        if (!x.mod(two).equals(BigInteger.ZERO)) x = q.subtract(x);
        return x;
    }

    static private BigInteger[] edwards(BigInteger[] P, BigInteger[] Q) {
        BigInteger x1 = P[0];
        BigInteger y1 = P[1];
        BigInteger x2 = Q[0];
        BigInteger y2 = Q[1];
        BigInteger dtemp = d.multiply(x1).multiply(x2).multiply(y1).multiply(y2);
        BigInteger x3 = ((x1.multiply(y2)).add((x2.multiply(y1)))).multiply(BigInteger.ONE.add(dtemp).modPow(qm2, q));
        BigInteger y3 = ((y1.multiply(y2)).add((x1.multiply(x2)))).multiply(BigInteger.ONE.subtract(dtemp).modPow(qm2, q));
        return new BigInteger[]{x3.mod(q), y3.mod(q)};
    }

    // removed recursion and replace with a loop
    static private BigInteger[] scalarMult(BigInteger[] P, BigInteger e) {
        BigInteger[] Q = {BigInteger.ZERO, BigInteger.ONE};
        Q=edwards(Q,Q);
        Q=edwards(Q,P);

        int len = e.bitLength()-2;
        for(int c=len;c>=0;c--){
            Q=edwards(Q,Q);
            if(e.testBit(c)) Q = edwards(Q,P);
        }
        return Q;
    }

    static private byte[] encodeInt(BigInteger y) {
        byte[] in = y.toByteArray();
        byte[] out = new byte[in.length];
        for (int i=0;i<in.length;i++) {
            out[i] = in[in.length-1-i];
        }
        return out;
    }

    static private byte[] encodePoint(BigInteger[] P) {
        BigInteger x = P[0];
        BigInteger y = P[1];
        byte[] out = encodeInt(y);
        out[out.length-1] |= (x.testBit(0) ? 0x80 : 0);
        return out;
    }

    static private int bit(byte[] h, int i) {
        return h[i / 8] >> (i % 8) & 1;
    }

    static private BigInteger hint(byte[] m) {
        byte[] h = hash(m);
        BigInteger hsum = BigInteger.ZERO;
        for (int i=0;i<2*b;i++) {
            hsum = hsum.add(two.pow(i).multiply(BigInteger.valueOf(bit(h,i))));
        }
        return hsum;
    }

    static private boolean isOnCurve(BigInteger[] P) {
        BigInteger x = P[0];
        BigInteger y = P[1];
        BigInteger xx = x.multiply(x);
        BigInteger yy = y.multiply(y);
        BigInteger dxxyy = d.multiply(yy).multiply(xx);
        return xx.negate().add(yy).subtract(BigInteger.ONE).subtract(dxxyy).mod(q).equals(BigInteger.ZERO);
    }

    static private BigInteger decodeInt(byte[] s) {
        byte[] out = new byte[s.length];
        for (int i=0;i<s.length;i++) {
            out[i] = s[s.length-1-i];
        }
        return new BigInteger(out).and(un);
    }

    static private BigInteger[] decodePoint(byte[] s) throws Exception {
        byte[] ybyte = new byte[s.length];
        for (int i=0;i<s.length;i++) {
            ybyte[i] = s[s.length-1-i];
        }
        BigInteger y = new BigInteger(ybyte).and(un);
        BigInteger x = xRecover(y);
        if ((x.testBit(0)?1:0) != bit(s, b-1)) {
            x = q.subtract(x);
        }
        BigInteger[] P = {x,y};
        if (!isOnCurve(P)) throw new Exception("decoding point that is not on curve");
        return P;
    }
}