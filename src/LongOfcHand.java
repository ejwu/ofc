import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.pokersource.game.Deck;

import com.google.common.annotations.VisibleForTesting;

public class LongOfcHand extends CachedValueOfcHand {

	@VisibleForTesting
	long front;
	@VisibleForTesting
	long middle;
	@VisibleForTesting
	long back;
	
	public LongOfcHand() {
		front = 0;
		middle = 0;
		back = 0;
	}
	
	private LongOfcHand(long front, long middle, long back) {
		this.front = front;
		this.middle = middle;
		this.back = back;
	}
	
	@Override
	public OfcHand copy() {
		return new LongOfcHand(front, middle, back);
	}

	@Override
	public int getBackSize() {
		return Long.bitCount(back);
	}
	
	@Override
	public int getMiddleSize() {
		return Long.bitCount(middle);
	}
	
	@Override
	public int getFrontSize() {
		return Long.bitCount(front);
	}
	
	@Override
	public void addBack(OfcCard card) {
		if (getBackSize() >= BACK_SIZE) {
			throw new IllegalStateException("Back already full");
		}
		if ((back & card.getMask()) != 0) {
			throw new IllegalArgumentException("Card already in back");
		}
		back |= card.getMask();
		
		if (getBackSize() == BACK_SIZE) {
			completeBack();
		}
	}

	@Override
	public void addMiddle(OfcCard card) {
		if (getMiddleSize() >= MIDDLE_SIZE) {
			throw new IllegalStateException("Middle already full");
		}
		if ((middle & card.getMask()) != 0) {
			throw new IllegalArgumentException("Card already in middle");
		}
		middle |= card.getMask();

		if (getMiddleSize() == MIDDLE_SIZE) {
			completeMiddle();
		}
	}

	@Override
	public void addFront(OfcCard card) {
		if (getFrontSize() >= FRONT_SIZE) {
			throw new IllegalStateException("Front already full");
		}
		if ((front & card.getMask()) != 0) {
			throw new IllegalArgumentException("Card already in front");
		}
		front |= card.getMask();

		if (getFrontSize() == FRONT_SIZE) {
			completeFront();
		}
	}

	@Override
	public boolean isComplete() {
		return getFrontSize() == FRONT_SIZE &&
				getMiddleSize() == MIDDLE_SIZE &&
				getBackSize() == BACK_SIZE;
	}

	@Override
	public boolean willBeFouled() {
		return willBeFouled;
	}

	@Override
	public boolean isFouled() {
		if (!isComplete()) {
			throw new IllegalStateException("Hand not complete");
		}
		// TODO: Be damn sure about this.  Making an assumption that when the hand is complete, willBeFouled is always populated
		// via the completeXXX methods
		return willBeFouled;
	}

	/**
	 * Fill the ranks and suits arrays with the value of the hand.
	 */
	@VisibleForTesting
	static void convertForEval(long mask, int[] ranks, int[] suits) {
		long numCards = Long.bitCount(mask);
		// TODO: be smarter about which hand we're checking
		if (numCards != FRONT_SIZE &&  numCards != BACK_SIZE) {
			throw new IllegalArgumentException("Hand isn't complete");
		}
		
		int index = 0;

		// NOTE: There's a hidden requirement here that the cards are sorted in descending order
		for (String card : Deck.cardMaskString(mask).split(" ")) {
			ranks[index] = Deck.parseRank(card.substring(0, 1));
			suits[index] = Deck.parseSuit(card.substring(1, 2));
			index++;
		}
	}
	
	@Override
	public long getFrontRank() {
		if (frontValue == UNSET) {
			if (getFrontSize() != FRONT_SIZE) {
				throw new IllegalStateException("Front not complete");
			}
			int[] ranks = new int[3];
			int[] suits = new int[3];
			convertForEval(front, ranks, suits);
			frontValue = StupidEval.eval3(ranks);
		}
		return frontValue;
	}

	@Override
	public long getMiddleRank() {
		if (middleValue == UNSET) {
			if (getMiddleSize() != MIDDLE_SIZE) {
				throw new IllegalStateException("Middle not complete");
			}
			int[] ranks = new int[5];
			int[] suits = new int[5];
			convertForEval(middle, ranks, suits);
			middleValue = StupidEval.eval(ranks, suits);
		}
		return middleValue;
	}

	@Override
	public long getBackRank() {
		if (backValue == UNSET) {
			if (getBackSize() != BACK_SIZE) {
				throw new IllegalStateException("Back not complete");
			}
			int[] ranks = new int[5];
			int[] suits = new int[5];
			convertForEval(back, ranks, suits);
			backValue = StupidEval.eval(ranks, suits);
		}
		return backValue;
	}

	@Override
	public int getStreet() {
		return getFrontSize() + getMiddleSize() + getBackSize() + 1;
	}

	private String maskToString(long mask) {
		if (mask == 0) {
			return "[]\n";
		}
		return Deck.cardMaskString(mask) + "\n";
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(maskToString(front));
		sb.append(maskToString(middle));
		sb.append(maskToString(back));
		return sb.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (back ^ (back >>> 32));
		result = prime * result + (int) (front ^ (front >>> 32));
		result = prime * result + (int) (middle ^ (middle >>> 32));
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
		LongOfcHand other = (LongOfcHand) obj;
		if (back != other.back)
			return false;
		if (front != other.front)
			return false;
		if (middle != other.middle)
			return false;
		return true;
	}

}
