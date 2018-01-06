package blake2bjava;

/**
 * Blake2b initilization parameters builder. 
 * 
 * @author Soon Hyung Kwon
 * @author Andre Hashimoto Oku
 */
public class Blake2IvBuilder {
	private static Blake2BTreeConfig sequentialTreeConfig = new Blake2BTreeConfig(0, 0, 1, 1);

	public static long[] configB(Blake2bConfig config, Blake2BTreeConfig treeConfig) throws Exception {
		boolean isSequential = treeConfig == null;
		if (isSequential)
			treeConfig = sequentialTreeConfig;
		long[] rawConfig = new long[8];
//		long[] result = new long[8];

		//digest length
		if (config.getOutputSizeInBytes() <= 0 | config.getOutputSizeInBytes() > 64)
			throw new Exception("config.OutputSize");
		rawConfig[0] |= (long)config.getOutputSizeInBytes();

		//Key length
		if (config.getKey() != null) {
			if (config.getKey().length > 64)
				throw new Exception(config.getKey() + "Key too long");
			rawConfig[0] |= (long)((int)config.getKey().length << 8);
		}
		
		// FanOut
		rawConfig[0] |= (int)treeConfig.getFanOut() << 16;
		// Depth
		rawConfig[0] |= (int)treeConfig.getMaxHeight() << 24;
		// Leaf length
		rawConfig[0] |= ((long)treeConfig.getLeafSize()) << 32;
		// Inner length
		if (!isSequential && (treeConfig.getIntermediateHashSize() <= 0 || treeConfig.getIntermediateHashSize() > 64))
			throw new Exception("treeConfig.TreeIntermediateHashSize");
		rawConfig[2] |= (int)treeConfig.getIntermediateHashSize() << 8;
		// Salt
		if (config.getSalt() != null)
		{
			if (config.getSalt().length != 16)
				throw new Exception("config.Salt has invalid length");
			rawConfig[4] = Blake2bCore.bytesToLong(config.getSalt(), 0);
			rawConfig[5] = Blake2bCore.bytesToLong(config.getSalt(), 8);
		}
		// Personalization
		if (config.getPersonalization() != null)
		{
			if (config.getPersonalization().length != 16)
				throw new Exception("config.Personalization has invalid length");
			rawConfig[6] = Blake2bCore.bytesToLong(config.getPersonalization(), 0);
			rawConfig[7] = Blake2bCore.bytesToLong(config.getPersonalization(), 8);
		}

		return rawConfig;
	}

}
