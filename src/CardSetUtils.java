import java.util.List;

import org.pokersource.game.Deck;

import com.google.common.collect.Lists;


public class CardSetUtils {

	/**
	 * Convert the mask to arrays of ranks and suits in descending rank order
	 */
	public static void convertToArrays(long mask, int[] ranks, int[] suits) {
		int index = 0;
		for (int r = Deck.RANK_ACE; r >= 0; r--) {	
			for (int s = Deck.SUIT_COUNT - 1; s >= 0; s--) {
				long m = Deck.createCardMask(r, s);
				if ((mask & m) != 0) {
					ranks[index] = r;
					suits[index] = s;
					index++;
		        }
			}
		}
	}

	public static List<OfcCard> asCards(long mask) {
		List<OfcCard> cards = Lists.newArrayListWithCapacity(Long.bitCount(mask));
		for (int r = Deck.RANK_ACE; r >= 0; r--) {	
			for (int s = Deck.SUIT_COUNT - 1; s >= 0; s--) {
				long m = Deck.createCardMask(r, s);
				if ((mask & m) != 0) {
					cards.add(new OfcCard(m));
		        }
			}
		}
		return cards;
	}
	
}
