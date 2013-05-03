
public abstract class CachedValueOfcHandTestCase extends OfcHandTestCase {

	CachedValueOfcHand cHand1;
	CachedValueOfcHand cHand2;
	
	public CachedValueOfcHandTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		hand1 = cHand1;
		hand2 = cHand2;
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	protected abstract void initHands();
	
	public void testCompleteFront() {
		assertEquals(CachedValueOfcHand.UNSET, cHand1.frontValue);
	}
	
}
