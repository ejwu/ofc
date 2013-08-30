
public class LongCompleteOfcHandTest extends LongOfcHandTest {

	public LongCompleteOfcHandTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testEvalCacheDoesntChange() {
		long front = VALID_NO_ROYALTY_HIGH.getFrontRank();
		assertEquals(front, VALID_NO_ROYALTY_HIGH.getFrontRank());
		long middle = VALID_NO_ROYALTY_HIGH.getMiddleRank();
		assertEquals(middle, VALID_NO_ROYALTY_HIGH.getMiddleRank());
		long back = VALID_NO_ROYALTY_HIGH.getBackRank();
		assertEquals(back, VALID_NO_ROYALTY_HIGH.getBackRank());
	}

}
