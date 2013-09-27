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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.pokersource.game.Deck;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Table;

public class GameState {
	private static final boolean USE_CONCURRENCY = false;
	private static final ExecutorService executor = Executors.newFixedThreadPool(20);
	
	// bit mask for the full deck, used for checking that a state is valid
	private static final long FULL_DECK_MASK;

	private static Map<String, File> CACHE_FILES;
	
	private static final LoadingCache<GameState, Double> CACHE =
			CacheBuilder.newBuilder().concurrencyLevel(1)
				.maximumSize(1500000L)
				.recordStats()
				.build(new CacheLoader<GameState, Double>() {
					public Double load(GameState state) {
						return state.calculateValue(Scorers.getScorer(), 
													USE_CONCURRENCY);
					}
				});
	
	// instrumentation
	private static final AtomicLong statesSolved;
	private static final AtomicLong counter;
	private static final Multiset<Double> statesSolvedByStreet;
	private static final long startTime = System.currentTimeMillis();
	private static long timestamp = 0L;

	private static long skipped = 0L;
	
	static {
		statesSolved = new AtomicLong(0L);
		counter = new AtomicLong(0L);
		statesSolvedByStreet = ConcurrentHashMultiset.create();
		
		
		FULL_DECK_MASK = createFullDeckMask();
//		CACHE_FILES = createCacheFiles();

		long timestamp = System.currentTimeMillis();

//		BufferedReader reader = null;
/*
		try {
			for (String cacheFileName : CACHE_FILES.keySet()) {
				reader = new BufferedReader(new FileReader(cacheFileName));
				String s = reader.readLine();
				while (s != null) {
					if (!s.trim().isEmpty()) {
						GameState state = fromFileString(s);
						Map<String, Double> cachedScores = CACHE.getIfPresent(state);
						if (cachedScores == null) {
							cachedScores = Maps.newHashMap();
							cachedScores.put(cacheFileName, getValueFromFileString(s));
							CACHE.put(state, cachedScores);
						} else {
							if (!cachedScores.containsKey(cacheFileName)) {
								cachedScores.put(cacheFileName, getValueFromFileString(s));
								CACHE.put(state, cachedScores);
							}
						}
						updateInstrumentation(state.getStreet(), 1);
					}
					s = reader.readLine();
				}
				reader.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println(CACHE.stats());
			try {
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("load from file took " + (System.currentTimeMillis() - timestamp));
*/
	}
	
	private static final long createFullDeckMask() {
		long fullDeck = 0L;
		for (int i = 0; i < 52; i++) {
			fullDeck <<= 1;
			fullDeck |= 1L;
		}
		return fullDeck;
	}
/*	
	private static final Map<String, File> createCacheFiles() {
		Map<String, File> tempCacheMap = Maps.newHashMap();
		for (Scorers.Scorer scorer : Scorers.getScorers()) {
			File file = new File(scorer.getCacheFile());
			System.out.println(file.getAbsolutePath());
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			tempCacheMap.put(scorer.getKey(), file);
		}
		return ImmutableMap.copyOf(tempCacheMap);
	}
	*/
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
		sb.append(CACHE.stats() + "\n");
		sb.append("skipped: " + skipped + "\n");
		return sb.toString();
	}
	
	private static void updateInstrumentation(double street, int numStates) {
		statesSolved.addAndGet(numStates);
		counter.addAndGet(numStates);
		statesSolvedByStreet.add(street, numStates);
	}

	// Do some optimization if there are no flush draws possible
	private boolean hasFlushDraw() {
		return player1.hasFlushDraw() || player2.hasFlushDraw();
	}
	
	private boolean[] liveFlushDraws() {
		boolean[] liveFlushDraws = new boolean[4];
		for (int i = 0; i < Deck.SUIT_COUNT; i++) {
			liveFlushDraws[i] = player1.liveFlushDraws()[i] || player2.liveFlushDraws()[i];
		}
		return liveFlushDraws;
	}
	
	// Really returning state and value, assume the cache saves us
	// TODO: maybe check to see if returning Pair helps?
	private GameState bestSetForCard(OfcCard card, Scorers.Scorer scorer) {
		// really just a List<Pair>
		Map<GameState, Double> allScoresForCard = Maps.newHashMap();
		
		for (OfcHand hand : player1.generateHands(card)) {
			GameState candidateState = new GameState(player2, hand, new OfcDeck(deck.withoutCard(card)));
			// Score is from opponent's perspective, so we flip it
			double scoreForHand = candidateState.getValue(scorer) * -1;
			allScoresForCard.put(candidateState, scoreForHand);
			// it would be nice to filter out hopeless settings here
		}

		// Now we have the score for each setting of this card, filter out the unusable ones

		// TODO: Double.MIN_VALUE doesn't compare properly.
		GameState bestSet = null;
		double bestValue = -1000000000000.0;
		for (Map.Entry<GameState, Double> entry : allScoresForCard.entrySet()) {
			if (entry.getValue() > bestValue) {
				bestSet = entry.getKey();
				bestValue = entry.getValue();
			}
		}

		updateInstrumentation(bestSet.getStreet(), 1);
		return bestSet;
	}
	
	private void updateBestStates(final Multimap<GameState, Double> bestScores, final boolean[] liveFlushDraws,
			final Scorers.Scorer scorer, List<OfcCard> rankList) {
		GameState reusableState = null;
		for (OfcCard card : rankList) {
			if (!liveFlushDraws[card.getSuit()]) {
				// Don't care about flush draws, reuse a precalculated state if available
				if (reusableState != null) {
					skipped++;
					// All these scores are flipped because they show the score for p2's perspective
					bestScores.put(reusableState, reusableState.getValue(scorer) * -1);
				} else {
					reusableState = bestSetForCard(card, scorer);
					bestScores.put(reusableState, reusableState.getValue(scorer) * -1);
				}
			} else {
				// Live flush draw somewhere, can't reuse these states
				// Technically reusable if the flush draws are identical and identically live, which might actually
				// help a lot on very early streets, but not dealing with that for now.
				GameState state = bestSetForCard(card, scorer);
				bestScores.put(state, state.getValue(scorer) * -1);
			}
		}
	}
	
	/**
	 * Generate the value of each best state reachable from this one, i.e., optimal setting for each card.
	 */
	private Multimap<GameState, Double> generateBestStateValues(final Scorers.Scorer scorer, boolean concurrent) {
		if (player1.isComplete()) {
			throw new IllegalStateException("Shouldn't generate new states for complete hand");
		}

		// ListMultimap allows duplicate key/value pairs
		final ListMultimap<GameState, Double> bestScores = Multimaps.synchronizedListMultimap(ArrayListMultimap.<GameState, Double>create());
		final boolean[] liveFlushDraws = liveFlushDraws();
		
		// This is a bad way to split up the work, since the time spent will be the max of the number of cards of any given rank,
		// but for now it makes the code simpler
		if (concurrent) {
			List<Future<?>> futures = Lists.newArrayList();
			int fs = futures.size();
			int k = bestScores.keySet().size();
			for (final List<OfcCard> rankList : deck.byRank()) {
				futures.add(executor.submit(new Runnable() {
					@Override
					public void run() {
						updateBestStates(bestScores, liveFlushDraws, scorer, rankList);
					};
				}));
				fs = futures.size();
				k = bestScores.keySet().size();
			}
			for (Future<?> f : futures) {
				try {
					f.get();
				} catch (Exception e) { // no recovery
					throw new RuntimeException(e);
				}
			}
		} else {
			for (List<OfcCard> rankList : deck.byRank()) {
				updateBestStates(bestScores, liveFlushDraws, scorer, rankList);
			}
		}
		
		if (counter.longValue() > 100000) {
			counter.set(0L);
			System.out.println(getInstrumentation());
		}

		// sanity check
		if (bestScores.keySet().size() > deck.toArray().length) {
			throw new IllegalStateException("Returning more settings than cards available");
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
	public double getValue(Scorers.Scorer scorer) {
		if (getStreet() > 13.0) {
			throw new IllegalStateException("never happen");
		}
		return CACHE.getUnchecked(this);
	}
	
	/**
	 * Calculate the value when it's not available in the cache
	 */
	private double calculateValue(Scorers.Scorer scorer, boolean concurrent) {
		// 13th street, no more choices		
		if (getStreet() == 13.0) {
			return calculate13thStreet(scorer);
		}

		double value;
		if (getStreet() > 11.5 && concurrent) {
			value = getValue(scorer, true);
		} else {
			value = getValue(scorer, false);
		}
		
		if (getStreet() < 11.5) {
			System.out.println("\n\nSolved");
			System.out.println(this);
			System.out.println(value);
//			System.out.println(getInstrumentation());
/*
			try {
				for (Scorers.Scorer scorer : scorers) {
					FileWriter fw = new FileWriter(CACHE_FILES.get(scorer.getCacheFile()).getAbsoluteFile(), true);
					BufferedWriter bw = new BufferedWriter(fw);
					bw.write(toFileString() + " " + values.get(scorer.getKey()) + "\n");
					bw.flush();
					bw.close();
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
*/
		}
		return value;
	}

	private double calculate13thStreet(Scorers.Scorer scorer) {
		OfcCard[] cards =  CardSetUtils.asCards(deck.getMask());
		LongOfcHandMatrix matrix = new LongOfcHandMatrix(player1, player2, cards);
		int count = cards.length * (cards.length - 1);
		Map<String, Double> scores = Maps.newHashMap();
		int score = scorer.score(matrix); 
		return (double) score / count;
	}

	public double getValue(Scorers.Scorer scorer, boolean concurrent) {
		Multimap<GameState, Double> bestStates = generateBestStateValues(scorer, concurrent);
		double value = 0.0;
		int count = 0;
		for (GameState state : bestStates.keySet()) {
			int stateCount = bestStates.get(state).size();
			count += stateCount;
			value += (bestStates.get(state).iterator().next() * stateCount);
		}
		return value / count;
	}

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

	public static void shutdown() {
		executor.shutdown();
	}
	
	@Override
	public String toString() {		
		StringBuilder sb = new StringBuilder();
		sb.append("Game state: \n");
		sb.append("Street: " + getStreet() + "\n");
		sb.append("Cards in deck: " + CardSetUtils.asCards(deck.getMask()).length + "\n");
		sb.append(player1);
		sb.append("\n");
		sb.append(player2);
		sb.append("\n");
		if (isComplete()) {
			sb.append(getValue(Scorers.getScorer()) + " points\n");
		}
		sb.append(deck);
		return sb.toString();
	}
}
