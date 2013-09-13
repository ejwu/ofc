
import java.util.Map;

import junit.framework.TestCase;

import com.google.common.collect.Lists;

public class OfcDeckTest extends TestCase {

	private OfcDeck deck;
	
	public OfcDeckTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		deck = new OfcDeck();
		deck.initialize();
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testInitialize() {
		deck = new OfcDeck();
		assertEquals("", deck.toString());
		deck.initialize();
		assertEquals(52, deck.toArray().length);
		for (String rank : Lists.newArrayList("2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K", "A")) {
			for (String suit : Lists.newArrayList("c", "d", "h", "s")) {
				assertTrue(deck.toString().contains(rank + suit));
			}
		}
	}
	
	public void testRemoveCardTwice() {
		deck.removeCard("Ks");
		assertEquals(51, deck.toArray().length);
		try {
			deck.removeCard("Ks");
			assertEquals("", deck.toString());
			fail("Shouldn't be able to removeCard twice");
		} catch (Exception expected) {
		}
	}
	
	public void testByRankCount() {
		Map<OfcCard, Integer> rankCount = deck.byRankCount();
		assertEquals(13, rankCount.size());
		for (OfcCard c : rankCount.keySet()) {
			assertEquals((Integer) 4, rankCount.get(c));
		}
		// specific suit here is an artifact of card ordering
		assertTrue(rankCount.containsKey(new OfcCard("As")));
		assertTrue(rankCount.containsKey(new OfcCard("Ks")));
		assertTrue(rankCount.containsKey(new OfcCard("Qs")));
		assertTrue(rankCount.containsKey(new OfcCard("Js")));
		assertTrue(rankCount.containsKey(new OfcCard("Ts")));
		assertTrue(rankCount.containsKey(new OfcCard("9s")));
		assertTrue(rankCount.containsKey(new OfcCard("8s")));
		assertTrue(rankCount.containsKey(new OfcCard("7s")));
		assertTrue(rankCount.containsKey(new OfcCard("6s")));
		assertTrue(rankCount.containsKey(new OfcCard("5s")));
		assertTrue(rankCount.containsKey(new OfcCard("4s")));
		assertTrue(rankCount.containsKey(new OfcCard("3s")));
		assertTrue(rankCount.containsKey(new OfcCard("2s")));

		deck.removeCard("8s");
		rankCount = deck.byRankCount();
		assertEquals(13, rankCount.size());
		assertFalse(rankCount.containsKey(new OfcCard("8s")));
		// More card ordering artifacts
		assertTrue(rankCount.containsKey(new OfcCard("8c")));
		assertEquals((Integer) 3, rankCount.get(new OfcCard("8c")));
		
		deck.removeCard("8c");
		deck.removeCard("8d");
		deck.removeCard("8h");
		rankCount = deck.byRankCount();
		assertEquals(12, rankCount.size());
		for (OfcCard c : rankCount.keySet()) {
			assertEquals((Integer) 4, rankCount.get(c));
		}
		// specific suit here is an artifact of card ordering
		assertTrue(rankCount.containsKey(new OfcCard("As")));
		assertTrue(rankCount.containsKey(new OfcCard("Ks")));
		assertTrue(rankCount.containsKey(new OfcCard("Qs")));
		assertTrue(rankCount.containsKey(new OfcCard("Js")));
		assertTrue(rankCount.containsKey(new OfcCard("Ts")));
		assertTrue(rankCount.containsKey(new OfcCard("9s")));
		assertTrue(rankCount.containsKey(new OfcCard("7s")));
		assertTrue(rankCount.containsKey(new OfcCard("6s")));
		assertTrue(rankCount.containsKey(new OfcCard("5s")));
		assertTrue(rankCount.containsKey(new OfcCard("4s")));
		assertTrue(rankCount.containsKey(new OfcCard("3s")));
		assertTrue(rankCount.containsKey(new OfcCard("2s")));
	}
}
