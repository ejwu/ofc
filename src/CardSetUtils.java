import java.util.List;

import org.pokersource.game.Deck;

import com.google.common.collect.Lists;


public class CardSetUtils {
	// Cache of cardMask by rank and suit
	private static long[][] cardMasks;
	

	private static long[] suitMasks;
	
	static {
		cardMasks = new long[13][4];
		suitMasks = new long[4];
		for (int r = Deck.RANK_ACE; r >= 0; r--) {	
			for (int s = Deck.SUIT_COUNT - 1; s >= 0; s--) {
				long card = Deck.createCardMask(r, s);
				cardMasks[r][s] = card;
				suitMasks[s] |= card;
			}
		}
		
	}
	
	/**
	 * Convert the mask to arrays of ranks and suits in descending rank order
	 */
	public static void convertToArrays(long mask, int[] ranks, int[] suits) {
		int index = 0;
		for (int r = Deck.RANK_ACE; r >= 0; r--) {	
			for (int s = Deck.SUIT_COUNT - 1; s >= 0; s--) {
				long m = cardMasks[r][s];
				if ((mask & m) != 0) {
					ranks[index] = r;
					suits[index] = s;
					index++;
		        }
			}
		}
	}

	public static boolean hasFlushDraw(long mask) {
		boolean hasCard = false;
		for (int i = 0; i < suitMasks.length; i++) {
			if ((mask & suitMasks[i]) != 0L) {
				if (hasCard) {
					return false; // we've seen a card not of this suit
				} else {
					hasCard = true; // If this is the first card we've seen, flush draw is still live
				}
			}
		}
		return true;
	}
	
	public static OfcCard[] asCards(long mask) {
		OfcCard[] cards = new OfcCard[Long.bitCount(mask)];
		int i = 0;
		for (int r = Deck.RANK_ACE; r >= 0; r--) {	
			for (int s = Deck.SUIT_COUNT - 1; s >= 0; s--) {
				long m = cardMasks[r][s];
				if ((mask & m) != 0) {
					cards[i++] = new OfcCard(m);
		        }
			}
		}
		return cards;
	}
	
}
