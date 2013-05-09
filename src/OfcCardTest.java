import org.pokersource.game.Deck;

import junit.framework.TestCase;


public class OfcCardTest extends TestCase {

	public OfcCardTest(String name) {
		super(name);
	}

	public void testRanksAndSuits() {
		for (String rank : new String[] {"2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K", "A"}) {
			for (String suit : new String[] {"c", "d", "h", "s"}) {
				OfcCard card = new OfcCard(Deck.parseCardMask(rank + suit));
				
				assertEquals(Deck.parseRank(rank), card.getRank());
				assertEquals(Deck.parseSuit(suit), card.getSuit());
			}
		}
	}
	
}
