public abstract class CachedValueOfcHand implements OfcHand {

	protected static final long UNSET = Long.MIN_VALUE;
	protected long backValue = UNSET;
	protected long middleValue = UNSET;
	protected long frontValue = UNSET;

	protected boolean willBeFouled = false;
	
	public CachedValueOfcHand() {
		super();
	}

	protected void copy(CachedValueOfcHand hand) {
		hand.backValue = this.backValue;
		hand.middleValue = this.middleValue;
		hand.frontValue = this.frontValue;
		hand.willBeFouled = this.willBeFouled;
	}
	
	protected void completeBack() {
		backValue = getBackRank();
		if (middleValue != UNSET && backValue < middleValue) {
			willBeFouled = true;
		}
		if (frontValue != UNSET && backValue < frontValue) {
			willBeFouled = true;
		}
	}
		
	protected void completeMiddle() {
		middleValue = getMiddleRank();
		if (frontValue != UNSET && middleValue < frontValue) {
			willBeFouled = true;
		}
		if (backValue != UNSET && backValue < middleValue) {
			willBeFouled = true;
		}
	}
	
	protected void completeFront() {
		frontValue = getFrontRank();
		if (middleValue != UNSET && middleValue < frontValue) {
			willBeFouled = true;
		}
		if (backValue != UNSET && backValue < frontValue) {
			willBeFouled = true;
		}
	}
}