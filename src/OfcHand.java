import java.util.Set;

public interface OfcHand {

	public static final int BACK_SIZE = 5;
	public static final int MIDDLE_SIZE = 5;
	public static final int FRONT_SIZE = 3;
	
	public abstract OfcHand copy();

	public abstract Set<OfcHand> generateHands(OfcCard card);

	public abstract void addBack(OfcCard card);

	public abstract void addMiddle(OfcCard card);

	public abstract void addFront(OfcCard card);

	public abstract boolean isComplete();

	// use for pruning the search space
	public abstract boolean willBeFouled();

	public abstract boolean isFouled();

	public abstract int getRoyaltyValue();

	public abstract int scoreAgainst(OfcHand other);

	public abstract long getFrontRank();

	public abstract long getMiddleRank();

	public abstract long getBackRank();

	public abstract int getStreet();

	public abstract int hashCode();

	public abstract boolean equals(Object obj);

	public abstract String toString();

}