import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;


public class StupidOfcHand extends CachedValueOfcHand implements OfcHand {

	private List<OfcCard> back = Lists.newArrayListWithExpectedSize(BACK_SIZE);
	private List<OfcCard> middle = Lists.newArrayListWithExpectedSize(MIDDLE_SIZE);
	private List<OfcCard> front = Lists.newArrayListWithExpectedSize(FRONT_SIZE);
		
	public StupidOfcHand() {
	}

	// copy constructor
	private StupidOfcHand(StupidOfcHand source) {
		super(source);
		for (OfcCard card : source.back) {
			this.addBack(card);
		}
		for (OfcCard card : source.middle) {
			this.addMiddle(card);
		}
		for (OfcCard card : source.front) {
			this.addFront(card);
		}
	}
	
	@Override
	public StupidOfcHand copy() {
		return new StupidOfcHand(this);
	}
		
	/* (non-Javadoc)
	 * @see OfcHand#addBack(OfcCard)
	 */
	@Override
	public void addBack(OfcCard card) {
		if (back.size() < BACK_SIZE) {
			back.add(card);
			// Once a hand is complete, calculate its rank immediately to cache it and pass it on to
			// future branches of this hand
			if (back.size() == BACK_SIZE) {
				completeBack();
			}
		} else {
			throw new IllegalStateException("Back is full");
		}
	}
	
	/* (non-Javadoc)
	 * @see OfcHand#addMiddle(OfcCard)
	 */
	@Override
	public void addMiddle(OfcCard card) {
		if (middle.size() < MIDDLE_SIZE) {
			middle.add(card);
			// Once a hand is complete, calculate its rank immediately to cache it and pass it on to
			// future branches of this hand
			if (middle.size() == MIDDLE_SIZE) {
				completeMiddle();
			}
		} else {
			throw new IllegalStateException("Middle is full");
		}
	}
	
	/* (non-Javadoc)
	 * @see OfcHand#addFront(OfcCard)
	 */
	@Override
	public void addFront(OfcCard card) {
		if (front.size() < FRONT_SIZE) {
			front.add(card);
			// Once a hand is complete, calculate its rank immediately to cache it and pass it on to
			// future branches of this hand
			if (front.size() == FRONT_SIZE) {
				completeFront();
			}
		} else {
			throw new IllegalStateException("Front is full");
		}
	}
	
	public int getBackSize() {
		return back.size();
	}
	
	public int getMiddleSize() {
		return middle.size();
	}
	
	public int getFrontSize() {
		return front.size();
	}
	
	/* (non-Javadoc)
	 * @see OfcHand#isComplete()
	 */
	@Override
	public boolean isComplete() {
		return getBackSize() == BACK_SIZE &&
				getMiddleSize() == MIDDLE_SIZE &&
				getFrontSize() == FRONT_SIZE;
	}
	
	// use for pruning the search space
	/* (non-Javadoc)
	 * @see OfcHand#willBeFouled()
	 */
	@Override
	public boolean willBeFouled() {
		return willBeFouled;
		// TODO: add more clever things here for when a hand isn't complete but will definitely foul
	}
	
	/* (non-Javadoc)
	 * @see OfcHand#isFouled()
	 */
	@Override
	public boolean isFouled() {
		if (!isComplete()) {
			throw new IllegalStateException("Can't evaluate hand before complete");
		}
		
		return getFrontRank() > getMiddleRank() || getMiddleRank() > getBackRank();
	}

	
	/* (non-Javadoc)
	 * @see OfcHand#getFrontRank()
	 */
	@Override
	public long getFrontRank() {
		if (frontValue == UNSET) {
			// TODO: maybe make these size 3 instead and don't check length in StupidEval
			int[] ranks = new int[5];
			int[] suits = new int[5];
			convertForEval(front, ranks, suits);
			frontValue = StupidEval.eval3(ranks);
		}
		return frontValue;
	}
	
	/* (non-Javadoc)
	 * @see OfcHand#getMiddleRank()
	 */
	@Override
	public long getMiddleRank() {
		if (middleValue == UNSET) {
			int[] ranks = new int[5];
			int[] suits = new int[5];
			convertForEval(middle, ranks, suits);
			middleValue = StupidEval.eval(ranks, suits);
		}
		return middleValue;
	}

	/* (non-Javadoc)
	 * @see OfcHand#getBackRank()
	 */
	@Override
	public long getBackRank() {
		if (backValue == UNSET) {
			int[] ranks = new int[5];
			int[] suits = new int[5];
			convertForEval(back, ranks, suits);
			backValue = StupidEval.eval(ranks, suits);
		}
		return backValue;
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
	
	/* (non-Javadoc)
	 * @see OfcHand#getStreet()
	 */
	@Override
	public int getStreet() {
		return getFrontSize() + getMiddleSize() + getBackSize() + 1;
	}
	
	/* (non-Javadoc)
	 * @see OfcHand#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((back == null) ? 0 : back.hashCode());
		result = prime * result + ((front == null) ? 0 : front.hashCode());
		result = prime * result + ((middle == null) ? 0 : middle.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see OfcHand#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StupidOfcHand other = (StupidOfcHand) obj;
		if (back == null) {
			if (other.back != null)
				return false;
		} else if (!back.equals(other.back))
			return false;
		if (front == null) {
			if (other.front != null)
				return false;
		} else if (!front.equals(other.front))
			return false;
		if (middle == null) {
			if (other.middle != null)
				return false;
		} else if (!middle.equals(other.middle))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see OfcHand#toString()
	 */
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
