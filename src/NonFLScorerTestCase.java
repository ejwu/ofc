
public abstract class NonFLScorerTestCase extends ScorerTestCase {

	public NonFLScorerTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testScoreFantasyland() throws Exception {
		assertEquals(0, scorer.getFantasylandValue(QUEENS_FRONT));
	}
	
}
