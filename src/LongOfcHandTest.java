import org.pokersource.game.Deck;


public class LongOfcHandTest extends CachedValueOfcHandTestCase {

	public LongOfcHandTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void initHands() {
		cHand1 = new LongOfcHand();
		cHand2 = new LongOfcHand();
	}
	
	public void testHandIsSorted() {
		LongOfcHand hand = new LongOfcHand();
		
		hand.addBack(new OfcCard("2c"));
		hand.addBack(new OfcCard("3c"));
		hand.addBack(new OfcCard("6c"));
		hand.addBack(new OfcCard("4h"));
		hand.addBack(new OfcCard("5c"));
		
		int[] ranks = new int[5];
		int[] suits = new int[5];
		LongOfcHand.convertForEval(hand.back, ranks, suits, OfcHand.BACK_SIZE);

		assertEquals(Deck.RANK_6, ranks[0]);
		assertEquals(Deck.RANK_5, ranks[1]);
		assertEquals(Deck.RANK_4, ranks[2]);
		assertEquals(Deck.RANK_3, ranks[3]);
		assertEquals(Deck.RANK_2, ranks[4]);

		assertEquals(Deck.SUIT_CLUBS, suits[0]);
		assertEquals(Deck.SUIT_CLUBS, suits[1]);
		assertEquals(Deck.SUIT_HEARTS, suits[2]);
		assertEquals(Deck.SUIT_CLUBS, suits[3]);
		assertEquals(Deck.SUIT_CLUBS, suits[4]);
	}
}
