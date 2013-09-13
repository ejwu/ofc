import org.pokersource.game.Deck;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

public class LongCompleteOfcHand extends LongOfcHand
	implements CompleteOfcHand {

	public LongCompleteOfcHand(LongOfcHand source, OfcCard card) {
		super(source);
		if (getBackSize() < BACK_SIZE) {
			addBack(card);
		} else if (getMiddleSize() < MIDDLE_SIZE) {
			addMiddle(card);
		} else if (getFrontSize() < FRONT_SIZE) {
			addFront(card);
		} 
		if (!super.isComplete()) {
			throw new IllegalStateException("Hand too empty.");
		}
	}

	public LongCompleteOfcHand(LongOfcHand source) {
		super(source);
		if (!source.isComplete()) {
			throw new IllegalStateException("Hand too empty.");
		}
	}
	
	// convenient factory for tests
	static CompleteOfcHand createComplete(String handString) {
		return new LongCompleteOfcHand(LongOfcHand.create(handString));
	}
	
	@Override
	public boolean isComplete() {
		throw new RuntimeException("Why are you asking?!");
		//return true;
	}

	@Override
	public boolean isFouled() {
		// TODO: Be damn sure about this.  Making an assumption that when the hand is complete, willBeFouled is always populated
		// via the completeXXX methods
		return willBeFouled;
	}

	@Override
	public int getStreet() {
		return 14;
	}

}
