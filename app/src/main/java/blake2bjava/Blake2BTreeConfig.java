package blake2bjava;

public class Blake2BTreeConfig {
	public int intermediateHashSize;
	public int maxHeight;
	public long leafSize;
	public int fanOut;

	public Blake2BTreeConfig(int intermediateHashSize, long leafSize, int fanOut, int maxHeight)
	{
		this.intermediateHashSize = intermediateHashSize;
		this.maxHeight = maxHeight;
		this.leafSize = leafSize;
		this.fanOut = fanOut;
	}
	
	public int getIntermediateHashSize() {
		return intermediateHashSize;
	}

	public void setIntermediateHashSize(int intermediateHashSize) {
		this.intermediateHashSize = intermediateHashSize;
	}

	public int getMaxHeight() {
		return maxHeight;
	}

	public void setMaxHeight(int maxHeight) {
		this.maxHeight = maxHeight;
	}

	public long getLeafSize() {
		return leafSize;
	}

	public void setLeafSize(long leafSize) {
		this.leafSize = leafSize;
	}

	public int getFanOut() {
		return fanOut;
	}

	public void setFanOut(int fanOut) {
		this.fanOut = fanOut;
	}
}
