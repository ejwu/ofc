import com.google.common.collect.*;

import org.pokersource.game.*;

public class OfcDeck {

	private long cardMask = 0L;
	
	public OfcDeck() {
	}
	
	// copy a deck from a card mask
	public OfcDeck(long cardMask) {
		this.cardMask = cardMask;
	}
	
	public void initialize() {
		for (String rank : Lists.newArrayList("2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K", "A")) {
			for (String suit : Lists.newArrayList("c", "d", "h", "s")) {
				cardMask |= Deck.parseCardMask(rank + suit);
			}
		}
	}

	public long getMask() {
		return cardMask;
	}
		
	public long withoutCard(String card) {
		long cardBit = Deck.parseCardMask(card);
		if ((cardMask & cardBit) == 0) {
			throw new IllegalArgumentException("Card not in deck");
		}

		return cardMask ^ cardBit;
	}
	
	// TODO: this returns array of length 1 for an empty deck, seems dumb
	public String[] allCards() {
		return Deck.cardMaskString(cardMask).split(" ");
	}
	
	public OfcCard removeCard(String card) {		
		cardMask = withoutCard(card);
		return new OfcCard(card);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (cardMask ^ (cardMask >>> 32));
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
		OfcDeck other = (OfcDeck) obj;
		if (cardMask != other.cardMask)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return Deck.cardMaskString(cardMask);
	}
}
