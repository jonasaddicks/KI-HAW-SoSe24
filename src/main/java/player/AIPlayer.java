package player;

import game.Board;

import java.util.Random;

public class AIPlayer extends Player {

    public AIPlayer(PlayerProperty playerProperty, Board board) {
        super(playerProperty, board);
    }

    @Override
    public boolean makeMove() {
        //TODO
        Random rand = new Random();
        int move = rand.nextInt(8) + 1;
        return board.placeToken(move, this);
    }

    private int calculateMove() {
        int bestMove = -1;
        int score = Integer.MIN_VALUE;

        for (int i = 1; i < 8; i++) {
            if (board.placeToken(i, this)) {
                int value = minimax(board, 9, false, Integer.MIN_VALUE, Integer.MAX_VALUE);
                board.removeToken(i);

                if (value > score) {
                    score = value;

                    bestMove = i;
                }
            }
        }

        return bestMove;
    }

    public int minimax(Board board, int depth, boolean isMaximizing, int alpha, int beta) {
        if (depth == 0 || board.getIsGameFinished()) {
            return evaluateBoard(board);
        }

            if (isMaximizing) {
                int maxEval = Integer.MIN_VALUE;
                for (int col = 1; col < 8; col++) {
                    if (board.placeToken(col, this)) {
                        // Annahme: AI ist der maximierende Spieler
                        int eval = minimax(board, depth - 1, false, alpha, beta);
                        board.removeToken(col);
                        maxEval = Math.max(maxEval, eval);
                        alpha = Math.max(alpha, maxEval);
                        if (beta <= alpha)
                            break;
                    }
                }
                return maxEval;
            } else {
                int minEval = Integer.MAX_VALUE;
                for (int col = 1; col < 8; col++) {
                    if(board.placeToken(col, this)) {
                        // Annahme: Spieler 2 ist der minimierende Spieler
                        int eval = minimax(board, depth - 1, true, alpha, beta);
                        board.removeToken(col);
                        minEval = Math.min(minEval, eval);
                        beta = Math.min(beta, minEval);
                        if (beta <= alpha)
                            break;
                    }
                }
                return minEval;
            }
    }



    public int evaluateBoard(Board board) {
        int evaluationScore = 0;
        //TODO
        return evaluationScore;
    }

    private int evalCentralPos(Board b, String token) {
        //TODO
        return 0;
    }

    private int countOpenFigures(Board b, String token) {
        //TODO
        return 0;
    }
}
