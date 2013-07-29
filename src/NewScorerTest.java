
public class NewScorerTest extends NonFLScorerTestCase {

	public NewScorerTest(String name) {
		super(name);
		scorer = new Scorers.NewScorer();
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
