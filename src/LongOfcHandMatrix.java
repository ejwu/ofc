import java.util.Arrays;

public class LongOfcHandMatrix implements OfcHandMatrix {
	// Implementation notes:
	
	// Each of the 2 players has 3 subhands. In the comments below, i refers to
	// a card index in 0..deckSize, p to a player index (0=p1, 1=p2), h to a
	// subhand index (0=front, 1=middle, 2=back).

	// For each player, one subhand varies based on which card is dealt to em,
	// and the other two subhands don't vary.

	// The naive implementation answers the questions of the OfcHandMatrix using
	// time quadratic in the size of the deck. By some tricky sorting and
	// inclusion-exclusion, we can make this n*log(n). But the deck size is
	// fixed at 28, so if we're not careful, our overhead will eat our gains.

	// (The general format of inclusion-exclusion is to consider p1 getting all
	// deckSize possible cards, then consider p2 getting all deckSize possible
	// cards, then subtract out the cases where they both got the same card.)

	// There are 9 possible ways the two handsets can vary. The lowest-overhead
	// approach would be 9 different implementations of the matrix, but that
	// would require a lot of copypasta. This way requires only two cases:
	// whether the the two handsets vary in the same subhand, or in different
	// ones. It has rather more array-index-arithmetic, however. Perhaps the JIT
	// will save us.

	// Size of the deck before we started dealing. The matrix we build will be
	// deckSize * deckSize, with the diagonal missing (since the two players
	// cannot receive the same card)
	private final int deckSize;

	// how many of the deckSize * (deckSize - 1) showdowns have p1
	// winning. TODO(ejwu): this biases towards p2 when subhands tie, which
	// AFAICT is a bug in the original codebase.
	private final int[] winCount = new int[4];
	
	// isFouled[p][i] tells whether player p's hand i is fouled.
	private final boolean[][] isFouled;
	
	// How many cards out of deckSize foul player[p]'s hand
	private final int[] foulCount = new int[2];

	// varies[p] is the index of p's subhand which varies in the matrix,
	// or -1 if the player always fouls.
	private final int[] varies = new int[2];

	// stableRanks[p][h], when h != varies[p], is the rank of player[p]'s stable
	// subhand at index h (regardless of which final card is dealt).
	// stableRanks[p][varies[p]] is undefined.
	private final long[][] stableRanks = new long[2][3];

	// ranks[p][i] gives the StupidEval rank of player p's subhand at
	// h=varies[p], given that the final card i was dealt--or the special value
	// FOULED if that board is fouled.
	private final static long FOULED = -1;
	private final long[][] ranks;
	
	// sortedRanks[p] is the array ranks[p] sorted in ascending order.
	private final long[][] sortedRanks;
	
	public LongOfcHandMatrix(OfcHand p1, OfcHand p2, OfcCard[] cards) {
		if ((p1.getStreet() != 13) || (p2.getStreet() != 13)) {
			throw new IllegalArgumentException(
				"Wrong streets! " + p1.getStreet() + "," + p2.getStreet());
		}
		boolean flushDraw = p1.hasFlushDraw() || p2.hasFlushDraw();
		if (p1.hasFlushDraw()) {
			System.out.println("flushdraw " + p1);
		}
		if (p2.hasFlushDraw()) {
			System.out.println("flushdraw " + p2);
		}

		deckSize = cards.length;
		ranks = new long[2][deckSize];
		sortedRanks = new long[2][deckSize];
		// TODO(abliss): remove casts
		LongOfcHand[] startHands = new LongOfcHand[] {
			(LongOfcHand) p1, (LongOfcHand) p2};
		isFouled = new boolean[2][deckSize];
		int lastCardRank = -1;
		for (int p = 0; p < 2; p++) {
			LongOfcHand startHand = startHands[p];
			if (startHand.willBeFouled()) {
				// If the hand is fouled anyway, we don't care about dealing it
				// out.
				foulCount[p] = deckSize;
				varies[p] = -1;
				stableRanks[p][FRONT] = FOULED;
				stableRanks[p][MIDDLE] = FOULED;
				stableRanks[p][BACK] = FOULED;
				for (int i = deckSize - 1; i >= 0; i--) {
					isFouled[p][i] = true;
				}
			} else if (startHand.getFrontSize() < OfcHand.FRONT_SIZE) {
				varies[p] = FRONT;
				stableRanks[p][MIDDLE] = startHand.getMiddleRank();
				stableRanks[p][BACK] = startHand.getBackRank();
				for (int i = deckSize - 1; i >= 0; i--) {
					OfcCard card = cards[i];
					if (!flushDraw && card.getRank() == lastCardRank) {
						if (isFouled[p][i + 1]) {
							isFouled[p][i] = true;
							foulCount[p]++;
						}
						sortedRanks[p][i] = ranks[p][i] = ranks[p][i + 1];
					} else {

						lastCardRank = card.getRank();
						long rank = startHand.getFrontRank(card);
						// TODO(abliss): if we were willing to carry the index
						// backmap through the sorting, we could do this faster
						// by zeroing out the fouled hands after we sort the
						// ranks.
						if (rank == FOULED) {
							isFouled[p][i] = true;
							foulCount[p]++;
						}
						sortedRanks[p][i] = ranks[p][i] = rank;
					}
				}
			} else if (startHand.getMiddleSize() < OfcHand.MIDDLE_SIZE) {
				stableRanks[p][FRONT] = startHand.getFrontRank();
				varies[p] = MIDDLE;
				stableRanks[p][BACK] = startHand.getBackRank();
				for (int i = deckSize - 1; i >= 0; i--) {
					OfcCard card = cards[i];
					if (!flushDraw && card.getRank() == lastCardRank) {
						if (isFouled[p][i + 1]) {
							isFouled[p][i] = true;
							foulCount[p]++;
						}
						sortedRanks[p][i] = ranks[p][i] = ranks[p][i + 1];
					} else {
						lastCardRank = card.getRank();
						long rank = startHand.getMiddleRank(card);
						if (rank == FOULED) {
							isFouled[p][i] = true;
							foulCount[p]++;
						}
						sortedRanks[p][i] = ranks[p][i] = rank;
					}
				}
			} else {
				stableRanks[p][FRONT] = startHand.getFrontRank();
				stableRanks[p][MIDDLE] = startHand.getMiddleRank();
				varies[p] = BACK;
				for (int i = deckSize - 1; i >= 0; i--) {
					OfcCard card = cards[i];
					if (!flushDraw && card.getRank() == lastCardRank) {
						if (isFouled[p][i + 1]) {
							isFouled[p][i] = true;
							foulCount[p]++;
						}
						sortedRanks[p][i] = ranks[p][i] = ranks[p][i + 1];
					} else {
						lastCardRank = card.getRank();
						long rank = startHand.getBackRank(card);
						if (rank == FOULED) {
							isFouled[p][i] = true;
							foulCount[p]++;
						}
						sortedRanks[p][i] = ranks[p][i] = rank;
					}
				}
			}
			Arrays.sort(sortedRanks[p]);
		}

		countFouls();
		if (varies[0] == varies[1]) {
			countWinsVariesSame();
		} else {
			countWinsVariesDifferent();
		}
	}
	
	// compute winCount values when one side fouls. Returns totalFouls
	private void countFouls() {
		final int[] foulWins = new int[]{0, 3};
		if (varies[0] < 0) {
			// p1 always fouls
			if (varies[1] >= 0) {
				// but p2 doesn't.
				winCount[foulWins[0]] = deckSize * (deckSize - 1);
			}
		} else if (varies[1] < 0) {
			// vice versa.
			winCount[foulWins[1]] += deckSize * (deckSize - 1);
		} else {
			// Neither player always fouls.
			for (int p = 0; p < 2; p++) {
				if (foulCount[p] > 0) {
					for (int i = deckSize - 1; i >= 0; i--) {
						if (isFouled[p][i]) {
							// Set wincount everywhere opponent didn't foul, by
							// inclusion-exclusion:
							int count = (deckSize - 1 - foulCount[1 - p]);
							// foulCount will be off by one for this purpose if
							// it includes the case where the opponent got the
							// same card.
							if (isFouled[1 - p][i]) {
								count++;
							}
							winCount[foulWins[p]] += count;
						}
					}
				}
			}
		}
	}

	// Uses binary search in the given sorted array, looking for the first index
	// larger than the given key. Starts looking at start. Returns sorted.length
	// if none found.
	private static int firstIndexBigger(long[] sorted, long key, int start) {
		int found = Arrays.binarySearch(sorted, start, sorted.length, key + 1);
		if (found >= 0) {
			return found;
		}
		return -1 - found;
	}
	
	// Compute foulless winCount values when varies[0] == varies[1]
	private void countWinsVariesSame() {
		if ((foulCount[0] == deckSize) || (foulCount[1] == deckSize)) {
			// one player always fouls. No foulless wins to count.
			return;
		}
		int stableWins = 0;
		for (int h = BACK; h >= FRONT; h--) {
			if (h != varies[0]) {
				if (stableRanks[0][h] > stableRanks[1][h]) {
					stableWins++;
				}
			}
		}

		// Count how many of P1's varied hands beat P2's varied hands,
		// by simultaneous scan. Inclusion:
		int[] index = new int[2];
		int wins = 0;
		// start off past the fouls
		for (int p = 0; p < 2; p++) {
			index[p] = foulCount[p];
		}

		while (true) {
			// Scan p1 forward until he can beat p2
			index[0] = firstIndexBigger(sortedRanks[0],
									   sortedRanks[1][index[1]],
									   index[0]);
			if (index[0] >= deckSize) {
				// P1 couldn't muster a winning hand. No more wins to count.
				break;
			}
			// P1's hand, and all hands better than it, are winning.
			final int numWinners = deckSize - index[0];
			// How many *new* hands are we beating?
			int lastIndex1 = index[1];
			index[1] = firstIndexBigger(sortedRanks[1],
									   sortedRanks[0][index[0]],
									   lastIndex1 + 1);
			final int numNewLosers = index[1] - lastIndex1;
			wins += numWinners * numNewLosers;
			if (index[1] >= deckSize) {
				// No more hands to count.
				break;
			}
		}
		// How many showdowns contain at least one foul: inclusion
		int totalFouls =
			foulCount[0] * deckSize +
			foulCount[1] * deckSize -
			foulCount[0] * foulCount[1];

		// Exclusion: remove foulless wins and fouled showdowns on the diagonal
		for (int i = deckSize - 1; i >= 0; i--) {
			if ((ranks[0][i] == FOULED) || (ranks[1][i] == FOULED)) {
				totalFouls--;
			} else if (ranks[0][i] > ranks[1][i]) {
				wins--;
			}
		}
		// Award all the foulless wins
		winCount[stableWins + 1] += wins;
		// Award all the foulless losses
		int losses = deckSize * (deckSize - 1) - totalFouls - wins;
		winCount[stableWins] += losses;
	}
	
	// Compute foulless winCount values when varies[0] != varies[1]
	private void countWinsVariesDifferent() {
		if ((foulCount[0] == deckSize) || (foulCount[1] == deckSize)) {
			// one player always fouls. No foulless wins to count.
			return;
		}
		int stableWins = 0;
		for (int h = BACK; h >= FRONT; h--) {
			if ((h != varies[0]) && (h != varies[1])) {
				if (stableRanks[0][h] > stableRanks[1][h]) {
					stableWins++;
				}
			}
		}

		// Inclusion:
		// How many times does p1 get an extra win from eir varied hand?
		int p1Win = firstIndexBigger(sortedRanks[0],
									 stableRanks[1][varies[0]],
									 foulCount[0]);
		// How many times does p2 get an extra win OR TIE from eir varies hand?
		int p2Win = firstIndexBigger(sortedRanks[1],
									 stableRanks[0][varies[1]] - 1,
									 foulCount[1]);
		// Add up the four corners of this punnett square
		winCount[stableWins] +=
			(deckSize - p2Win) * (p1Win - foulCount[0]);
		winCount[stableWins + 1] +=
			(deckSize - p2Win) * (deckSize - p1Win) +
			(p2Win - foulCount[1]) * (p1Win - foulCount[0]);
		winCount[stableWins + 2] +=
			(deckSize - p1Win) * (p2Win - foulCount[1]);

		// Exclusion: remove foulless wins on the diagonal
		for (int i = deckSize - 1; i >= 0; i--) {
			if ((ranks[0][i] != FOULED) && (ranks[1][i] != FOULED)) {
				int wins = stableWins;
				if (ranks[0][i] > stableRanks[1][varies[0]]) {
					wins++;
				}
				if (ranks[1][i] < stableRanks[0][varies[1]]) {
					wins++;
				}
				winCount[wins]--;
			}
		}
	}
	
	@Override
	public int numHandsOfRank(int player, int h, long rank) {
		// shortcut if hand is always fouled
		if (varies[player] < 0) {
			return 0;
		}
		
		if (varies[player] != h) {
			if (stableRanks[player][h] >= rank) {
				return (deckSize - foulCount[player]) * (deckSize - 1);
			} else {
				return 0;
			}
		} else {
			int ind = firstIndexBigger(sortedRanks[player],
									   rank - 1, foulCount[player]);
			return (deckSize - ind) * (deckSize - 1);
		}
	}
	
	@Override
	public int numP1Wins(int wins) {
		return winCount[wins];
	}
}

