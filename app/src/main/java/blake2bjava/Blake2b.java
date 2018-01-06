package blake2bjava;

//import org.apache.commons.codec.binary.Hex;

/**
 * Blake2b hash.
 * 
 * Implementation of Blake2b hash algorithm.
 * This class exposes interfaces to generate digest for a given message.
 * Default digest size is 64 bit.
 * 
 * @author Soon Hyung Kwon
 * @author Andre Hashimoto Oku
 */
public class Blake2b {

	/**
	 * Creates Blake2b hash for given data passed as byte array.
	 * In Java String can be converted to byte array using String.getBytes()
	 * function.
	 * 
	 * @param data is the byte array data to be hashed.
	 * @return byte array of 64 elements.
	 */
	public static byte[] computeHash(byte[] data) {
		return computeHash(data, 0, data.length, null);
	}
	
	private static byte[] computeHash(byte[] data, int start, int count, Blake2bConfig config) {
		Blake2bHasher hasher = new Blake2bHasher(config);
		hasher.Update(data, start, count);
		return hasher.Finish();
	}
	
	/**
	 * Tests the algorithm with known message-digest pair.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		//byte[] input = "The quick brown fox jumps over the lazy dog".getBytes();
		//String digest = "A8ADD4BDDDFD93E4877D2746E62817B116364A1FA7BC148D95090BC7333B3673F82401CF7AA2E4CB1ECD90296E3F14CB5413F8ED77BE73045B13914CDCD6A918";
		//byte[] hash = Blake2b.computeHash(input);
		//System.out.println("Calculated digest corresponds: " +
		//		(digest.compareToIgnoreCase(Hex.encodeHexString(hash))==0?"true":"false"));
	}
}
