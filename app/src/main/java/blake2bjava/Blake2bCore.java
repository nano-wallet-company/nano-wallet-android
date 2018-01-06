package blake2bjava;

/**
 * Blake2b hash algorithm implementation class
 * 
 * @author Soon Hyung Kwon
 * @author Andre Hashimoto Oku
 */
public class Blake2bCore {
	
	private boolean isInitialized;
	
	private int bufferFilled;
	private byte[] buffer;
	private long[] m;
	private long[] h;
	private long[] v;
	private long counter0, 
				 counter1,
				 finalizationFlag0,
				 finalizationFlag1;
	
	private final int numberOfRounds;
	private final int blockSizeInBytes;
	
	private final long iv0;
	private final long iv1;
	private final long iv2;
	private final long iv3;
	private final long iv4;
	private final long iv5;
	private final long iv6;
	private final long iv7;
	
	private final int[] sigma;
	
	public Blake2bCore() throws Exception {
		this.isInitialized = false;
		this.buffer = new byte[128];
		
		this.m = new long[16];
		this.h = new long[8];
		this.v = new long[16];
		
		this.numberOfRounds = 12;
		this.blockSizeInBytes = 128;
		
		this.iv0 = hexToLong("6A09E667F3BCC908");
		this.iv1 = hexToLong("BB67AE8584CAA73B");
		this.iv2 = hexToLong("3C6EF372FE94F82B");
		this.iv3 = hexToLong("A54FF53A5F1D36F1");
		this.iv4 = hexToLong("510E527FADE682D1");
		this.iv5 = hexToLong("9B05688C2B3E6C1F");
		this.iv6 = hexToLong("1F83D9ABFB41BD6B");
		this.iv7 = hexToLong("5BE0CD19137E2179");
		
		this.sigma = new int[] {
			 0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15,
			14, 10,  4,  8,  9, 15, 13,  6,  1, 12,  0,  2, 11,  7,  5,  3,
			11,  8, 12,  0,  5,  2, 15, 13, 10, 14,  3,  6,  7,  1,  9,  4,
			 7,  9,  3,  1, 13, 12, 11, 14,  2,  6,  5, 10,  4,  0, 15,  8,
			 9,  0,  5,  7,  2,  4, 10, 15, 14,  1, 11, 12,  6,  8,  3, 13,
			 2, 12,  6, 10,  0, 11,  8,  3,  4, 13,  7,  5, 15, 14,  1,  9,
			12,  5,  1, 15, 14, 13,  4, 10,  0,  7,  6,  3,  9,  2,  8, 11,
			13, 11,  7, 14, 12,  1,  3,  9,  5,  0, 15,  4,  8,  6,  2, 10,
			 6, 15, 14,  9, 11,  3,  0,  8, 12,  2, 13,  7,  1,  4, 10,  5,
			10,  2,  8,  4,  7,  6,  1,  5, 15, 11,  9, 14,  3, 12, 13,  0,
			 0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15,
			14, 10,  4,  8,  9, 15, 13,  6,  1, 12,  0,  2, 11,  7,  5,  3
		};
	}

	public void initialize(long[] config) throws Exception {
		if (config == null)
			throw new Exception("config");
		if (config.length != 8)
			throw new Exception("config length must be 8 words");
		
		this.isInitialized = true;

		this.h[0] = this.iv0;
		this.h[1] = this.iv1;
		this.h[2] = this.iv2;
		this.h[3] = this.iv3;
		this.h[4] = this.iv4;
		this.h[5] = this.iv5;
		this.h[6] = this.iv6;
		this.h[7] = this.iv7;

		this.counter0 = 0;
		this.counter1 = 0;
		this.finalizationFlag0 = 0;
		this.finalizationFlag1 = 0;

		this.bufferFilled = 0;

		for (int i = 0; i < this.buffer.length; i++)
			buffer[i] = 0;

		for (int i = 0; i < 8; i++)
			this.h[i] ^= config[i];
	}

	/**
	 * Main hash method
	 * @throws Exception 
	 */
	public void hashCore(byte[] array, int start, int count) throws Exception
	{
		if (!this.isInitialized)
			throw new Exception("Not initialized");
		if (array == null)
			throw new Exception("array");
		if (start < 0)
			throw new Exception("start");
		if (count < 0)
			throw new Exception("count");
		if ((long)start + (long)count > array.length)
			throw new Exception("start+count");
			
		int offset = start;
		int bufferRemaining = this.blockSizeInBytes - this.bufferFilled;

		if ((this.bufferFilled > 0) && (count > bufferRemaining)) {
			System.arraycopy(array, offset, this.buffer, this.bufferFilled, bufferRemaining);
			this.counter0 += this.blockSizeInBytes;
			if (this.counter0 == 0)
				this.counter1++;
			compress(this.buffer, 0);
			offset += bufferRemaining;
			count -= bufferRemaining;
			this.bufferFilled = 0;
		}

		while (count > this.blockSizeInBytes) {
			this.counter0 += this.blockSizeInBytes;
			if (this.counter0 == 0)
				this.counter1++;
			compress(array, offset);
			offset += this.blockSizeInBytes;
			count -= this.blockSizeInBytes;
		}

		if (count > 0) {
			System.arraycopy(array, offset, this.buffer, this.bufferFilled, count);
			this.bufferFilled += count;
		}
	}
	
	public byte[] hashFinal() throws Exception {
		return hashFinal(false);
	}

	public byte[] hashFinal(boolean isEndOfLayer) throws Exception
	{
		if (!this.isInitialized)
			throw new Exception("Not initialized");
		this.isInitialized = false;

		//Last compression
		this.counter0 += (int)this.bufferFilled;
		this.finalizationFlag0 = hexToLong("FFFFFFFFFFFFFFFF");
		if (isEndOfLayer)
			this.finalizationFlag1 = hexToLong("FFFFFFFFFFFFFFFF");
		for (int i = this.bufferFilled; i < this.buffer.length; i++)
			this.buffer[i] = 0;
		compress(this.buffer, 0);

		//Output
		byte[] hash = new byte[64];
		for (int i = 0; i < 8; ++i)
			longToBytes(this.h[i], hash, i << 3);
		return hash;
	}
	
	/**
	 * Helper method.
	 * Creates from 8 sequential entries of buffer byte array
	 * (8 * 8 bit = 64 bit) to a long and returns it.
	 * 
	 * @param buffer
	 * @param offset
	 * @return
	 */
	public static long bytesToLong(byte[] buffer, int offset) {
		return
			((long)buffer[offset + 7] << 7 * 8) |
			((long)buffer[offset + 6] << 6 * 8) |
			((long)buffer[offset + 5] << 5 * 8) |
			((long)buffer[offset + 4] << 4 * 8) |
			((long)buffer[offset + 3] << 3 * 8) |
			((long)buffer[offset + 2] << 2 * 8) |
			((long)buffer[offset + 1] << 1 * 8) |
			((long)buffer[offset]);		
	}
	
	/**
	 * Helper method.
	 * Divides a long value in 8 pieces and puts them in byte array.
	 * 
	 * @param value
	 * @param buffer array gets filled with parts of value parameter.
	 * @param offset
	 */
	public static void longToBytes(long value, byte[] buffer, int offset)
	{
		buffer[offset + 7] = (byte)(value >>> 7 * 8);
		buffer[offset + 6] = (byte)(value >>> 6 * 8);
		buffer[offset + 5] = (byte)(value >>> 5 * 8);
		buffer[offset + 4] = (byte)(value >>> 4 * 8);
		buffer[offset + 3] = (byte)(value >>> 3 * 8);
		buffer[offset + 2] = (byte)(value >>> 2 * 8);
		buffer[offset + 1] = (byte)(value >>> 1 * 8);
		buffer[offset] = (byte)value;
	}
	
	private long rotateRight(long value, int nBits) {
		return (value >>> nBits) | (value << (64 - nBits));
	}
	
	private void G(int a, int b, int c, int d, int r, int i) {
		int p = (r << 4) + i;
		int p0 = this.sigma[p];
		int p1 = this.sigma[p + 1];
		long[] v = this.v;
		long[] m = this.m;

		v[a] += v[b] + m[p0];
		v[d] = rotateRight(v[d] ^ v[a], 32);
		v[c] += v[d];
		v[b] = rotateRight(v[b] ^ v[c], 24);
		v[a] += v[b] + m[p1];
		v[d] = rotateRight(v[d] ^ v[a], 16);
		v[c] += v[d];
		v[b] = rotateRight(v[b] ^ v[c], 63);
	}
	
	private void compress(byte[] block, int start) {
		long[] v = this.v;
		long[] h = this.h;
		long[] m = this.m;

		for (int i = 0; i < 16; ++i)
			m[i] = bytesToLong(block, start + (i << 3));

		v[0] = h[0];
		v[1] = h[1];
		v[2] = h[2];
		v[3] = h[3];
		v[4] = h[4];
		v[5] = h[5];
		v[6] = h[6];
		v[7] = h[7];

		v[8]  = this.iv0;
		v[9]  = this.iv1;
		v[10] = this.iv2;
		v[11] = this.iv3;
		v[12] = this.iv4 ^ this.counter0;
		v[13] = this.iv5 ^ this.counter1;
		v[14] = this.iv6 ^ this.finalizationFlag0;
		v[15] = this.iv7 ^ this.finalizationFlag1;

		for (int r = 0; r < this.numberOfRounds; ++r) {
			G(0, 4, 8, 12, r, 0);
			G(1, 5, 9, 13, r, 2);
			G(2, 6, 10, 14, r, 4);
			G(3, 7, 11, 15, r, 6);
			G(0, 5, 10, 15, r, 8);
			G(1, 6, 11, 12, r, 10);
			G(2, 7, 8, 13, r, 12);
			G(3, 4, 9, 14, r, 14);
		}

		for (int i = 0; i < 8; ++i)
			h[i] = h[i] ^ v[i] ^ v[i + 8];
	}
	
	/**
	 * Helper method.
	 * Returns a long with the bit representation of hex string parameter. 
	 * 
	 * @param hex
	 * @return
	 * @throws Exception 
	 */
	private long hexToLong(String hex) throws Exception {
		if(hex.length() != 16)
			throw new Exception("hex parameter is different than 16 bits.");
		long mostSig = Long.parseLong(hex.substring(0, 8), 16);
		long leastSig = Long.parseLong(hex.substring(8, 16), 16);
		mostSig <<= 8 * 4;
		return mostSig | leastSig;
	}
}
