import java.util.Set;

import org.pokersource.game.Deck;

public class LongOfcHand extends CachedValueOfcHand {

	private long front;
	private long middle;
	private long back;
	
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
	public Set<OfcHand> generateHands(OfcCard card) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addBack(OfcCard card) {
		if (Long.bitCount(back) >= BACK_SIZE) {
			throw new IllegalStateException("Back already full");
		}
		if ((back & card.getMask()) != 0) {
			throw new IllegalArgumentException("Card already in back");
		}
		back |= card.getMask();
	}

	@Override
	public void addMiddle(OfcCard card) {
		if (Long.bitCount(middle) >= MIDDLE_SIZE) {
			throw new IllegalStateException("Middle already full");
		}
		if ((middle & card.getMask()) != 0) {
			throw new IllegalArgumentException("Card already in middle");
		}
		middle |= card.getMask();
	}

	@Override
	public void addFront(OfcCard card) {
		if (Long.bitCount(front) >= FRONT_SIZE) {
			throw new IllegalStateException("Front already full");
		}
		if ((front & card.getMask()) != 0) {
			throw new IllegalArgumentException("Card already in front");
		}
		front |= card.getMask();
	}

	@Override
	public boolean isComplete() {
		return Long.bitCount(front) == FRONT_SIZE &&
				Long.bitCount(middle) == MIDDLE_SIZE &&
				Long.bitCount(back) == BACK_SIZE;
	}

	@Override
	public boolean willBeFouled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFouled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getRoyaltyValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int scoreAgainst(OfcHand other) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getFrontRank() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getMiddleRank() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getBackRank() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getStreet() {
		return Long.bitCount(front) + Long.bitCount(middle) + Long.bitCount(back) + 1;
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
