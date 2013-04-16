import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;


public class OfcHand {

	public static final int BACK_SIZE = 5;
	public static final int MIDDLE_SIZE = 5;
	public static final int FRONT_SIZE = 3;
	
	private List<OfcCard> back = Lists.newArrayListWithExpectedSize(BACK_SIZE);
	private List<OfcCard> middle = Lists.newArrayListWithExpectedSize(MIDDLE_SIZE);
	private List<OfcCard> front = Lists.newArrayListWithExpectedSize(FRONT_SIZE);
	
	private static final Map<Long, Integer> backRoyaltyMap = ImmutableMap.<Long, Integer>builder()
		.put(StupidEval.STRAIGHT, 2)
		.put(StupidEval.FLUSH, 4)
		.put(StupidEval.FULL_HOUSE, 6)
		.put(StupidEval.QUADS, 8)
		.put(StupidEval.STRAIGHT_FLUSH, 10)
		.put(StupidEval.ROYAL_FLUSH, 25)
		.build();
		
	public OfcHand() {
	}

	public void addBack(OfcCard card) {
		if (back.size() < BACK_SIZE) {
			back.add(card);
		} else {
			throw new IllegalStateException("Back is full");
		}
	}
	
	public void addMiddle(OfcCard card) {
		if (middle.size() < MIDDLE_SIZE) {
			middle.add(card);
		} else {
			throw new IllegalStateException("Middle is full");
		}
	}
	
	public void addFront(OfcCard card) {
		if (front.size() < FRONT_SIZE) {
			front.add(card);
		} else {
			throw new IllegalStateException("Front is full");
		}
	}
	
	public boolean isComplete() {
		return back.size() == BACK_SIZE &&
				middle.size() == MIDDLE_SIZE &&
				front.size() == FRONT_SIZE;
	}
	
	// use for pruning the search space
	public boolean willBeFouled() {
		return false;
		// TODO: add clever things here for when a hand isn't complete but will definitely foul
	}
	
	public boolean isFouled() {
		if (!isComplete()) {
			throw new IllegalStateException("Can't evaluate hand before complete");
		}
		
		return getFrontRank() > getMiddleRank() || getMiddleRank() > getBackRank();
	}

	public int getRoyaltyValue() {
		if (!isComplete()) {
			throw new IllegalArgumentException("Hand not complete");
		}
		if (isFouled()) {
			return 0;
		}
		int value = 0;
		
		// Stupid integer division hack to zero out all the insignificant digits so we can use a map to look up
		// royalty values
		long rank = getBackRank() / StupidEval.ONE_PAIR * StupidEval.ONE_PAIR;
		if (backRoyaltyMap.containsKey(rank)) {
			value += backRoyaltyMap.get(rank);
		}
		rank = getMiddleRank() / StupidEval.ONE_PAIR * StupidEval.ONE_PAIR;
		if (backRoyaltyMap.containsKey(rank)) {
			value += backRoyaltyMap.get(rank) * 2;
		}
		
		rank = getFrontRank();
		if (rank >= StupidEval.TRIPS) {
			rank -= StupidEval.TRIPS;
			// StupidEval implementation is to leave only the rank of the card here.  Deuce = 0, per Deck constants
			// Yes, this is super lame. 15 points for 222, one more for every higher rank.
			value += 15 + rank;
		} else if (rank >= StupidEval.ONE_PAIR) {
			// More stupid implementation dependent details.  Subtract out the ONE_PAIR constant, integer divide
			// the kickers away, get left with the rank of the pair based on Deck constants.  66 = 5.
			rank -= StupidEval.ONE_PAIR;
			rank /= StupidEval.PAIR_CONSTANT;
			if (rank >= 5) {
				value += rank - 4;
			}
		}
		
		
		return value;
	}
	
	public int scoreAgainst(OfcHand other) {
		if (!isComplete() || !other.isComplete()) {
			throw new IllegalArgumentException("Can only compare complete hands");
		}
		if (isFouled()) {
			if (other.isFouled()) {
				return 0;
			}
			return -6;
		}
		if (other.isFouled()) {
			return 6;
		}
		int wins = 0;
		if (getBackRank() > other.getBackRank()) {
			wins++;
		}
		if (getMiddleRank() > other.getMiddleRank()) {
			wins++;
		}
		if (getFrontRank() > other.getFrontRank()) {
			wins++;
		}
		
		switch (wins) {
			case 0:
				return -6;
			case 1:
				return -1;
			case 2:
				return 1;
			case 3:
				return 6;
			default:
				throw new IllegalStateException("wtf");
		}
	}
	
	public long getFrontRank() {
		// TODO: see other TODOS
		// TODO: maybe makes these size 3 instead and don't check length in StupidEval
		int[] ranks = new int[5];
		int[] suits = new int[5];
		convertForEval(front, ranks, suits);
		return StupidEval.eval3(ranks);
	}
	
	public long getMiddleRank() {
		// TODO: reuse the array
		// TODO: cache this value?
		int[] ranks = new int[5];
		int[] suits = new int[5];
		convertForEval(middle, ranks, suits);
		return StupidEval.eval(ranks, suits);
	}

	public long getBackRank() {
		// TODO: reuse the array
		// TODO: cache this value?
		int[] ranks = new int[5];
		int[] suits = new int[5];
		convertForEval(back, ranks, suits);
		return StupidEval.eval(ranks, suits);
	}
	
	/**
	 * Fill the ranks and suits arrays with the value of the hand.
	 */
	@VisibleForTesting
	static void convertForEval(List<OfcCard> hand, int[] ranks, int[] suits) {
		Collections.<OfcCard>sort(hand, new Comparator<OfcCard>() {
			public int compare(OfcCard first, OfcCard second) {
				return second.getRank() - first.getRank();
			}
			
			public boolean equals(Object o) {
				return false;
			}
		});
				
		// TODO: be smarter about which hand we're checking
		if (hand.size() != FRONT_SIZE && hand.size() != BACK_SIZE) {
			throw new IllegalArgumentException("Hand isn't complete");
		}
		
		int index = 0;
		for (OfcCard card : hand) {
			ranks[index] = card.getRank();
			suits[index] = card.getSuit();
			index++;
		}
	}
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(front.toString());
		sb.append("\n");
		sb.append(middle.toString());
		sb.append("\n");
		sb.append(back.toString());
		sb.append("\n");
		return sb.toString();
	}
}
