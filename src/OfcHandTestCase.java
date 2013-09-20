import java.util.Arrays;

import org.pokersource.game.Deck;
import static org.pokersource.game.Deck.SUIT_CLUBS;
import static org.pokersource.game.Deck.SUIT_DIAMONDS;
import static org.pokersource.game.Deck.SUIT_HEARTS;
import static org.pokersource.game.Deck.SUIT_SPADES;

import junit.framework.TestCase;
import static org.junit.Assert.*;


public abstract class OfcHandTestCase extends TestCase {

	protected OfcHand hand1;
	protected OfcHand hand2;
	// really only here to allow the use of setHand()
	protected OfcDeck deck;
	protected CompleteOfcHand ch1;

	protected static final CompleteOfcHand VALID_NO_ROYALTY_HIGH =
		LongCompleteOfcHand.createComplete("5c5s6c/AcAsKcKsQs/8s8c8d7s6s"); // 55/AAKK/888
	
	protected static final CompleteOfcHand VALID_NO_ROYALTY_LOW =
		LongCompleteOfcHand.createComplete("QhJhTh/KhQdJdTd8d/Ad6d4d3d2h"); // Q-high/K-high/A-high

	private static final Integer[] ALL_SUITS = {Deck.SUIT_CLUBS, Deck.SUIT_DIAMONDS, Deck.SUIT_HEARTS, Deck.SUIT_SPADES};
	
	protected void setUp() throws Exception {
		super.setUp();
		initHands();
		deck = new OfcDeck();
		deck.initialize();
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	protected OfcHandTestCase(String name) {
		super(name);
	}

	protected abstract void initHands();
	
	private void assertHashesDifferent() {
		assertTrue(hand1.hashCode() != hand2.hashCode());
	}
	
	public void testHashesSame() {
		hand1.addBack(new OfcCard("8h"));
		hand2.addBack(new OfcCard("8h"));
		assertEquals(hand1.hashCode(), hand2.hashCode());
		hand1.addMiddle(new OfcCard("9d"));
		hand2.addMiddle(new OfcCard("9d"));
		assertEquals(hand1.hashCode(), hand2.hashCode());
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

	public void testAddTooManyFrontCards() {
		int startCard = 2;
		for (int i = startCard; i < OfcHand.FRONT_SIZE + startCard; i++) {
			hand1.addFront(new OfcCard(i + "s"));
		}
		try {
			hand1.addFront(new OfcCard("As"));
			fail("Shouldn't be able to add this many cards");
		} catch (Exception expected) {
		}
	}
	
	public void testAddTooManyMiddleCards() {
		int startCard = 2;
		for (int i = startCard; i < OfcHand.MIDDLE_SIZE + startCard; i++) {
			hand1.addMiddle(new OfcCard(i + "s"));
		}
		try {
			hand1.addMiddle(new OfcCard("As"));
			fail("Shouldn't be able to add this many cards");
		} catch (Exception expected) {
		}
	}
	
	public void testAddTooManyBackCards() {
		int startCard = 2;
		for (int i = startCard; i < OfcHand.BACK_SIZE + startCard; i++) {
			hand1.addBack(new OfcCard(i + "s"));
		}
		try {
			hand1.addBack(new OfcCard("As"));
			fail("Shouldn't be able to add this many cards");
		} catch (Exception expected) {
		}
	}
	
	public void testAddSameFrontCard() {
		addFront1("As");
		try {
			addFront1("As");
			fail();
		} catch (Exception expected) {
		}
	}

	public void testAddSameMiddleCard() {
		addMiddle1("As");
		try {
			addMiddle1("As");
			fail();
		} catch (Exception expected) {
		}
	}
	
	public void testAddSameBackCard() {
		addBack1("As");
		try {
			addBack1("As");
			fail();
		} catch (Exception expected) {
		}
	}
	
	public void testFrontSize() {
		assertEquals(0, hand1.getFrontSize());
		addFront1("As");
		assertEquals(1, hand1.getFrontSize());
		addFront1("Ks");
		assertEquals(2, hand1.getFrontSize());
		addFront1("Qs");
		assertEquals(3, hand1.getFrontSize());
	}
	
	public void testMiddleSize() {
		assertEquals(0, hand1.getMiddleSize());
		addMiddle1("As");
		assertEquals(1, hand1.getMiddleSize());
		addMiddle1("Ks");
		assertEquals(2, hand1.getMiddleSize());
		addMiddle1("Qs");
		assertEquals(3, hand1.getMiddleSize());
		addMiddle1("Js");
		assertEquals(4, hand1.getMiddleSize());
		addMiddle1("2s");
		assertEquals(5, hand1.getMiddleSize());
	}
	
	public void testBackSize() {
		assertEquals(0, hand1.getBackSize());
		addBack1("As");
		assertEquals(1, hand1.getBackSize());
		addBack1("Ks");
		assertEquals(2, hand1.getBackSize());
		addBack1("Qs");
		assertEquals(3, hand1.getBackSize());
		addBack1("Js");
		assertEquals(4, hand1.getBackSize());
		addBack1("2s");
		assertEquals(5, hand1.getBackSize());
	}

	public void testGetStreet() {
		// Okay, so 0-5 shouldn't even be possible.  Whatever.
		assertEquals(1, hand1.getStreet());
	
		addFront1("As");
		assertEquals(2, hand1.getStreet());
		addFront1("Ks");
		assertEquals(3, hand1.getStreet());
		addFront1("Qs");
		assertEquals(4, hand1.getStreet());

		addMiddle1("Ac");
		assertEquals(5, hand1.getStreet());
		addMiddle1("Kc");
		assertEquals(6, hand1.getStreet());
		addMiddle1("Qc");
		assertEquals(7, hand1.getStreet());
		addMiddle1("Jc");
		assertEquals(8, hand1.getStreet());
		addMiddle1("9c");
		assertEquals(9, hand1.getStreet());

		addBack1("Ah");
		assertEquals(10, hand1.getStreet());
		addBack1("Kh");
		assertEquals(11, hand1.getStreet());
		addBack1("Qh");
		assertEquals(12, hand1.getStreet());
		addBack1("Jh");
		assertEquals(13, hand1.getStreet());

		// This doesn't really mean anything
		addBack1("Th");
		assertEquals(14, hand1.getStreet());
	}
	
	public void testIsComplete() {
		hand1.addFront(new OfcCard("As"));
		assertTrue(!hand1.isComplete());
		hand1.addFront(new OfcCard("Ks"));
		assertTrue(!hand1.isComplete());
		hand1.addFront(new OfcCard("Qs"));
		assertTrue(!hand1.isComplete());
		hand1.addMiddle(new OfcCard("Ad"));
		assertTrue(!hand1.isComplete());
		hand1.addMiddle(new OfcCard("Kd"));
		assertTrue(!hand1.isComplete());
		hand1.addMiddle(new OfcCard("Qd"));
		assertTrue(!hand1.isComplete());
		hand1.addMiddle(new OfcCard("Jd"));
		assertTrue(!hand1.isComplete());
		hand1.addMiddle(new OfcCard("Td"));
		assertTrue(!hand1.isComplete());
		hand1.addBack(new OfcCard("Ah"));
		assertTrue(!hand1.isComplete());
		hand1.addBack(new OfcCard("Kh"));
		assertTrue(!hand1.isComplete());
		hand1.addBack(new OfcCard("Qh"));
		assertTrue(!hand1.isComplete());
		hand1.addBack(new OfcCard("Jh"));
		assertTrue(!hand1.isComplete());
		hand1.addBack(new OfcCard("Th"));
		assertTrue(hand1.isComplete());
	}

	// TODO: find out if this is actually true.  Signs point to no
	public void testBackMustBeatMiddle() {
		foulHand1();
		
		assertTrue(ch1.isFouled());
	}
	
	public void testMiddleMustBeatFront() {
		ch1 = LongCompleteOfcHand.createComplete("AdAsKd/AhAcQsJsTs/2c2d2h2s3h");
		assertTrue(ch1.isFouled());
	}
	
	public void testMiddleCanOutkickFront() {
		ch1 = LongCompleteOfcHand.createComplete("AdAsKd/AhAcKs3d2d/4h4c4d4s3h");
		assertTrue(!ch1.isFouled());
	}
	
	public void testToKeyStringDoesNotMutate() {
		hand1.setHand("AcAdAs/Kd2c3c4c5c/", deck);
		
		OfcHand copy = hand1.copy();
		assertEquals(copy, hand1);
		hand1.toKeyString();
		assertEquals(copy, hand1);
	}

	public void testHasFlushDrawWithMiddleOneCard() {
		hand1.setHand("AcAd/Ks/TcTh", deck);
		assertTrue(hand1.hasFlushDraw());
	}
	
	public void testHasFlushDrawWithMiddleSuited() {
		hand1.setHand("AcAd/KhQhJh/TcTh", deck);
		assertTrue(hand1.hasFlushDraw());
	}
	
	public void testHasFlushDrawWithBackOneCard() {
		hand1.setHand("AcAd/TcTh/Ks", deck);
		assertTrue(hand1.hasFlushDraw());
	}

	public void testHasFlushDrawWithBack() {
		hand1.setHand("AcAd/TcTh/KhQhJh", deck);
		assertTrue(hand1.hasFlushDraw());
	}
	
	public void testNoFlushDraw() {
		hand1.setHand("AcAd/KcKd/QcQd", deck);
		assertFalse(hand1.hasFlushDraw());
	}
	
	public void testNoFlushDrawEmptyFront() {
		hand1.setHand("/KcKd/QcQd", deck);
		assertFalse(hand1.hasFlushDraw());
	}
	
	public void testLiveFlushDrawsBack() {
		assertLive(ALL_SUITS);

		addMiddle1("Ac"); // kill the middle flush draw
		addMiddle1("2d");		
		assertLive(ALL_SUITS);
		
		addBack1("5s");
		assertLive(SUIT_SPADES);
		addBack1("5d");
		assertLive();
	}

	public void testLiveFlushDrawsMiddle2() {
		hand1.setHand("QcJh/9s4s2s/KsKd7d2d3d", deck);
		assertLive(SUIT_SPADES);
	}
	
	public void testLiveFlushDrawsMiddle() {
		assertLive(ALL_SUITS);
		
		addBack1("2c"); // kill the back flush draw
		addBack1("2h");
		
		assertLive(ALL_SUITS);
		
		addMiddle1("7h");
		assertLive(SUIT_HEARTS);
		addMiddle1("2s");
		assertLive();
	}

	public void testLiveFlushDrawsEmptyMiddle() {
		hand1.setHand("AcKcQd//8h7s6h5h4h", deck);
		assertLive(ALL_SUITS);
	}

	public void testLiveFlushDrawsEmptyBack() {
		hand1.setHand("AcKcQd/8h7s6h5h4h/", deck);
		assertLive(ALL_SUITS);
	}
	
	public void testFillingMiddleWithFlushKillsLiveFlushDraw() {
		setHand1("AcKd/9d8d7d6d/6s5c");
		assertLive(SUIT_DIAMONDS);
		addMiddle1("5d"); //
		assertLive();
	}

	public void testFillingMiddleWithoutFlushKillsLiveFlushDraw() {
		setHand1("AcKd/9d8d7d6d/6s5c");
		assertLive(SUIT_DIAMONDS);
		addMiddle1("5h"); //
		assertLive();
	}
	
	// TODO: many more tests, especially for scoring
	public void testFouled() {
		
	}

	// TODO: poorly named, but need a lot more tests around fouling anyways
	protected void foulHand1() {
		ch1 = LongCompleteOfcHand.createComplete("2c3c4c/AdKd4d3d2d/AsKs4s3s2s");
	}
	
	// too lazy to type
	protected void addFront1(String card) {
		hand1.addFront(new OfcCard(card));
	}

	protected void addMiddle1(String card) {
		hand1.addMiddle(new OfcCard(card));
	}
	
	protected void addBack1(String card) {
		hand1.addBack(new OfcCard(card));
	}

	protected void setHand1(String hand) {
		hand1.setHand(hand, deck);
	}
	
	private void assertLive(Integer... suits) {
		for (int suit : ALL_SUITS) {
			if (Arrays.asList(suits).contains(suit)) {
				assertTrue(hand1.liveFlushDraws()[suit]);
			} else {
				assertFalse(hand1.liveFlushDraws()[suit]);
			}
		}
	}
}
