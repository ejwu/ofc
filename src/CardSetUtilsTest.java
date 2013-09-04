import static org.junit.Assert.*;
import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class CardSetUtilsTest extends TestCase {
	
	public CardSetUtilsTest() {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testHasFlushDrawNoHand() {
		assertTrue(CardSetUtils.hasFlushDraw(createMask("")));
	}

	public void testHasFlushDrawClubs() {
		assertTrue(CardSetUtils.hasFlushDraw(createMask("AcKcQc")));
	}
	
	public void testHasFlushDrawNonClubs() {
		assertTrue(CardSetUtils.hasFlushDraw(createMask("AdKdQd")));
	}
	
	public void testDoesntHaveFlushDraw() {
		assertFalse(CardSetUtils.hasFlushDraw(createMask("AcAs")));
	}
	
	private long createMask(String hand) {
		return LongOfcHand.create("//" + hand).getBackMask();	
	}
	
}
