package player;

import game.Board;

import java.util.Objects;

public class AIPlayer extends Player {

    public AIPlayer(PlayerProperty playerProperty, Board board) {
        super(playerProperty, board);
    }

    public AIPlayer(PlayerProperty playerProperty, Board board, Player opponent) {
        super(playerProperty, board, opponent);
    }

    @Override
    public boolean makeMove() {
        int calculatedMove = calculateMove();
        return board.placeToken(calculatedMove, this);
    }

    private int calculateMove() {
        int bestMove = -1;
        int score = Integer.MIN_VALUE;

        for (int i = 1; i < 8; i++) {
            if (board.placeToken(i, this)) {
                int value = minimax(8, false, Integer.MIN_VALUE, Integer.MAX_VALUE);
                board.removeToken(i);

                if (value > score) {
                    score = value;
                    bestMove = i;
                }
            }
        }

        return bestMove;
    }

    public int minimax(int depth, boolean isMaximizing, int alpha, int beta) {
        if (depth == 0 || board.getIsGameFinished()) {
            return evaluateBoard();
        }

        if (isMaximizing) { //AI IS MAXIMIZING PLAYER
            int maxEval = Integer.MIN_VALUE;
            for (int col = 1; col < 8; col++) {
                if (board.placeToken(col, this)) {
                    int eval = minimax(depth - 1, false, alpha, beta);
                    board.removeToken(col);
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
            for (int col = 1; col < 8; col++) {
                if(board.placeToken(col, this.getOpponent())) {
                    int eval = minimax(depth - 1, true, alpha, beta);
                    board.removeToken(col);
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



    private static final int[][] posScore = {
            {3, 4, 5, 7, 5, 4, 3},
            {4, 6, 8, 10, 8, 6, 4},
            {5, 8, 11, 13, 11, 8, 5},
            {5, 8, 11, 13, 11, 8, 5},
            {4, 6, 8, 10, 8, 6, 4},
            {3, 4, 5, 7, 5, 4, 3}
    };

    private int evaluateBoard() {
        int evaluationScore = 0;

        evaluationScore += evalPosScore(this);
        evaluationScore -= evalPosScore(this.getOpponent());

        if (board.getIsGameFinished()) {
            evaluationScore += evalGameWon(this);
            evaluationScore -= evalGameWon(this.getOpponent());
        }

        return evaluationScore;
    }

    private int evalPosScore(Player player) {
        return player.getPlayersTokens().map(t -> posScore[t.row][t.col]).mapToInt(Integer::intValue).sum();
    }

    private int evalGameWon(Player player) {
        return board.getHasWon().getID() == player.getID() ? 1000 : 0;
    }

    //TODO evaluation properties
}
