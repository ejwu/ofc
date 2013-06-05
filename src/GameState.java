import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Table;
import com.sun.xml.internal.bind.v2.TODO;

public class GameState {
	/*
	private static final ExecutorService executor = Executors.newCachedThreadPool();
	
	private static final File CACHE_FILE;
	private static final File FL_CACHE_FILE;
	
	private static final LoadingCache<GameState, Double> cache =
		CacheBuilder.newBuilder().concurrencyLevel(1)
			.maximumSize(2000000L)
			.recordStats()
			.build(new CacheLoader<GameState, Double>() {
				public Double load(GameState state) {
					return state.calculateValue();
				}
			});
			
	private static final LoadingCache<GameState, Double> flcache =
			CacheBuilder.newBuilder().concurrencyLevel(1)
				.maximumSize(2000000L)
				.recordStats()
				.build(new CacheLoader<GameState, Double>() {
					public Double load(GameState state) {
						return state.calculateValue();
					}
				});
*/
	// bit mask for the full deck, used for checking that a state is valid
	private static final long FULL_DECK_MASK;
	
	static {
		long fullDeck = 0L;
		for (int i = 0; i < 52; i++) {
			fullDeck <<= 1;
			fullDeck |= 1L;
		}
		FULL_DECK_MASK = fullDeck;
	/*	
		CACHE_FILE = new File("cache.txt");
		if (!CACHE_FILE.exists()) {
			try {
				CACHE_FILE.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		FL_CACHE_FILE = new File("fl_cache.txt");
		if (!FL_CACHE_FILE.exists()) {
			try {
				FL_CACHE_FILE.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		long timestamp = System.currentTimeMillis();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader("cache.txt"));
			String s = reader.readLine();
			while (s != null) {
				if (!s.trim().isEmpty()) {
					cache.put(fromFileString(s), getValueFromFileString(s));
				}
				s = reader.readLine();
			}
			reader.close();
			
			reader = new BufferedReader(new FileReader("fl_cache.txt"));
			s = reader.readLine();
			while (s != null) {
				if (!s.trim().isEmpty()) {
					flcache.put(fromFileString(s), getValueFromFileString(s));
				}
				s = reader.readLine();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
//			System.out.println(cache.stats());
			try {
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("load from file took " + (System.currentTimeMillis() - timestamp));
*/
	}
	
	// instrumentation
	private static final AtomicLong statesSolved = new AtomicLong(0L);
	private static final AtomicLong counter = new AtomicLong(0L);
	private static final long startTime = System.currentTimeMillis();
	private static long timestamp = 0L;
	private static final Multiset<Double> statesSolvedByStreet = ConcurrentHashMultiset.create();
		
	// The current player
	OfcHand player1;
	OfcHand player2;
	OfcDeck deck;
	
	public GameState(OfcHand player1, OfcHand player2, OfcDeck deck) {
		this.player1 = player1;
		this.player2 = player2;
		this.deck = deck;
		if (player1.getStreet() != player2.getStreet() &&
			player1.getStreet() != player2.getStreet() - 1) {
			throw new IllegalStateException("Player 1 must be on same or previous street as player 2");
		}
		
		// Don't permit any dead cards.  We'd like game states to be normalized so that any 2 of {player1, player2, deck}
		// define the state.
		if (FULL_DECK_MASK != (player1.getBackMask() ^ player1.getMiddleMask() ^ player1.getFrontMask() ^
				player2.getBackMask() ^ player2.getMiddleMask() ^ player2.getFrontMask() ^ deck.getMask())) {
			throw new IllegalStateException("Hands + deck != full deck");
		}
	}
	
	private String getInstrumentation() {
		StringBuilder sb = new StringBuilder();
		sb.append(statesSolved.longValue() + "\n");
		long newTime = System.currentTimeMillis();
		sb.append("total time: " + (newTime - startTime) / 1000 + " seconds \n");
		sb.append("since last time: " + (newTime - timestamp) + " ms\n");
		timestamp = newTime;
		Double[] streets = statesSolvedByStreet.elementSet().toArray(new Double[0]);
		Arrays.sort(streets);
		for (Double street : streets) {
			sb.append(street + ": " + statesSolvedByStreet.count(street) + "\n");
		}
//		sb.append(cache.stats() + "\n");
//		sb.append(flcache.stats() + "\n");
		return sb.toString();
	}
	
	
	
	
	/**
	 * Generate the value of each best state reachable from this one, i.e., optimal setting for each card. // lies
	 * If a state is the best setting for any scorer, it will be returned.
	 */
	private Table<String, GameState, Double> generateBestStateValues(List<Scorers.Scorer> scorers) {
		if (player1.isComplete()) {
			throw new IllegalStateException("Shouldn't generate new states for complete hand");
		}

		Table<String, GameState, Double> bestScores = HashBasedTable.create();
		
		for (OfcCard card : CardSetUtils.asCards(deck.getMask())) {

			Table<String, GameState, Double> allScoresForHand = HashBasedTable.create();
			
			for (OfcHand hand : player1.generateHands(card)) {
				GameState candidateState = new GameState(player2, hand, new OfcDeck(deck.withoutCard(card)));
				Map<String, Double> scoresForHand = candidateState.getValue(scorers);
				for (Scorers.Scorer scorer : scorers) {
					// Score is from opponent's perspective, so we flip it
					allScoresForHand.put(scorer.getKey(), candidateState, scoresForHand.get(scorer.getKey()) * -1);
				}
				// it would be nice to filter out hopeless settings here
			}

			// Now we have the score for each setting of this card, filter out the unusable ones
			for (Scorers.Scorer scorer : scorers) {
				// TODO: Double.MIN_VALUE doesn't compare properly.
				GameState bestSet = null;
				double bestValue = -1000000000000.0;
				Map<GameState, Double> scoresForScorer = allScoresForHand.row(scorer.getKey());
				for (Map.Entry<GameState, Double> entry : scoresForScorer.entrySet()) {
					if (entry.getValue() > bestValue) {
						bestSet = entry.getKey();
						bestValue = entry.getValue();
					}
				}
				bestScores.put(scorer.getKey(), bestSet, bestValue);
			}
			
		}

		statesSolved.addAndGet(bestScores.columnKeySet().size());
		counter.addAndGet(bestScores.columnKeySet().size());
		statesSolvedByStreet.add(bestScores.columnKeySet().iterator().next().getStreet(), bestScores.columnKeySet().size());
		if (counter.longValue() > 10000) {
			counter.set(0L);
			System.out.println(getInstrumentation());
		}

		// sanity check
		if (bestScores.columnKeySet().size() > (52 * 3 * scorers.size())) {
			throw new IllegalStateException("shouldn't happen");
		}

		return bestScores;
	}
	
	public boolean isComplete() {
		return player1.isComplete() && player2.isComplete();
	}
	
	/**
	 * Get the value of this state for the first player.  If the game is complete, this is the score.
	 * Otherwise, it is the average of all states generated by dealing the opponent a card.
	 */
	public Map<String, Double> getValue(List<Scorers.Scorer> scorers) {
		if (getStreet() > 13.0) {
			throw new IllegalStateException("never happen");
		}
/*
		Double value = cache.getUnchecked(this);
		Double flvalue = flcache.getUnchecked(this);
		if (value != null && flvalue != null) {
			return new Score(value, flvalue);
		}
*/
		return calculateValue(scorers);
	}
	
	/**
	 * Calculate the value when it's not available in the cache
	 */
	private Map<String, Double> calculateValue(List<Scorers.Scorer> scorers) {
		// 13th street, no more choices		
		if (getStreet() == 13.0) {
			return calculate13thStreet(scorers);
		}

		Map<String, Double> values = getValueSequential(scorers);
			
		if (getStreet() < 12.0) {
			System.out.println("\n\nSolved");
			System.out.println(this);
			System.out.println(values);
			System.out.println(getInstrumentation());
			/*
			try {
				FileWriter fw = new FileWriter(CACHE_FILE.getAbsoluteFile(), true);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(toFileString() + " " + value + "\n");
				bw.flush();
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
				// can't do much, oh well
			}
			*/
		}

		// TODO: be smarter about cache policy
//		cache.put(this, value);
		return values;
	}

	private Map<String, Double> calculate13thStreet(List<Scorers.Scorer> scorers) {
		int count = 0;
		int[] sums = new int[scorers.size()];
		
		for (OfcCard p1Card : CardSetUtils.asCards(deck.getMask())) {
			Set<OfcHand> p1Hands = player1.generateHands(p1Card);
			if (p1Hands.size() != 1) {
				throw new IllegalStateException("should only generate one hand");
			}
			// TODO: just a wonky way to get a single hand, maybe mess with generateHands 
			OfcHand p1Hand = p1Hands.iterator().next();
			for (OfcCard p2Card : CardSetUtils.asCards(deck.withoutCard(p1Card))) {
				Set<OfcHand> p2Hands = player2.generateHands(p2Card);
				if (p2Hands.size() != 1) {
					throw new IllegalStateException("should only generate one hand");
				}
				// TODO: just a wonky way to get a single hand, maybe mess with generateHands 
				OfcHand p2Hand = p2Hands.iterator().next();
				count++;

				int index = 0;
				for (Scorers.Scorer scorer : scorers) {
					sums[index] += scorer.score(p1Hand, p2Hand);
					index++;
				}
			}
		}
		
		int index = 0;
		Map<String, Double> scores = Maps.newHashMap();
		for (Scorers.Scorer scorer : scorers) {
			scores.put(scorer.getCacheFile(), (double) sums[index] / count);
			index++;
		}

		return scores;
	}

	public Map<String, Double> getValueSequential(List<Scorers.Scorer> scorers) {

		Table<String, GameState, Double> bestStates = generateBestStateValues(scorers);
		
		Map<String, Double> values = Maps.newHashMap();

		for (String key : bestStates.rowKeySet()) {
			int count = 0;
			double sum = 0.0;
			for (GameState state : bestStates.row(key).keySet()) {
				count++;
				sum += bestStates.get(key, state);
			}
			values.put(key, sum / count);
		}
		
		return values;
	}

	/*
	public double getValueConcurrent() {
		double sum = 0.0;
		int count = 0;
		List<Future<Double>> results = Lists.newArrayList();
		for (final GameState nextState : generateBestStates()) {
			results.add(executor.submit(new Callable<Double>() {
				@Override
				public Double call() throws Exception {
					return nextState.getValue();
				}					
			}));				
		}
		
		for (Future<Double> result : results) {
			try {
				sum += result.get();
				count++;
			} catch (Exception e) {
				throw new RuntimeException("oh, this is bad");
			}
		}

		// Current player's value is the negative of the opponent's value.
		return sum / count * -1;
	}
*/
	private String toFileString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getStreet());
		sb.append(" ");
		sb.append(player1.toKeyString());
		sb.append(" ");
		sb.append(player2.toKeyString());
		
		return sb.toString();
	}
	
	@VisibleForTesting
	static GameState fromFileString(String fileString) {
		String[] splitString = fileString.split(" ");

		OfcHand p1 = LongOfcHand.fromKeyString(splitString[1]);
		OfcHand p2 = LongOfcHand.fromKeyString(splitString[2]);
		
		return new GameState(p1, p2, new OfcDeck(deriveDeck(p1, p2)));
	}
	
	@VisibleForTesting
	static double getValueFromFileString(String fileString) {
		String[] splitString = fileString.split(" ");
		return Double.parseDouble(splitString[3]);
	}
	
	@VisibleForTesting
	static long deriveDeck(OfcHand p1, OfcHand p2) {
		return ~(~FULL_DECK_MASK | p1.getFrontMask() | p1.getMiddleMask() | p1.getBackMask()
					| p2.getFrontMask() | p2.getMiddleMask() | p2.getBackMask());
	}
	
	public double getStreet() {
		return (player1.getStreet() + player2.getStreet()) / 2.0;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((deck == null) ? 0 : deck.hashCode());
		result = prime * result + ((player1 == null) ? 0 : player1.hashCode());
		result = prime * result + ((player2 == null) ? 0 : player2.hashCode());
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
		GameState other = (GameState) obj;
		if (deck == null) {
			if (other.deck != null)
				return false;
		} else if (!deck.equals(other.deck))
			return false;
		if (player1 == null) {
			if (other.player1 != null)
				return false;
		} else if (!player1.equals(other.player1))
			return false;
		if (player2 == null) {
			if (other.player2 != null)
				return false;
		} else if (!player2.equals(other.player2))
			return false;
		return true;
	}

	@Override
	public String toString() {		
		StringBuilder sb = new StringBuilder();
		sb.append("Game state: \n");
		sb.append("Street: " + getStreet() + "\n");
		sb.append("Cards in deck: " + CardSetUtils.asCards(deck.getMask()).size() + "\n");
		sb.append(player1);
		sb.append("\n");
		sb.append(player2);
		sb.append("\n");
		if (isComplete()) {
			sb.append(getValue(Scorers.getScorers()) + " points\n");
		}
		sb.append(deck);
		return sb.toString();
	}
}
