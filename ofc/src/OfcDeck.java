import com.google.common.collect.*;

import org.pokersource.game.*;

public class OfcDeck {

	private long cardMask = 0L;
	
	public OfcDeck() {
	}
	
	public void initialize() {
		for (String rank : Lists.newArrayList("2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K", "A")) {
			for (String suit : Lists.newArrayList("c", "d", "h", "s")) {
				cardMask += Deck.parseCardMask(rank + suit);
			}
		}
	}
	
	public OfcCard removeCard(String card) {
		long oldCardMask = cardMask;
		cardMask -= Deck.parseCardMask(card);
		if (cardMask != oldCardMask) {
			return new OfcCard(card);
		}
		throw new IllegalArgumentException("card not in deck");
	}
	
	@Override
	public String toString() {
		return Deck.cardMaskString(cardMask);
	}
}
