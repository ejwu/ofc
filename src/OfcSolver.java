
public class OfcSolver {
//spurious
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

//		System.out.println(System.mapLibraryName("poker"));
//		System.load("/cygdrive/c/Users/ejwu/Desktop/workspace/ofc/poker.dll");
//	System.out.println(System.getProperty("java.library.path"));
//		System.loadLibrary("poker");
		
		OfcDeck deck = new OfcDeck();
		deck.initialize();
		
		OfcHand player1 = new LongOfcHand();
		player1.addFront(deck.removeCard("2d"));
		player1.addFront(deck.removeCard("Td"));
		player1.addFront(deck.removeCard("2c"));
		
		player1.addMiddle(deck.removeCard("7s"));
//		player1.addMiddle(deck.removeCard("7c"));
		player1.addMiddle(deck.removeCard("Qd"));
		//player1.addMiddle(deck.removeCard("7h"));
		player1.addMiddle(deck.removeCard("Kc"));

		player1.addBack(deck.removeCard("Ah"));
		player1.addBack(deck.removeCard("Jh"));
//		player1.addBack(deck.removeCard("Jd"));
		player1.addBack(deck.removeCard("Ad"));
		
		OfcHand player2 = new LongOfcHand();
		player2.addFront(deck.removeCard("6h"));
		player2.addFront(deck.removeCard("2h"));
//		player2.addFront(deck.removeCard("8c"));
		
//		player2.addMiddle(deck.removeCard("8d"));
//		player2.addMiddle(deck.removeCard("8s"));
		player2.addMiddle(deck.removeCard("8h"));
		player2.addMiddle(deck.removeCard("Js"));
		player2.addMiddle(deck.removeCard("9s"));
		
		player2.addBack(deck.removeCard("Tc"));
		player2.addBack(deck.removeCard("Ts"));
		player2.addBack(deck.removeCard("Kd"));
		player2.addBack(deck.removeCard("Ks"));
		player2.addBack(deck.removeCard("2s"));

		
		/*	
		OfcHand player3 = new OfcHand();
		player3.addFront(deck.removeCard("4s"));
		player3.addFront(deck.removeCard("Qs"));
		player3.addFront(deck.removeCard("7d"));
		
		player3.addMiddle(deck.removeCard("9h"));
		player3.addMiddle(deck.removeCard("5h"));
		player3.addMiddle(deck.removeCard("9d"));
		player3.addMiddle(deck.removeCard("3s"));
		
		player3.addBack(deck.removeCard("Ac"));
		player3.addBack(deck.removeCard("Qc"));
		player3.addBack(deck.removeCard("7c"));
		player3.addBack(deck.removeCard("6c"));
*/
		GameState gs = new GameState(player1, player2, deck);
		System.out.println(gs);

		long timestamp = System.currentTimeMillis();	
		System.out.println(gs.getValue());
		System.out.println((System.currentTimeMillis() - timestamp) / 1000 + " seconds");
	}
	
}
