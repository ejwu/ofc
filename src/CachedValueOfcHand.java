import java.util.Set;

import com.google.common.collect.Sets;

public abstract class CachedValueOfcHand implements OfcHand {

	protected static final long UNSET = Long.MIN_VALUE;
	protected long backValue = UNSET;
	protected long middleValue = UNSET;
	protected long frontValue = UNSET;

	protected boolean willBeFouled = false;

	protected CachedValueOfcHand() {
		backValue = UNSET;
		middleValue = UNSET;
		frontValue = UNSET;
		willBeFouled = false;
	}
	
	// copy constructor
	protected CachedValueOfcHand(CachedValueOfcHand source) {
		this.backValue = source.backValue;
		this.middleValue = source.middleValue;
		this.frontValue = source.frontValue;
		this.willBeFouled = source.willBeFouled;
	}
	
	protected void completeBack() {
		backValue = getBackRank();
		if (middleValue != UNSET && backValue < middleValue) {
			willBeFouled = true;
		}
		if (frontValue != UNSET && backValue < frontValue) {
			willBeFouled = true;
		}
	}
		
	protected void completeMiddle() {
		middleValue = getMiddleRank();
		if (frontValue != UNSET && middleValue < frontValue) {
			willBeFouled = true;
		}
		if (backValue != UNSET && backValue < middleValue) {
			willBeFouled = true;
		}
	}
	
	protected void completeFront() {
		frontValue = getFrontRank();
		if (middleValue != UNSET && middleValue < frontValue) {
			willBeFouled = true;
		}
		if (backValue != UNSET && backValue < frontValue) {
			willBeFouled = true;
		}
	}
	
	/* 
	 * TODO: this really belongs in AbstractOfcHand
	 * @see OfcHand#getRoyaltyValue()
	 */
	@Override
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
	
	/*
	 * TODO: this also belongs in AbstractOfcHand
	 * @see OfcHand#scoreAgainst(OfcHand)
	 */
	@Override
	public int scoreAgainst(OfcHand other) {
		if (!isComplete() || !other.isComplete()) {
			throw new IllegalArgumentException("Can only compare complete hands");
		}
		if (isFouled()) {
			if (other.isFouled()) {
				return 0;
			}
			return -6 - other.getRoyaltyValue();
		}
		if (other.isFouled()) {
			return 6 + getRoyaltyValue();
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
				return -6 + getRoyaltyValue() - other.getRoyaltyValue();
			case 1:
				return -1 + getRoyaltyValue() - other.getRoyaltyValue();
			case 2:
				return 1 + getRoyaltyValue() - other.getRoyaltyValue();
			case 3:
				return 6 + getRoyaltyValue() - other.getRoyaltyValue();
			default:
				throw new IllegalStateException("wtf");
		}
	}
	
	/*
	 * TODO: again, AbstractOfcHand
	 * @see OfcHand#generateHands(OfcCard)
	 */
	@Override
	public Set<OfcHand> generateHands(OfcCard card) {
		Set<OfcHand> hands = Sets.newHashSetWithExpectedSize(3);
		if (getBackSize() < BACK_SIZE) {
			OfcHand copy = this.copy();
			copy.addBack(card);
			hands.add(copy);
			if (willBeFouled()) {
				// exit early if foul is guaranteed
				return hands;
			}
		}
		if (getMiddleSize() < MIDDLE_SIZE) {
			OfcHand copy = this.copy();
			copy.addMiddle(card);
			hands.add(copy);
			if (willBeFouled()) {
				return hands;
			}
		}
		if (getFrontSize() < FRONT_SIZE) {
			OfcHand copy = this.copy();
			copy.addFront(card);
			hands.add(copy);
		}
		return hands;
	}

}