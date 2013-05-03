import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.google.common.collect.ConcurrentHashMultiset;

public class GameState {
	
	ExecutorService executor = Executors.newFixedThreadPool(1);

	private static final Cache<GameState, Double> cache =
		CacheBuilder.newBuilder().concurrencyLevel(2)
			.maximumSize(50000L)
			.recordStats().build();
	
//	private static ConcurrentMap<GameState, Double> cache = Maps.newConcurrentMap();
	
	// instrumentation
	private static AtomicLong statesGenerated = new AtomicLong(0L);
	private static AtomicLong counter = new AtomicLong(0L);
	private static final long startTime = System.currentTimeMillis();
	private static long timestamp = 0L;
	private static final Multiset<Double> streetStatesGenerated = ConcurrentHashMultiset.create();
		
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
	}
	
	private String getInstrumentation() {
		StringBuilder sb = new StringBuilder();
		sb.append(statesGenerated.longValue() + "\n");
		long newTime = System.currentTimeMillis();
		sb.append("total time: " + (newTime - startTime) / 1000 + " seconds \n");
		sb.append("since last time: " + (newTime - timestamp) + " ms\n");
		timestamp = newTime;
		Double[] streets = streetStatesGenerated.elementSet().toArray(new Double[0]);
		Arrays.sort(streets);
		for (Double street : streets) {
			sb.append(street + ": " + streetStatesGenerated.count(street) + "\n");
		}
//		sb.append("cache hits/misses/size: " + cacheHits + " " + cacheMisses + " " + cache.size() + "\n");
		sb.append(cache.stats() + "\n");
		return sb.toString();
	}
	
	/**
	 * Generate best states reachable from this state..
	 * The next states will be from the perspective of the opponent.
	 */
	public Set<GameState> generateBestStates() {
		if (player1.isComplete()) {
			throw new IllegalStateException("Shouldn't generate new states for a complete hand");
		}
		Set<GameState> newStates = Sets.newHashSet();
		for (String card : deck.allCards()) {
			// TODO: Double.MIN_VALUE doesn't compare properly.
			double handSettingValue = -1000000000.0;
			GameState bestSet = null;

			Set<OfcHand> hands = player1.generateHands(new OfcCard(card));
			for (OfcHand hand : hands) {
				GameState candidateState = new GameState(player2, hand, new OfcDeck(deck.withoutCard(card)));
				if (hands.size() == 1) {
					// Save the effort of finding the best setting when there's only one possible set
					bestSet = candidateState;
				} else if (candidateState.getValue() > handSettingValue) {
					handSettingValue = candidateState.getValue();
					bestSet = candidateState;
				}
			}
			if (bestSet != null) {
				newStates.add(bestSet);
			}
		}
		if (!newStates.isEmpty()) {
			statesGenerated.addAndGet(newStates.size());
			counter.addAndGet(newStates.size());
			streetStatesGenerated.add(newStates.iterator().next().getStreet(), newStates.size());
			if (counter.longValue() > 10000) {
				counter.set(0L);
				System.out.println(getInstrumentation());
			}
		}
//		System.out.println("adding states for street: " + getStreet() + newStates);
		return newStates;
	}

	public boolean isComplete() {
		return player1.isComplete() && player2.isComplete();
	}
	
	// all states reachable from this state, without choosing optimal hand setting
	private Set<GameState> generateAllStates() {
		if (player1.isComplete()) {
			throw new IllegalStateException("Shouldn't generate new states for a complete hand");
		}
		Set<GameState> newStates = Sets.newHashSet();
		for (String card : deck.allCards()) {
			Set<OfcHand> hands = player1.generateHands(new OfcCard(card));
			for (OfcHand hand : hands) {
				GameState candidateState = new GameState(player2, hand, new OfcDeck(deck.withoutCard(card)));
			}
		}
		return newStates;
	}
	
	public double getValueBfs() {

		for (GameState nextState : generateAllStates()) {
			
		}
		return 0.0;
	}
	
	/**
	 * Get the value of this state for the first player.  If the game is complete, this is the score.
	 * Otherwise, it is the average of all states generated by dealing the opponent a card.
	 */
	public double getValue() {

		Double value = cache.getIfPresent(this);
		if (value != null) {
//			System.out.println(cache.stats());
			return value;
		}
		/*
		if (cache.containsKey(this)) {

			String thisString = toString();
			String thatString = cache.keySet().iterator().next().toString();
			int key = cache.keySet().iterator().next().hashCode();
			int thisKey = hashCode();
			cacheHits.incrementAndGet();
//			System.out.println("cache hit: " + cacheHits + " " + cacheMisses + " " + cache.size());
			return cache.get(this);
		} else {
			cacheMisses.incrementAndGet();
		}
		*/
		// 13th street, no more choices		
		if (getStreet() == 13.0) {
			int count = 0;
			double sum = 0.0;
			for (String p1Card : deck.allCards()) {
				Set<OfcHand> p1Hands = player1.generateHands(new OfcCard(p1Card));
				if (p1Hands.size() != 1) {
					throw new IllegalStateException("should only generate one hand");
				}
				OfcHand p1Hand = p1Hands.iterator().next();
				for (String p2Card : new OfcDeck(deck.withoutCard(p1Card)).allCards()) {
					Set<OfcHand> p2Hands = player2.generateHands(new OfcCard(p2Card));
					if (p2Hands.size() != 1) {
						throw new IllegalStateException("should only generate one hand");
					}
					OfcHand p2Hand = p2Hands.iterator().next();
					count++;
					sum += p1Hand.scoreAgainst(p2Hand);
				}
			}
//			System.out.println(count + " " + sum + " " + sum/count);
//			System.out.println(this);
			cache.put(this,  sum / count);
			return sum / count;
		}
		
		if (getStreet() > 13.0) {
			throw new IllegalStateException("never happen");
		}
		if (!isComplete()) {
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
			
			if (getStreet() < 11.5) {
				System.out.println("\n\nSolved");
				System.out.println(this);
				System.out.println(sum / count * -1);
				System.out.println(getInstrumentation());
			}
			
			/*
			if (cache.containsKey(this)) {
				// okay, maybe two threads inserted into the cache simultaneously
				if (cache.get(this) != sum / count * -1) {
					throw new IllegalStateException("Different value in cache (" + cache.get(this) + "): "
						+ ( sum / count * -1) + " " + this);
				}
			} else {
				cache.put(this, sum / count * -1);
			}
			*/
			cache.put(this,  sum / count * -1);
			// Current player's value is the negative of the opponent's value.
			return sum / count * -1;
		}

		// this should never happen either
		throw new IllegalStateException();
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
		sb.append("Cards in deck: " + deck.allCards().length + "\n");
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