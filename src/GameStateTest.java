import junit.framework.TestCase;


public class GameStateTest extends TestCase {

	public GameStateTest(String name) {
		super(name);
	}

	
	public void testFileString() {
		String fileString = "11.5 6h2h-Js9s8h-KsKdTsTc2s Td2c2d-KcQd7s-AsAdAhJdJh -1.7013370235005392";
		OfcHand hand1 = new LongOfcHand();
		hand1.addFront(new OfcCard("6h"));
		hand1.addFront(new OfcCard("2h"));

		hand1.addMiddle(new OfcCard("Js"));
		hand1.addMiddle(new OfcCard("9s"));
		hand1.addMiddle(new OfcCard("8h"));
		
		hand1.addBack(new OfcCard("Ks"));
		hand1.addBack(new OfcCard("Kd"));
		hand1.addBack(new OfcCard("Ts"));
		hand1.addBack(new OfcCard("Tc"));
		hand1.addBack(new OfcCard("2s"));

		OfcHand hand2 = new LongOfcHand();
		hand2.addFront(new OfcCard("Td"));
		hand2.addFront(new OfcCard("2c"));
		hand2.addFront(new OfcCard("2d"));
		
		hand2.addMiddle(new OfcCard("Kc"));
		hand2.addMiddle(new OfcCard("Qd"));
		hand2.addMiddle(new OfcCard("7s"));

		hand2.addBack(new OfcCard("As"));
		hand2.addBack(new OfcCard("Ad"));
		hand2.addBack(new OfcCard("Ah"));
		hand2.addBack(new OfcCard("Jd"));
		hand2.addBack(new OfcCard("Jh"));
		
		GameState state1 = new GameState(hand1, hand2, new OfcDeck(GameState.deriveDeck(hand1, hand2)));
		GameState state2 = GameState.fromFileString(fileString);
		
		assertEquals(state1, state2);
	}
	
	/*
	 * Golden values:
	 * 	player1.setHand("Kd3hKs/As2d9d/JdJc7c7d", deck);
		player2.setHand("QcAd/6d7sKc/AhKhQhJhTh", deck);
	 *  -11.661805
	 * 
	 */
	
	
}
