import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

/**
 * A collection of slow tests to verify that the values for a game state remain the same.
 *
 * Random timing notes:
 * 1-8: 278s on desktop.
 * 2: 10s
 * 7: 23s
 * 8: 223s
 */
public class SlowGameStateTest extends TestCase {

	private static final double EPSILON = 0.00000001d;
	
	OfcDeck deck;
	OfcHand player1;
	OfcHand player2;
	GameState state;
	
	public static final Scorers.Scorer FL_SCORER = new Scorers.NewFantasylandScorer();

	public SlowGameStateTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		deck = new OfcDeck();
		deck.initialize();
		player1 = new LongOfcHand();
		player2 = new LongOfcHand();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void test1() {
		player1.setHand("Kd3hKs/As2d9d/JdJc7c7d", deck);
		player2.setHand("QcAd/6d7sKc/AhKhQhJhTh", deck);
		assertValue(-11.66180548d);
	}
	
	public void test2() {
		player1.setHand("KhJd/Ac9d5c/7s7h6d6c3c", deck);
		player2.setHand("9h2s/QsJhTc6s2h/Kd8d3d", deck);
		assertValue(4.97738741d);
	}

	public void test3() {
		player1.setHand("9h4s/AdAsKd4c/ThTc8h7c", deck);
		player2.setHand("QdQs/KcKhJc8d/6c6d2s2c", deck);
		assertValue(-23.12123020d);
	}
	
	public void test4() {
		player1.setHand("KcQh/AcKdTd9c/6s2s2h7c", deck);
		player2.setHand("QsTh/6c8d9h/JhJc4s4c5h", deck);
		assertValue(0.74548324d);
	}
	
	// same as 4, except for the Kd placement
	public void test5() {
		player1.setHand("KcQhKd/AcTd9c/6s2s2h7c", deck);
		player2.setHand("QsTh/6c8d9h/JhJc4s4c5h", deck);
		assertValue(-2.05499523d);
	}

	public void test6() {
		player1.setHand("Kd6d/3s7h9h3d/JsJhJc5h", deck);
		player2.setHand("5c/6c2s4s6h/9s9dTsJdQc", deck);
		assertValue(3.99854411d);
	}
	
	public void test7() {
		player1.setHand("Kc7h/Ac4h3c6d/Td5d5s5c", deck);
		player2.setHand("QcJh/9s4s2s/KsKd7d2d3d", deck);
		assertValue(6.12212125d);
	}
	
	// same as 7, except for Ks
	public void test8() {
		player1.setHand("Kc7h/Ac4h3c6d/Td5d5s5c", deck);
		player2.setHand("QcJh/Ks9s4s2s/Kd7d2d3d", deck);
		assertValue(3.98300769d);
	}

	private void assertValue(double expected) {
		state = new GameState(player1, player2, deck);
		assertEquals(expected, state.getValue(FL_SCORER), EPSILON);
	}
	
}
