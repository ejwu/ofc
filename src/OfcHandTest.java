import junit.framework.TestCase;


public class OfcHandTest extends TestCase {

	private OfcHand hand1;
	private OfcHand hand2;
	
	protected void setUp() {
		hand1 = new OfcHand();
		hand2 = new OfcHand();
	}
	
	public OfcHandTest(String name) {
		super(name);
	}

	private void assertHashesDifferent() {
		assertTrue(hand1.hashCode() != hand2.hashCode());
	}
	
	public void testHashCodeDifferentCard() {
		hand1.addBack(new OfcCard("As"));
		hand2.addBack(new OfcCard("Ks"));
		assertHashesDifferent();
	}
	
	public void testHashCodeDifferentSet() {
		hand1.addBack(new OfcCard("7d"));
		hand2.addMiddle(new OfcCard("7d"));
		assertHashesDifferent();
	}

}
