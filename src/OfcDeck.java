import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.pokersource.game.Deck;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class OfcDeck {

	private long cardMask;
	
	public OfcDeck() {
		cardMask = 0L;
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
		
	public long withoutCard(OfcCard card) {
		return withoutCard(card.mask);
	}

	public long withoutCard(long cardBit) {
		if ((cardMask & cardBit) == 0) {
			throw new IllegalArgumentException("Card not in deck");
		}

		return cardMask ^ cardBit;
	}
	
	public OfcCard removeCard(String cardString) {		
		OfcCard card = new OfcCard(Deck.parseCardMask(cardString));
		cardMask = withoutCard(card);
		return card;
	}
	
	public OfcCard[] toArray() {
		return CardSetUtils.asCards(cardMask);
	}
	
	/**
	 * Return a count of the number of cards in the deck for each rank.  Key to the map is the first card seen of that rank.
	 * @return
	 */
	public Map<OfcCard, Integer> byRankCount() {
		Map<OfcCard, Integer> rankCount = Maps.newHashMap();
		Map.Entry<OfcCard, Integer> currentEntry = null;
		for (OfcCard card : CardSetUtils.asCards(cardMask)) {
			if (currentEntry != null && currentEntry.getKey().getRank() == card.getRank()) {
				currentEntry.setValue(currentEntry.getValue() + 1);
			} else {
				rankCount.put(card,  1);
				for (Map.Entry<OfcCard, Integer> entry : rankCount.entrySet()) {
					if (entry.getKey().getRank() == card.getRank()) {
						currentEntry = entry;
					}
				}
			}
		}
		return rankCount;
	}
	
// TODO: write some tests for this, given how badly I messed this up
	public List<List<OfcCard>> byRank() {
		List<List<OfcCard>> list = Lists.newArrayList();

		int previousRank = -1;
		List<OfcCard> rankList = Lists.newArrayList();
		for (OfcCard card : CardSetUtils.asCards(cardMask)) {
			if (card.getRank() == previousRank) {
				rankList.add(card);
			} else {
				if (!rankList.isEmpty()) {
					list.add(rankList);
				}
				rankList = Lists.newArrayList(card);
				previousRank = card.getRank();
			}
		}
		if (!rankList.isEmpty()) {
			list.add(rankList);
		}
		
		return list;
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
