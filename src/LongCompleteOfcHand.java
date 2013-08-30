import org.pokersource.game.Deck;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

import gnu.trove.map.TLongLongMap;
import gnu.trove.map.hash.TLongLongHashMap;

import java.util.HashMap;

public class LongCompleteOfcHand extends LongOfcHand
	implements CompleteOfcHand {

	static TLongLongMap evalCache = new TLongLongHashMap();
	
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
	public long getFrontRank() {
		if (frontValue == UNSET) {
			if (!evalCache.containsKey(front)) {
				int[] ranks = new int[FRONT_SIZE];
				int[] suits = new int[FRONT_SIZE];
				convertForEval(front, ranks, suits, FRONT_SIZE);
				frontValue = StupidEval.eval3(ranks);
				evalCache.put(front, frontValue);
			} else {
				frontValue = evalCache.get(front);
			}
		}
		return frontValue;
	}

	@Override
	public long getMiddleRank() {
		if (middleValue == UNSET) {
			if (!evalCache.containsKey(middle)) {
				int[] ranks = new int[MIDDLE_SIZE];
				int[] suits = new int[MIDDLE_SIZE];
				convertForEval(middle, ranks, suits, MIDDLE_SIZE);
				middleValue = StupidEval.eval(ranks, suits);
				evalCache.put(middle, middleValue);
			} else {
				middleValue = evalCache.get(middle);
			}
		}
		return middleValue;
	}

	@Override
	public long getBackRank() {
		if (backValue == UNSET) {
			if (!evalCache.containsKey(back)) {
				int[] ranks = new int[BACK_SIZE];
				int[] suits = new int[BACK_SIZE];
				convertForEval(back, ranks, suits, BACK_SIZE);
				backValue = StupidEval.eval(ranks, suits);
				evalCache.put(back, backValue);
			} else {
				backValue = evalCache.get(back);
			}
		}
		return backValue;
	}

	@Override
	public int getStreet() {
		return 14;
	}

}
