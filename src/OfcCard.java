import java.util.Map;

import org.pokersource.game.Deck;

import com.google.common.collect.Maps;


public class OfcCard {

	private static final Map<Character, Integer> rankMap = Maps.newHashMap();
	private static final Map<Character, Integer> suitMap = Maps.newHashMap();
	
	static {
		rankMap.put('A', Deck.RANK_ACE);
		rankMap.put('K', Deck.RANK_KING);
		rankMap.put('Q', Deck.RANK_QUEEN);
		rankMap.put('J', Deck.RANK_JACK);
		rankMap.put('T', Deck.RANK_TEN);
		rankMap.put('9', Deck.RANK_9);
		rankMap.put('8', Deck.RANK_8);
		rankMap.put('7', Deck.RANK_7);
		rankMap.put('6', Deck.RANK_6);
		rankMap.put('5', Deck.RANK_5);
		rankMap.put('4', Deck.RANK_4);
		rankMap.put('3', Deck.RANK_3);
		rankMap.put('2', Deck.RANK_2);

		suitMap.put('c', Deck.SUIT_CLUBS);
		suitMap.put('d', Deck.SUIT_DIAMONDS);
		suitMap.put('h', Deck.SUIT_HEARTS);
		suitMap.put('s', Deck.SUIT_SPADES);
	}
	
	private String card;
	
	public OfcCard(String card) {
		this.card = card;
		
		if (card == null) {
			throw new IllegalArgumentException("Card must not be null");
		}
		
		if (card.length() != 2) {
			throw new IllegalArgumentException("Card string must be length 2");
		}
		
		if (!rankMap.containsKey(card.charAt(0))) {
			throw new IllegalArgumentException("Invalid rank");
		}
		
		if (!suitMap.containsKey(card.charAt(1))) {
			throw new IllegalArgumentException("Invalid suit");
		}
	}

	public int getRank() {
		return rankMap.get(card.charAt(0));
	}
	
	public int getSuit() {
		return suitMap.get(card.charAt(1));
	}

	public long getMask() {
		return Deck.createCardMask(getRank(), getSuit());
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((card == null) ? 0 : card.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OfcCard other = (OfcCard) obj;
		if (card == null) {
			if (other.card != null)
				return false;
		} else if (!card.equals(other.card))
			return false;
		return true;
	}

	
	public String toString() {
		return card;
	}
	
}
