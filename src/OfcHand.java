import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

public interface OfcHand {

	public static final int BACK_SIZE = 5;
	public static final int MIDDLE_SIZE = 5;
	public static final int FRONT_SIZE = 3;

	static final Map<Long, Integer> backRoyaltyMap = ImmutableMap.<Long, Integer>builder()
			.put(StupidEval.STRAIGHT, 2)
			.put(StupidEval.FLUSH, 4)
			.put(StupidEval.FULL_HOUSE, 6)
			.put(StupidEval.QUADS, 8)
			.put(StupidEval.STRAIGHT_FLUSH, 10)
			.put(StupidEval.ROYAL_FLUSH, 25)
			.build();	
	
	OfcHand copy();

	Set<OfcHand> generateHands(OfcCard card);

	void addBack(OfcCard card);
	void addMiddle(OfcCard card);
	void addFront(OfcCard card);

	int getBackSize();
	int getMiddleSize();
	int getFrontSize();
	
	long getFrontRank();
	long getMiddleRank();
	long getBackRank();

	// These really are just for LongOfcHand and don't belong in the interface
	long getBackMask();
	long getMiddleMask();
	long getFrontMask();
	
	boolean isComplete();

	// use for pruning the search space
	boolean willBeFouled();

	boolean isFouled();

	int getRoyaltyValue();

	int scoreAgainst(OfcHand other);

	int getStreet();
	
	/**
	 * Convert this hand to a less human readable but more compact string for storage purposes.
	 */
	String toKeyString();
}