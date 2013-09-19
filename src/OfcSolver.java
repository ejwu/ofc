import java.util.Arrays;


public class OfcSolver {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		OfcDeck deck = new OfcDeck();
		deck.initialize();
		
		OfcHand player1 = new LongOfcHand();
		OfcHand player2 = new LongOfcHand();

		player1.setHand("Kc7h/Ac4h3c6d/Td5d5s5c", deck);
		player2.setHand("QcJh/9s4s2s/KsKd7d2d3d", deck);
		//assertValue(6.12212125d);
		
		GameState gs = new GameState(player1, player2, deck);
		System.out.println(gs);

		long timestamp = System.currentTimeMillis();	
		System.out.println(gs.getValue(Scorers.getScorer()));
		System.out.println((System.currentTimeMillis() - timestamp) + " ms");
		gs.shutdown();
	}
	
}
