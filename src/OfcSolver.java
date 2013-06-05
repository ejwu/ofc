
public class OfcSolver {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		OfcDeck deck = new OfcDeck();
		deck.initialize();
		
		OfcHand player1 = new LongOfcHand();
		OfcHand player2 = new LongOfcHand();

		player2.addFront(deck.removeCard("Qd"));
		player2.addFront(deck.removeCard("7s"));
		
		player2.addMiddle(deck.removeCard("2d"));
		player2.addMiddle(deck.removeCard("4h"));
		player2.addMiddle(deck.removeCard("3s"));
		player2.addMiddle(deck.removeCard("5h"));

		player2.addBack(deck.removeCard("8c"));
		player2.addBack(deck.removeCard("8s"));
		player2.addBack(deck.removeCard("9c"));
		player2.addBack(deck.removeCard("8d"));
		player2.addBack(deck.removeCard("7d"));

		player1.addFront(deck.removeCard("Qs"));
		player1.addFront(deck.removeCard("Kd"));
		
		player1.addMiddle(deck.removeCard("Ac"));
		player1.addMiddle(deck.removeCard("2h"));
		player1.addMiddle(deck.removeCard("As"));
		player1.addMiddle(deck.removeCard("3d"));
		
		player1.addBack(deck.removeCard("5s"));
		player1.addBack(deck.removeCard("5d"));
		player1.addBack(deck.removeCard("Th"));
		player1.addBack(deck.removeCard("Td"));

		
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
		System.out.println(gs.getValue(Scorers.getScorers()));
		System.out.println((System.currentTimeMillis() - timestamp) / 1000 + " seconds");
	}
	
}
