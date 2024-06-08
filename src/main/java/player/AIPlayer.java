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
                int value = minimax(8, true, Integer.MIN_VALUE, Integer.MAX_VALUE);
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



    public int evaluateBoard() {
        int evaluationScore = 0;
        //TODO
        return evaluationScore;
    }

    //TODO evaluation properties
}
