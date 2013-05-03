import java.util.Set;

public interface OfcHand {

	public static final int BACK_SIZE = 5;
	public static final int MIDDLE_SIZE = 5;
	public static final int FRONT_SIZE = 3;
	
	OfcHand copy();

	Set<OfcHand> generateHands(OfcCard card);

	void addBack(OfcCard card);

	void addMiddle(OfcCard card);

	void addFront(OfcCard card);

	boolean isComplete();

	// use for pruning the search space
	boolean willBeFouled();

	boolean isFouled();

	int getRoyaltyValue();

	int scoreAgainst(OfcHand other);

	long getFrontRank();

	long getMiddleRank();

	long getBackRank();

	int getStreet();
}