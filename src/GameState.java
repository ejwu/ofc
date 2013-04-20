import java.util.Set;

import com.google.common.collect.Sets;


public class GameState {
	// The current player
	OfcHand player1;
	OfcHand player2;
	OfcDeck deck;
	
	public GameState(OfcHand player1, OfcHand player2, OfcDeck deck) {
		this.player1 = player1;
		this.player2 = player2;
		this.deck = deck;
	}
	
	public Set<GameState> generateNewStates() {
		Set<GameState> newStates = Sets.newHashSet();
		for (String card : deck.allCards()) {
			for (OfcHand hand : player1.generateHands(new OfcCard(card))) {
				newStates.add(new GameState(player2, hand, new OfcDeck(deck.withoutCard(card))));
			}
		}
		return newStates;
	}

	public boolean isComplete() {
		return player1.isComplete() && player2.isComplete();
	}
	
	public int getValue() {
		if (!isComplete()) {
			throw new IllegalStateException("game must be complete");
		}
		return player1.scoreAgainst(player2);
	}
	
	@Override
	public String toString() {		
		StringBuilder sb = new StringBuilder();
		sb.append("Game state: \n");
		sb.append(player1);
		sb.append("\n");
		sb.append(player2);
		sb.append("\n");
		if (isComplete()) {
			sb.append(getValue() + " points\n");
		}
		sb.append(deck);
		return sb.toString();
	}
}
