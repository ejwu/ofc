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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

public class GameState {
	
	ExecutorService executor = Executors.newCachedThreadPool();
	
	private static final File CACHE_FILE;
	
	private static final LoadingCache<GameState, Double> cache =
		CacheBuilder.newBuilder().concurrencyLevel(8)
			.maximumSize(3000000L)
			.recordStats()
			.build(new CacheLoader<GameState, Double>() {
				public Double load(GameState state) {
					return state.calculateValue();
				}
			});
			

	// bit mask for the full deck, used for checking that a state is valid
	private static final long FULL_DECK_MASK;
	
	static {
		long fullDeck = 0L;
		for (int i = 0; i < 52; i++) {
			fullDeck <<= 1;
			fullDeck |= 1L;
		}
		FULL_DECK_MASK = fullDeck;
		
		CACHE_FILE = new File("cache.txt");
		if (!CACHE_FILE.exists()) {
			try {
				CACHE_FILE.createNewFile();
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
				System.out.println(s);
				if (!s.trim().isEmpty()) {
					cache.put(fromFileString(s), getValueFromFileString(s));
				}
				s = reader.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println(cache.stats());
			try {
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("load from file took " + (System.currentTimeMillis() - timestamp));
	}
	
	// instrumentation
	private static AtomicLong statesSolved = new AtomicLong(0L);
	private static AtomicLong counter = new AtomicLong(0L);
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
		sb.append(cache.stats() + "\n");
		return sb.toString();
	}
	
	/**
	 * Generate the value of each best state reachable from this one, i.e., optimal setting for each card.
	 */
	private Map<GameState, Double> generateBestStateValues() {
		if (player1.isComplete()) {
			throw new IllegalStateException("Shouldn't generate new states for complete hand");
		}
		Map<GameState, Double> bestStates = Maps.newHashMap();
		for (OfcCard card : CardSetUtils.asCards(deck.getMask())) {
			// TODO: Double.MIN_VALUE doesn't compare properly.
			GameState bestSet = null;
			double bestValue = -1000000000000.0;

			Set<OfcHand> hands = player1.generateHands(card);
			for (OfcHand hand : hands) {
				GameState candidateState = new GameState(player2, hand, new OfcDeck(deck.withoutCard(card)));
				double value = candidateState.getValue();
				// candidateState's value is from the opponent's perspective, so we flip it
				if (value * -1 > bestValue) {
					bestSet = candidateState;
					bestValue = value * -1;
				}
			}
			// shouldn't happen
			if (bestStates.containsKey(bestSet)) {
				throw new IllegalStateException("what happened?");
			}
			bestStates.put(bestSet, bestValue);
		}

		if (!bestStates.isEmpty()) {
			statesSolved.addAndGet(bestStates.size());
			counter.addAndGet(bestStates.size());
			statesSolvedByStreet.add(bestStates.keySet().iterator().next().getStreet(), bestStates.size());
			if (counter.longValue() > 10000) {
				counter.set(0L);
				System.out.println(getInstrumentation());
			}
		} else {
			throw new IllegalStateException("never happen");
		}

		// sanity check
		if (bestStates.size() > (52 * 3)) {
			throw new IllegalStateException("shouldn't happen");
		}
		
		return bestStates;
	}
	
	public boolean isComplete() {
		return player1.isComplete() && player2.isComplete();
	}
	
	/**
	 * Get the value of this state for the first player.  If the game is complete, this is the score.
	 * Otherwise, it is the average of all states generated by dealing the opponent a card.
	 */
	public double getValue() {
		if (getStreet() > 13.0) {
			throw new IllegalStateException("never happen");
		}

		Double value = cache.getUnchecked(this);
		if (value != null) {
			return value;
		}
		return calculateValue();
	}
	
	/**
	 * Calculate the value when it's not available in the cache
	 */
	private double calculateValue() {
		// 13th street, no more choices		
		if (getStreet() == 13.0) {
			return calculate13thStreet();
		}

		double value;
		value = getValueSequential();
			
		if (getStreet() < 12.0) {
			System.out.println("\n\nSolved");
			System.out.println(this);
			System.out.println(value);
			System.out.println(getInstrumentation());
			
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
		}

		// TODO: be smarter about cache policy
		cache.put(this, value);
		return value;
	}

	private double calculate13thStreet() {
		int count = 0;
		double sum = 0.0;
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
				sum += p1Hand.scoreAgainst(p2Hand);
			}
		}
		double value = sum / count;
		
		cache.put(this, value);
		return value;
	}

	public double getValueSequential() {
		double sum = 0.0;
		int count = 0;
		
		Map<GameState, Double> bestStates = generateBestStateValues();
		
		for (GameState state : bestStates.keySet()) {
			count++;
			sum += bestStates.get(state);
		}
		
		// Current player's value is the negative of the opponent's value.
		return sum / count;
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
			sb.append(getValue() + " points\n");
		}
		sb.append(deck);
		return sb.toString();
	}
}
