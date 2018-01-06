package blake2bjava;

public class Blake2bConfig {
	private byte[] personalization;
	private byte[] salt;
	private byte[] key;
	private int outputSizeInBytes;
	
	public Blake2bConfig() {
		this.outputSizeInBytes = 64;
	}

	public byte[] getPersonalization() {
		return personalization;
	}

	public void setPersonalization(byte[] personalization) {
		this.personalization = personalization;
	}

	public byte[] getSalt() {
		return salt;
	}

	public void setSalt(byte[] salt) {
		this.salt = salt;
	}

	public byte[] getKey() {
		return key;
	}

	public void setKey(byte[] key) {
		this.key = key;
	}

	public int getOutputSizeInBytes() {
		return outputSizeInBytes;
	}

	public void setOutputSizeInBytes(int outputSizeInBytes) {
		this.outputSizeInBytes = outputSizeInBytes;
	}

	public int getOutputSizeInBits() {
		return outputSizeInBytes * 8;
	}

	public void setOutputSizeInBits(int outputSizeInBits) {
		this.outputSizeInBytes = outputSizeInBits / 8;
	}
}
