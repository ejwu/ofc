import java.util.List;

import com.google.common.collect.Lists;

import junit.framework.TestCase;


public class StupidEvalTest extends TestCase {

	private int[] ranks = new int[5];
	private int[] suits = new int[5];
	private List<OfcCard> hand = Lists.newArrayListWithExpectedSize(5);
	
	public StupidEvalTest(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	private long eval() {
		return StupidEval.eval(ranks, suits);
	}
	
	// space delimited hand
	private void makeHand(String handString) {
		hand.clear();
		for (String card : handString.split(" ")) {
			hand.add(new OfcCard(card));
		}
		OfcHand.convertForEval(hand, ranks, suits);
	}
	
	private long evalHand(String handString) {
		makeHand(handString);
		return eval();
	}
	
	private long eval3(String handString) {
		makeHand(handString);
		return StupidEval.eval3(ranks);
	}
	
	public void testRoyal() {
		makeHand("Ac Kc Qc Jc Tc");
		assertEquals(StupidEval.ROYAL_FLUSH, eval());
	}
	
	public void testRoyalsAreEqual() {
		makeHand("Ad Kd Qd Jd Td");
		long diamondRoyal = eval();
		makeHand("As Ks Qs Js Ts");
		assertEquals(diamondRoyal, eval());
	}
	
	public void testHandBecomesSorted() {
		makeHand("Qh Th Jh Ah Kh");
		assertEquals(StupidEval.ROYAL_FLUSH, eval());
	}
	
	public void testRoyalBeatsStraightFlush() {
		makeHand("Ah Kh Qh Jh Th");
		long royal = eval();
		makeHand("Kh Qh Jh Th 9h");
		assertTrue(royal > eval());
	}
	
	public void test5HighStraightFlush() {
		makeHand("Ah 2h 3h 4h 5h");
		long value = eval();
		assertTrue(value >= StupidEval.STRAIGHT_FLUSH);
		assertTrue(value < StupidEval.ROYAL_FLUSH);
	}
	
	public void testStraightFlush() {
		makeHand("Kh Qh Jh Th 9h");
		long khigh = eval();
		assertTrue(khigh > StupidEval.STRAIGHT_FLUSH);
		assertTrue(khigh < StupidEval.ROYAL_FLUSH);
		makeHand("Ah 2h 3h 4h 5h");
		assertTrue(khigh > eval());
	}
	
	public void testQuads() {
		makeHand("Ah Ac Ad As Ks");
		long quads = eval();
		assertTrue(quads >= StupidEval.QUADS);
		assertTrue(quads < StupidEval.STRAIGHT_FLUSH);
	}
	
	public void testQuadsComparison() {
		makeHand("Jh Jc Jd Js 2d");
		long quadJ = eval();
		makeHand("Th Tc Td Ts Ad");
		long quadT = eval();
		assertTrue(quadJ > quadT);
	}
	
	public void testFullHouse() {
		makeHand("Ac Ad Ah Kh Kd");
		long boat = eval();
		assertTrue(boat >= StupidEval.FULL_HOUSE);
		assertTrue(boat < StupidEval.QUADS);
	}
	
	public void testFullHouseComparison() {
		makeHand("Ac Ad Ah 2h 2d");
		long acesFull = eval();
		makeHand("Ac Ad 2h 2d 2s");
		long deucesFull = eval();
		assertTrue(deucesFull >= StupidEval.FULL_HOUSE);
		assertTrue(acesFull > deucesFull);
	}
	
	public void testFlush() {
		makeHand("Ac Kc Qc Jc 9c");
		long flush = eval();
		assertTrue(flush >= StupidEval.FLUSH);
		assertTrue(flush < StupidEval.FULL_HOUSE);
	}
	
	public void testFlushComparison() {
		makeHand("Ac Kc Qc Jc 9c");
		long highFlush = eval();
		makeHand("7c 5c 4c 3c 2c");
		long lowFlush = eval();
		assertTrue(lowFlush >= StupidEval.FLUSH);
		assertTrue(highFlush > lowFlush);
	}
	
	public void testStraight() {
		makeHand("Ac Kc Qc Jc Td");
		long straight = eval();
		assertTrue(straight >= StupidEval.STRAIGHT);
		assertTrue(straight < StupidEval.FLUSH);
	}
	
	public void testStraightComparison() {
		makeHand("Ac Kd Qs Jh Tc");
		long broadway = eval();
		makeHand("Ac 2c 3c 4h 5s");
		long wheel = eval();
		makeHand("2h 3h 4c 5c 6s");
		long sixHigh = eval();
		assertTrue(sixHigh >= StupidEval.STRAIGHT);
		assertTrue(wheel >= StupidEval.STRAIGHT);
		assertTrue(broadway > sixHigh);
		assertTrue(sixHigh > wheel);
	}
	
	public void testTrips() {
		makeHand("Ac Ad As Ks Qd");
		long aces = eval();
		assertTrue(aces >= StupidEval.TRIPS);
		assertTrue(aces < StupidEval.STRAIGHT);
		
		makeHand("Ac Kd Ks Kh Qh");
		long kings = eval();
		assertTrue(kings >= StupidEval.TRIPS);
		assertTrue(aces > kings);
		
		makeHand("Kh Jd 2c 2d 2h");
		long deuces = eval();
		assertTrue(deuces >= StupidEval.TRIPS);
		assertTrue(kings > deuces);
	}
	
	public void testTwoPair() {
		makeHand("Ac Ad Kh Kd Qd");
		long twoPair = eval();
		assertTrue(twoPair >= StupidEval.TWO_PAIR);
		assertTrue(twoPair < StupidEval.TRIPS);
	}
	
	public void testTwoPairKickers() {
		makeHand("Tc Td Ac 4h 4c");
		long aceKicker = eval();
		assertTrue(aceKicker >= StupidEval.TWO_PAIR);
		
		makeHand("Tc Td 8c 4h 4c");
		long eightKicker = eval();
		assertTrue(aceKicker > eightKicker);
		
		makeHand("Tc Td 2c 4h 4c");
		long deuceKicker = eval();
		assertTrue(eightKicker > deuceKicker);
		assertTrue(deuceKicker >= StupidEval.TWO_PAIR);
	}

	public void testTwoPairFirstPairComparison() {
		makeHand("Kc Kd 3h 3d 2d");
		long kingsUp = eval();
		makeHand("Qc Qd 3h 3d 2d");
		long queensUp = eval();
		assertTrue(kingsUp > queensUp);
		makeHand ("Qc Qd Jh Jd Td");
		queensUp = eval();
		assertTrue(kingsUp > queensUp);
	}
	
	public void testTwoPairSecondPairComparison() {
		makeHand("Kc Kd Jh Jd 2c");
		long jacks = eval();
		makeHand("Kc Kd Th Td 2c");
		long tens = eval();
		assertTrue(jacks > tens);
	}

	public void testOnePairBounds() {
		makeHand("Ac Ad Kd Qd Jd");
		long aces = eval();
		assertTrue(aces >= StupidEval.ONE_PAIR);
		assertTrue(aces < StupidEval.TWO_PAIR);
		makeHand("2c 2d 3d 4h 5s");
		long deuces = eval();
		assertTrue(aces > deuces);
		assertTrue(deuces >= StupidEval.ONE_PAIR);
	}
	
	public void testOnePairComparisons() {
		long highKickers = evalHand("4h 4d Ac Kc Qc");
		long lowKickers = evalHand("5h 5d 4d 3d 2d");
		assertTrue(lowKickers > highKickers);
	}
	
	public void testOnePairKickers() {
		makeHand("8c 8d Ac Kc Qc");
		long akq = eval();
		makeHand("8c 8d Ac Kc Qd");
		long akq2 = eval();
		assertEquals(akq, akq2);
		makeHand("8c 8d Ac Kc Jd");
		long akj = eval();
		assertTrue(akq > akj);
		makeHand("8c 8d Ac Kc 7d");
		long ak7 = eval();
		assertTrue(akj > ak7);
		makeHand("8c 8d Ac 7d 6d");
		long a76 = eval();
		assertTrue(ak7 > a76);
		long kqj = evalHand("8c 8d Kc Qc Jc");
		assertTrue(a76 > kqj);
		long kq2 = evalHand("8c 8d Kc Qc 2c");
		assertTrue(kqj > kq2);
		long k32 = evalHand("8c 8d Kc 3c 2c");
		assertTrue(kq2 > k32);
		long seven = evalHand("8c 8d 7c 6c 5c");
		assertTrue(k32 > seven);
		long four = evalHand("8c 8d 4c 3c 2c");
		assertTrue(seven > four);
	}
	
	// TODO: no pair tests
	
	///////////////3 card tests
	
	public void test3Trips() {
		long tripA = eval3("Ac Ad As");
		assertTrue(tripA >= StupidEval.TRIPS);
		long tripA2 = eval3("As Ah Ad");
		assertEquals(tripA, tripA2);
		long tripK = eval3("Ks Kh Kd");
		assertTrue(tripA > tripK);
		long trip2 = eval3("2c 2d 2h");
		assertTrue(tripK > trip2);
		assertTrue(trip2 >= StupidEval.TRIPS);
	}

	public void test3Pair() {
		long acesk = eval3("Ac Ad Kd");
		assertTrue(acesk < StupidEval.TRIPS);
		long acesk2 = eval3("Ac Ad Ks");
		assertEquals(acesk, acesk2);
		long acesq = eval3("Ac Ad Qs");
		assertTrue(acesk > acesq);
		long kings = eval3("Kc Kd Qs");
		assertTrue(acesq > kings);
		long deucesHigh = eval3("Ac 2c 2d");
		assertTrue(kings > deucesHigh);
		long deucesLow = eval3("2c 3h 2d");
		assertTrue(deucesHigh > deucesLow);
		assertTrue(deucesLow >= StupidEval.ONE_PAIR);
	}

	public void test3NoPair() {
		long akq = eval3("Ac Kc Qc");
		assertTrue(akq < StupidEval.ONE_PAIR);
		long akq2 = eval3("Ad Ks Qh");
		assertEquals(akq, akq2);
		long akj = eval3("Ad Kd Jd");
		assertTrue(akq2 > akj);
		long a32 = eval3("Ad 3s 2d");
		assertTrue(akj > a32);
		long kqj = eval3("Ks Qs Js");
		assertTrue(a32 > kqj);
		long nutFour = eval3("4s 3d 2h");
		assertTrue(kqj > nutFour);
		assertTrue(nutFour >= StupidEval.NO_PAIR);
	}

	// 5 vs 3 card hand tests
	
}
