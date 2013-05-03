public class StupidOfcHandTest extends CachedValueOfcHandTestCase {

	public StupidOfcHandTest(String name) {
		super(name);
	}

	protected void initHands() {
		cHand1 = new StupidOfcHand();
		cHand2 = new StupidOfcHand();
	}
	
}
