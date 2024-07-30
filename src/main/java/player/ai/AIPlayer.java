package player.ai;

import game.Board;
import player.Player;
import player.PlayerProperty;
import player.ai.genetic.Genome;

import java.util.Objects;
import java.util.Random;

public class AIPlayer extends Player {

    private static final int[] TURN_ORDER = new int[]{4, 5, 6, 7, 1, 2, 3};

    private Genome genome;
    private byte[][] posScore;

    public AIPlayer(PlayerProperty playerProperty, Board board, boolean beginningPlayer, Genome genome) {
        super(playerProperty, board, beginningPlayer);
        this.genome = genome;
    }

    public AIPlayer(PlayerProperty playerProperty, Board board, Player opponent, boolean beginningPlayer, Genome genome) {
        super(playerProperty, board, opponent, beginningPlayer);
        this.genome = genome;
    }



    @Override
    public boolean makeMove() {
        int calculatedMove = calculateMove();
        return board.placeToken(calculatedMove, this);
    }

    private int calculateMove() {
        int bestMove = -1;
        int score = Integer.MIN_VALUE;
        int propabilityBound = 2;

        for (int i = 0; i < TURN_ORDER.length; i++) {
            if (board.placeToken(TURN_ORDER[i], this)) {
                int value = minimax(8, false, Integer.MIN_VALUE, Integer.MAX_VALUE);
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

    private int evalPosScore(Player player) {
        return player.getPlayersTokens().map(t -> posScore[t.row][t.col]).mapToInt(Byte::intValue).sum();
    }

    private int evalGameWon(Player player) {
        return board.getHasWon().getID() == player.getID() ? genome.winEvaluation() : 0;
    }

    private int evalMajorThreats(Player player) {
        return board.getMajorThreats(player);
    }

    private int evalMinorThreats(Player player) {
        return board.getMinorThreats(player);
    }



    public void setGenome(Genome genome) {
        this.genome = genome;
    }

    public Genome getGenome() {
        return this.genome;
    }
}
