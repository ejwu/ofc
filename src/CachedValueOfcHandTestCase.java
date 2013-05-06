import org.pokersource.game.Deck;


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
		int[] ranks = new int[] {Deck.RANK_4, Deck.RANK_3, Deck.RANK_2};
		long handValue = StupidEval.eval3(ranks);
		
		assertEquals(CachedValueOfcHand.UNSET, cHand1.frontValue);
		cHand1.addFront(new OfcCard("4c"));
		assertEquals(CachedValueOfcHand.UNSET, cHand1.frontValue);
		cHand1.addFront(new OfcCard("3c"));
		assertEquals(CachedValueOfcHand.UNSET, cHand1.frontValue);
		cHand1.addFront(new OfcCard("2c"));
		assertEquals(handValue, cHand1.frontValue);
	}

	public void testCompleteMiddle() {
		int[] ranks = new int[] {Deck.RANK_6, Deck.RANK_5, Deck.RANK_4, Deck.RANK_3, Deck.RANK_2};
		int[] suits = new int[] {Deck.SUIT_CLUBS,  Deck.SUIT_CLUBS, Deck.SUIT_CLUBS, Deck.SUIT_CLUBS, Deck.SUIT_DIAMONDS};
		long handValue = StupidEval.eval(ranks, suits);

		assertEquals(CachedValueOfcHand.UNSET, cHand1.middleValue);
		cHand1.addMiddle(new OfcCard("6c"));
		assertEquals(CachedValueOfcHand.UNSET, cHand1.middleValue);
		cHand1.addMiddle(new OfcCard("5c"));
		assertEquals(CachedValueOfcHand.UNSET, cHand1.middleValue);
		cHand1.addMiddle(new OfcCard("4c"));
		assertEquals(CachedValueOfcHand.UNSET, cHand1.middleValue);
		cHand1.addMiddle(new OfcCard("3c"));
		assertEquals(CachedValueOfcHand.UNSET, cHand1.middleValue);
		cHand1.addMiddle(new OfcCard("2d"));
		assertEquals(handValue, cHand1.middleValue);
	}

	public void testCompleteBack() {
		int[] ranks = new int[] {Deck.RANK_6, Deck.RANK_6, Deck.RANK_6, Deck.RANK_3, Deck.RANK_3};
		int[] suits = new int[] {Deck.SUIT_CLUBS,  Deck.SUIT_HEARTS, Deck.SUIT_SPADES, Deck.SUIT_CLUBS, Deck.SUIT_DIAMONDS};
		long handValue = StupidEval.eval(ranks, suits);

		assertEquals(CachedValueOfcHand.UNSET, cHand1.backValue);
		cHand1.addBack(new OfcCard("6c"));
		assertEquals(CachedValueOfcHand.UNSET, cHand1.backValue);
		cHand1.addBack(new OfcCard("6h"));
		assertEquals(CachedValueOfcHand.UNSET, cHand1.backValue);
		cHand1.addBack(new OfcCard("6s"));
		assertEquals(CachedValueOfcHand.UNSET, cHand1.backValue);
		cHand1.addBack(new OfcCard("3c"));
		assertEquals(CachedValueOfcHand.UNSET, cHand1.backValue);
		cHand1.addBack(new OfcCard("3d"));
		assertEquals(handValue, cHand1.backValue);
	}
	
	public void testCopyRetainsCachedValue() {
		assertEquals(CachedValueOfcHand.UNSET, cHand1.middleValue);
		cHand1.addMiddle(new OfcCard("6c"));
		cHand1.addMiddle(new OfcCard("5c"));
		cHand1.addMiddle(new OfcCard("4c"));
		cHand1.addMiddle(new OfcCard("3c"));
		cHand1.addMiddle(new OfcCard("2d"));
		long middleValue = cHand1.middleValue;
		
		CachedValueOfcHand copy = (CachedValueOfcHand) cHand1.copy();
		assertEquals(middleValue, copy.middleValue);
		
	}
}
