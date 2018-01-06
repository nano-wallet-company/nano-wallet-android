package blake2bjava;

public class Blake2bHasher {

	private Blake2bCore core;
	private long[] rawConfig;
	private byte[] key;
	private int outputSizeInBytes;
	private Blake2bConfig defaultConfig;
	
	public Blake2bHasher(Blake2bConfig config) {		
		try {
			core = new Blake2bCore();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		defaultConfig = new Blake2bConfig();
		
		if (config == null)
			config = this.defaultConfig;
		try {
			this.rawConfig = Blake2IvBuilder.configB(config, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if ( config.getKey() != null && config.getKey().length != 0) {
			this.key = new byte[128];
			System.arraycopy(config.getKey(), 0, this.key, 0, config.getKey().length);
		}
		this.outputSizeInBytes = config.getOutputSizeInBytes();
		initialize();
	}
	
	private void initialize() {
		try {
			core.initialize(this.rawConfig);
			if (this.key != null)
				core.hashCore(this.key, 0, this.key.length);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void Update(byte[] data, int start, int count) {
		try {
			core.hashCore(data, start, count);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public byte[] Finish() {
		byte[] fullResult = null;
		try {
			fullResult = core.hashFinal();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (this.outputSizeInBytes != fullResult.length) {
			byte[] result = new byte[this.outputSizeInBytes];
			System.arraycopy(fullResult, 0, result, 0, result.length);
			return result;
		}
		return fullResult;
	}
}
