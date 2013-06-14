import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;


public class Scorers {
	private static final Map<Long, Integer> STANDARD_BACK_ROYALTY_MAP = ImmutableMap.<Long, Integer>builder()
			.put(StupidEval.STRAIGHT, 2)
			.put(StupidEval.FLUSH, 4)
			.put(StupidEval.FULL_HOUSE, 6)
			.put(StupidEval.QUADS, 8)
			.put(StupidEval.STRAIGHT_FLUSH, 10)
			.put(StupidEval.ROYAL_FLUSH, 20)
			.build();
	
	private static final Map<Long, Integer> NEW_BACK_ROYALTY_MAP = ImmutableMap.<Long, Integer>builder()
			.put(StupidEval.STRAIGHT, 2)
			.put(StupidEval.FLUSH, 4)
			.put(StupidEval.FULL_HOUSE, 6)
			.put(StupidEval.QUADS, 10)
			.put(StupidEval.STRAIGHT_FLUSH, 15)
			.put(StupidEval.ROYAL_FLUSH, 25)
			.build();
	
	private static final int SCOOPING_VALUE = 6;
	private static final int SCOOPED_VALUE = -6;
	private static final int FANTASYLAND_VALUE = 12;
	
	public interface Scorer {
		/**
		 * Return the value of p1's hand scored against p2
		 */
		int score(OfcHand first, OfcHand second);
		
		String getCacheFile();
		String getKey();
	}

	private static abstract class AbstractScorer implements Scorer {
		protected abstract Map<Long, Integer> getBackRoyaltyMap();
		protected abstract int getFantasylandValue(OfcHand hand);

		// NOTE: actually never mind, these must be the same.  Don't mess with this
		public String getKey() {
			return getCacheFile();
		}
		/*	
		protected final File cacheFile = initCacheFile();
		protected final Cache<GameState, Double> cache = initCache();

		protected final File initCacheFile() {
			File file = new File(getCacheFile());
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			return file;
		}
		
		protected final Cache<GameState, Double> initCache() {
			Cache<GameState, Double> cache = CacheBuilder.newBuilder().concurrencyLevel(1)
					.maximumSize(1000000L)
					.recordStats()
					.build(new CacheLoader<GameState, Double>() {
						public Double load(GameState state) {
							return state.calculateValue(this);
						}});
			
			long timestamp = System.currentTimeMillis();
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(getCacheFile()));
				String s = reader.readLine();
				while (s != null) {
					if (!s.trim().isEmpty()) {
						cache.put(GameState.fromFileString(s), GameState.getValueFromFileString(s));
					}
					s = reader.readLine();
				}
				reader.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				try {
					reader.close();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			System.out.println("load from " + getCacheFile() + " took " + (System.currentTimeMillis() - timestamp));
			return cache;
		}
	*/
		protected final int getRoyaltyValue(OfcHand hand) {
			if (!hand.isComplete()) {
				throw new IllegalArgumentException("Hand not complete");
			}
			if (hand.isFouled()) {
				return 0;
			}
			
			int value = 0;
			Map<Long, Integer> backRoyaltyMap = getBackRoyaltyMap();
			
			// Stupid integer division hack to zero out all the insignificant digits so we can use a map to look up
			// royalty values
			long rank = hand.getBackRank() / StupidEval.ONE_PAIR * StupidEval.ONE_PAIR;
			if (backRoyaltyMap.containsKey(rank)) {
				value += backRoyaltyMap.get(rank);
			}
			rank = hand.getMiddleRank() / StupidEval.ONE_PAIR * StupidEval.ONE_PAIR;
			if (backRoyaltyMap.containsKey(rank)) {
				value += backRoyaltyMap.get(rank) * 2;
			}
			
			rank = hand.getFrontRank();
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
		
		protected final boolean isFantasyland(OfcHand hand) {
			return hand.getFrontRank() > StupidEval.FANTASYLAND_THRESHOLD;
		}
		
		public final int score(OfcHand first, OfcHand second) {
			if (!first.isComplete() || !second.isComplete()) {
				throw new IllegalArgumentException("Can only compare complete hands");
			}
			
			if (first.isFouled()) {
				if (second.isFouled()) {
					return 0;
				}
				return SCOOPED_VALUE - getRoyaltyValue(second);
			}
			if (second.isFouled()) {
				return SCOOPING_VALUE + getRoyaltyValue(first);
			}
			int wins = 0;
			if (first.getBackRank() > second.getBackRank()) {
				wins++;
			}
			if (first.getMiddleRank() > second.getMiddleRank()) {
				wins++;
			}
			if (first.getFrontRank() > second.getFrontRank()) {
				wins++;
			}
			
			int score = getRoyaltyValue(first) - getRoyaltyValue(second);
			int flValue = getFantasylandValue(first) - getFantasylandValue(second);
			switch (wins) {
				case 0:
					score += SCOOPED_VALUE;
					break;
				case 1:
					score -= 1;
					break;
				case 2:
					score += 1;
					break;
				case 3:
					score += SCOOPING_VALUE;
					break;
				default:
					throw new IllegalStateException("wtf");
			}
			return score + flValue;

		}
	}
	
	private static class StandardScorer extends AbstractScorer {
		protected Map<Long, Integer> getBackRoyaltyMap() {
			return STANDARD_BACK_ROYALTY_MAP;
		}
		
		protected int getFantasylandValue(OfcHand hand) {
			return 0;
		}
		
		public String getCacheFile() {
			return "standard.txt";
		}
	}
	
	private static final class StandardFantasylandScorer extends StandardScorer {
		@Override
		protected int getFantasylandValue(OfcHand hand) {
			if (isFantasyland(hand)) {
				return FANTASYLAND_VALUE;
			}
			return 0;
		}
		
		@Override
		public String getCacheFile() {
			return "standard-fantasyland.txt";
		}
	}
	
	private static class NewScorer extends AbstractScorer {
		protected Map<Long, Integer> getBackRoyaltyMap() {
			return NEW_BACK_ROYALTY_MAP;
		}
		
		protected int getFantasylandValue(OfcHand hand) {
			return 0;
		}
		
		public String getCacheFile() {
			return "new.txt";
		}
	}
	
	private static final class NewFantasylandScorer extends StandardScorer {
		@Override
		protected int getFantasylandValue(OfcHand hand) {
			if (isFantasyland(hand)) {
				return FANTASYLAND_VALUE;
			}
			return 0;
		}
		
		@Override
		public String getCacheFile() {
			return "new-fantasyland.txt";
		}
	}
	
	private static final List<Scorer> SCORERS = ImmutableList.<Scorer>of(new StandardScorer(), new StandardFantasylandScorer(),
			new NewScorer(), new NewFantasylandScorer());
	
	private static final List<Scorer> STANDARD_SCORERS = ImmutableList.<Scorer>of(new StandardScorer(), new StandardFantasylandScorer());
	
	public static List<Scorer> getScorers() {
		return STANDARD_SCORERS;
	}
}
