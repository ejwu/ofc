
public class OldFantasylandScorerTest extends FantasylandTestCase {

	public OldFantasylandScorerTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		scorer = new Scorers.OldFantasylandScorer();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
