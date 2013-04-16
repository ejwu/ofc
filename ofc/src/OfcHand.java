import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;


public class OfcHand {

	public static final int BACK_SIZE = 5;
	public static final int MIDDLE_SIZE = 5;
	public static final int FRONT_SIZE = 3;
	
	private List<OfcCard> back = Lists.newArrayListWithExpectedSize(BACK_SIZE);
	private List<OfcCard> middle = Lists.newArrayListWithExpectedSize(MIDDLE_SIZE);
	private List<OfcCard> front = Lists.newArrayListWithExpectedSize(FRONT_SIZE);
	
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
		
		return false;
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
