import java.util.Arrays;



public class OfcSolver {

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
		
		System.out.println(deck);
		System.out.println(deck);

		OfcHand player1 = new OfcHand();
		player1.addFront(deck.removeCard("2d"));
		player1.addFront(deck.removeCard("Td"));
		player1.addFront(deck.removeCard("2c"));
		
		player1.addMiddle(deck.removeCard("7s"));
		player1.addMiddle(deck.removeCard("5s"));
		player1.addMiddle(deck.removeCard("Qd"));
		player1.addMiddle(deck.removeCard("7h"));
		player1.addMiddle(deck.removeCard("Kc"));

		player1.addBack(deck.removeCard("Ah"));
		player1.addBack(deck.removeCard("Jh"));
		player1.addBack(deck.removeCard("Jd"));
		player1.addBack(deck.removeCard("Ad"));
		
		OfcHand player2 = new OfcHand();
		player2.addFront(deck.removeCard("6h"));
		player2.addFront(deck.removeCard("2h"));
		player2.addFront(deck.removeCard("8c"));
		
		player2.addMiddle(deck.removeCard("8d"));
		player2.addMiddle(deck.removeCard("8s"));
		player2.addMiddle(deck.removeCard("3c"));
		player2.addMiddle(deck.removeCard("Js"));
		player2.addMiddle(deck.removeCard("9s"));
		
		player2.addBack(deck.removeCard("Tc"));
		player2.addBack(deck.removeCard("Ts"));
		player2.addBack(deck.removeCard("Kd"));
		player2.addBack(deck.removeCard("Ks"));
		
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

		System.out.println(deck);
		System.out.println(player1);
		System.out.println(player2);
		System.out.println(player3);		

		player1.addBack(deck.removeCard("As"));
		System.out.println(player1);
		System.out.println(player1.getRoyaltyValue());
		System.out.println(player1.getBackRank());
		System.out.println(player1.getMiddleRank());
		System.out.println(player1.getFrontRank());
	}
	
}
