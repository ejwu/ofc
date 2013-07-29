
public abstract class FantasylandTestCase extends ScorerTestCase {

	public FantasylandTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	
	public void testScoreFantasyland() throws Exception {
		assertEquals(0, scorer.getFantasylandValue(JACKS_FRONT));
		assertEquals(Scorers.FANTASYLAND_VALUE, scorer.getFantasylandValue(QUEENS_FRONT));
		assertEquals(Scorers.FANTASYLAND_VALUE, scorer.getFantasylandValue(TRIP_2_FRONT));
	}
	
	public void testFouledHandsFL() throws Exception {
		assertEquals(0, scorer.getFantasylandValue(QUEENS_FRONT_FOULED));
	}
}
