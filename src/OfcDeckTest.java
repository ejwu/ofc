

import java.util.Arrays;

import org.pokersource.game.Deck;

import com.google.common.collect.Lists;

import junit.framework.TestCase;


public class OfcDeckTest extends TestCase {
	public OfcDeckTest(String name) {
		super(name);
	}
	
	public void testInitialize() {
		OfcDeck deck = new OfcDeck();
		// TODO: this seems like a bad idea
		assertEquals(1, deck.allCards().length);
		assertEquals("", deck.toString());
		deck.initialize();
		assertEquals(52, deck.allCards().length);
		for (String rank : Lists.newArrayList("2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K", "A")) {
			for (String suit : Lists.newArrayList("c", "d", "h", "s")) {
				assertTrue(deck.toString().contains(rank + suit));
			}
		}
	}
	
	public void testRemoveCardTwice() {
		OfcDeck deck = new OfcDeck();
		deck.initialize();		
		deck.removeCard("Ks");
		assertEquals(51, deck.allCards().length);
		try {
			deck.removeCard("Ks");
			assertEquals("", deck.toString());
			fail("Shouldn't be able to removeCard twice");
		} catch (Exception expected) {
		}
	}
}
