package player.ai;

import game.Board;
import player.Player;
import player.PlayerProperty;
import player.ai.genetic.Genome;

import java.util.Objects;
import java.util.Random;

/**
 * AI-driven player implementation that selects moves using a Minimax search with alpha-beta pruning
 * and evaluates board states using genome-encoded heuristics.
 *
 * <p>
 * The AI uses a fixed move order {@link #TURN_ORDER} to evaluate central columns first when
 * exploring possible moves. The evaluation function is parameterized
 * by a {@link Genome} instance which encodes weights for positional scores, major/minor threats,
 * and winning evaluation. The genome also supplies a positional scoring matrix used by
 * {@link #evalPosScore(Player)}.
 * </p>
 *
 * <p>Thread-safety: this class is not synchronized. Instances are intended to be used
 * by a single game thread at a time.</p>
 */
public class AIPlayer extends Player {

    private static final int[] TURN_ORDER = new int[]{4, 5, 3, 6, 2, 7, 1};

    private Genome genome;
    private byte[][] posScore;

    /**
     * Constructs an AIPlayer that does not yet have an opponent reference.
     *
     * @param playerProperty  property identifying this player (PLAYER1/PLAYER2)
     * @param board           shared game board instance
     * @param beginningPlayer whether this player starts the match
     * @param genome          genome controlling evaluation heuristics
     */
    public AIPlayer(PlayerProperty playerProperty, Board board, boolean beginningPlayer, Genome genome) {
        super(playerProperty, board, beginningPlayer);
        this.genome = genome;
    }

    /**
     * Constructs an AIPlayer with an opponent reference.
     *
     * @param playerProperty  property identifying this player (PLAYER1/PLAYER2)
     * @param board           shared game board instance
     * @param opponent        the opposing player instance
     * @param beginningPlayer whether this player starts the match
     * @param genome          genome controlling evaluation heuristics
     */
    public AIPlayer(PlayerProperty playerProperty, Board board, Player opponent, boolean beginningPlayer, Genome genome) {
        super(playerProperty, board, opponent, beginningPlayer);
        this.genome = genome;
    }



    /**
     * Request the AI to perform a move. The AI calculates its best move using {@link #calculateMove()}
     * and places the token on the board. The return value indicates whether the placement succeeded.
     *
     * @return {@code true} if the token was successfully placed; {@code false} otherwise
     */
    @Override
    public boolean makeMove() {
        int calculatedMove = calculateMove();
        return board.placeToken(calculatedMove, this);
    }

    /**
     * Determines the best column to play by iterating over {@link #TURN_ORDER}, performing
     * hypothetical placements, running a Minimax search and selecting the column with the
     * highest evaluation. Ties are broken pseudo-randomly to introduce some diversity.
     *
     * @return chosen 1-based column index to place the token, or -1 if no legal move found
     */
    private int calculateMove() {

        int bestMove = -1;
        int score = Integer.MIN_VALUE;
        int propabilityBound = 2;

        for (int i = 0; i < TURN_ORDER.length; i++) {
            if (board.placeToken(TURN_ORDER[i], this)) {
                int value = minimax(8, false, Integer.MIN_VALUE, Integer.MAX_VALUE); //depth of the tree = depth + 1
                board.removeToken(TURN_ORDER[i]);

                if (value >= score) {
                    if (value == score) {
                        if (new Random().nextInt(propabilityBound) == 0) {
                            bestMove = TURN_ORDER[i];
                            propabilityBound++;
                        }
                    } else {
                        bestMove = TURN_ORDER[i];
                        score = value;
                    }
                }
            }
        }

        return bestMove;
    }

    /**
     * Recursive minimax search with alpha-beta pruning.
     *
     * @param depth        remaining depth to search (0 = evaluate leaf)
     * @param isMaximizing {@code true} if the current node is a maximizing node (AI player's turn)
     * @param alpha        alpha value for pruning
     * @param beta         beta value for pruning
     * @return evaluation integer score for the current subtree
     */
    public int minimax(int depth, boolean isMaximizing, int alpha, int beta) {
        if (depth == 0 || board.getIsGameFinished()) {
            return evaluateBoard() * (depth + 1);
        }

        if (isMaximizing) { //AI IS MAXIMIZING PLAYER
            int maxEval = Integer.MIN_VALUE;
            for (int col = 0; col < TURN_ORDER.length; col++) {
                if (board.placeToken(TURN_ORDER[col], this)) {
                    int eval = minimax(depth - 1, false, alpha, beta);
                    board.removeToken(TURN_ORDER[col]);
                    maxEval = Math.max(maxEval, eval);
                    alpha = Math.max(alpha, maxEval);
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            return maxEval;
        } else { //AI OPPONENT IS MINIMIZING PLAYER
            int minEval = Integer.MAX_VALUE;
            for (int col = 0; col < TURN_ORDER.length; col++) {
                if(board.placeToken(TURN_ORDER[col], this.getOpponent())) {
                    int eval = minimax(depth - 1, true, alpha, beta);
                    board.removeToken(TURN_ORDER[col]);
                    minEval = Math.min(minEval, eval);
                    beta = Math.min(beta, minEval);
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            return minEval;
        }
    }



    /**
     * Evaluates the current board from this AI's perspective using the genome-provided heuristics.
     * The method:
     * <ol>
     *   <li>refreshes the {@link #posScore} table from the genome</li>
     *   <li>accumulates positional scores for both players weighted by genome parameters</li>
     *   <li>adds major/minor threat contributions for both players</li>
     *   <li>if the game is finished, adds win/loss weights</li>
     * </ol>
     *
     * @return integer evaluation score (higher = better for this AI)
     */
    private int evaluateBoard() {
        this.posScore = genome.posScore();

        int evaluationScore = 0;

        evaluationScore += evalPosScore(this) * genome.posScoreWeightPlayer();
        evaluationScore += evalPosScore(this.getOpponent()) * genome.posScoreWeightOpponent();

        evaluationScore += evalMajorThreats(this) * genome.majorWeightPlayer();
        evaluationScore += evalMajorThreats(this.getOpponent()) * genome.majorWeightOpponent();

        evaluationScore += evalMinorThreats(this) * genome.minorWeightPlayer();
        evaluationScore += evalMinorThreats(this.getOpponent()) * genome.minorWeightOpponent();

        if (board.getIsGameFinished() && Objects.nonNull(board.getHasWon())) {
            evaluationScore += evalGameWon(this) * genome.winWeightPlayer();
            evaluationScore += evalGameWon(this.getOpponent()) * genome.winWeightOpponent();
        }

        return evaluationScore;
    }

    /**
     * Computes the aggregate positional score for all tokens owned by the given player
     * by summing corresponding entries in the {@link #posScore} table.
     *
     * @param player player whose tokens are scored
     * @return sum of positional scores for the player's tokens
     */
    private int evalPosScore(Player player) {
        return player.getPlayersTokens().map(t -> posScore[t.row][t.col]).mapToInt(Byte::intValue).sum();
    }

    /**
     * Returns the genome-encoded value awarded when the specified player is the winner.
     *
     * @param player player to evaluate (compared to {@link Board#getHasWon()})
     * @return genome.winEvaluation() if the player has won; otherwise 0
     */
    private int evalGameWon(Player player) {
        return board.getHasWon().getID() == player.getID() ? genome.winEvaluation() : 0;
    }

    /**
     * Proxy to {@link Board#getMajorThreats(Player)} for the given player.
     *
     * @param player player to evaluate
     * @return major threat count from the board heuristic
     */
    private int evalMajorThreats(Player player) {
        return board.getMajorThreats(player);
    }

    /**
     * Proxy to {@link Board#getMinorThreats(Player)} for the given player.
     *
     * @param player player to evaluate
     * @return minor threat count from the board heuristic
     */
    private int evalMinorThreats(Player player) {
        return board.getMinorThreats(player);
    }



    /**
     * Replace the genome used by this AI. The caller is responsible for providing
     * a consistent genome (matching expected length and semantics).
     *
     * @param genome new genome to use for evaluation
     */
    public void setGenome(Genome genome) {
        this.genome = genome;
    }

    /**
     * Returns the genome currently used by this AI.
     *
     * @return genome instance
     */
    public Genome getGenome() {
        return this.genome;
    }
}
