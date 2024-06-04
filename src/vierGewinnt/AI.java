package vierGewinnt;

import java.util.*;

public class AI extends Player{
    private final String BLANKSPACE = "\u0020";

    public int makeMove(Board board) {
        int bestMove = -1;
        int score = Integer.MIN_VALUE;

        for (int i = 1; i < 8; i++) {
            if (board.placeToken(i, this)) {
                if (board.getIsGameFinished()) {
                    board.removeToken(i);
                    return i;
                }
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
                    if(board.placeToken(col, getOponent())) {
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

    /**
     * Implementierung der Bewertungsfunktion
     * Diese Methode führt mehrere Prüfungen aus. evalCentralPos basiert auf der Überlegung, dass die Platzierung eines
     * Steins besser in der Mitte des Feldes ist. Von dort ergeben sich mehr Möglichkeiten eine Reihe zu bilden.
     * countOpenFigures hat einen größeren Einfluss auf den Score eines Zuges, denn sie analysiert, ob und wie gut
     * ein Zug die KI näher daran bringt, einen Stein zu einer schon existierenden Reihe hinzuzufügen oder eine Reihe
     * zu vervollständigen
     * @param board
     * @return der score, der die Bewertung eines Zuges zurückgibt
     */
    public int evaluateBoard(Board board) {
        int score = 0;
        score += evalCentralPos(board, this.getToken());
        score -= evalCentralPos(board, this.getOponent().getToken());
        score += countOpenFigures(board, this.getToken()) * 100;
        score -= countOpenFigures(board, this.getOponent().getToken()) * 100;
        if (Objects.nonNull(board.getHasWon()) && board.getHasWon().getID() == 2) return Integer.MAX_VALUE / 2;
        if (Objects.nonNull(board.getHasWon()) && board.getHasWon().getID() == 1) return Integer.MIN_VALUE / 2;
        return score;
    }

    private int evalCentralPos(Board b, String token) {
        int score = 0;
        Board.Token[][] board = b.getBoard();
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 6; j++) {
                if (board[j][i].equals(token)) {
                    switch (i) {
                        case 1 -> score += 1;
                        case 2, 4 -> score += 2;
                        case 3 -> score += 5;
                        case 5 -> score += 10;
                    }
                    if (i == 3) {
                        switch (j) {
                            case 1, 4 -> score += 5;
                            case 2, 3 -> score += 10;
                        }
                    } else if (i == 4 || i == 2) {
                        switch (j) {
                            case 1, 4 -> score += 1;
                            case 2, 3 -> score += 2;
                        }
                    }
                }
            }
        }
        return score;
    }

    private int countOpenFigures(Board b, String token) {
        int score = 0;
        int tokenCounter;
        Board.Token[][] board = b.getBoard();

        //count open fours
        //vertical
        for (int x = 2; x < 4; x++) {
            for (int i = 0; i < 7; i++) {
                tokenCounter = 0;
                for (int j = 5; j >= 0; j--) {
                    if (board[j][i].equals(token)) {
                        tokenCounter++;
                        if (j > 0 && tokenCounter == x && board[j - 1][i].equals(BLANKSPACE)) {
                            if (x == 3) {
                                score = Integer.MIN_VALUE / 2;
                            }
                            score += x;
                        }
                    } else {
                        tokenCounter = 0;
                    }
                }
            }

            //horizontal
            for (int i = 0; i < 6; i++) {
                tokenCounter = 0;
                for (int j = 0; j < 7; j++) {
                    if (token.equals(board[i][j])) {
                        tokenCounter++;
                        if (j < 6 && tokenCounter == x && board[i][j + 1].equals(BLANKSPACE)) {
                            if (j >= x && board[i][j - x].equals(BLANKSPACE)) {
                                if (x == 3 && ((i == 5) || !board[i + 1][j - x].equals(BLANKSPACE) && !board[i + 1][j + 1].equals(BLANKSPACE))) {
                                    score = Integer.MAX_VALUE / 2;
                                }
                                score = Integer.MAX_VALUE / 2;
                            }
                            score += x * 5;
                        }
                    } else {
                        tokenCounter = 0;
                    }
                }
            }

            //diagonal
            //diagonale Prüfung /
            for (int j = 0; j < 6; j++) {
                for (int i = 3; i < 7; i++) {
                    tokenCounter = 0;

                    int rowCounter = 0;
                    int colCounter = i;

                    while (rowCounter + j < 6 && colCounter >= 0) {
                        if (board[rowCounter][colCounter].equals(token)) {
                            tokenCounter++;
                            if (tokenCounter == x && rowCounter < 5 && colCounter > 0 && board[rowCounter + 1][colCounter - 1].equals(BLANKSPACE)) {
                                if (rowCounter > x && colCounter < 4 && board[rowCounter - x][colCounter + x].equals(BLANKSPACE)) {
                                    if ( x == 3 && (rowCounter == 4 || !board[rowCounter + 2][colCounter - 1].equals(BLANKSPACE)) && !board[rowCounter - x +1][colCounter + x].equals(BLANKSPACE)) {
                                        score = Integer.MAX_VALUE / 2;
                                    }
                                    score += x * 5;
                                }
                                score += x;
                            }
                        } else {
                            tokenCounter = 0;
                        }

                        rowCounter++;
                        colCounter--;
                    }
                }
            }
            // diagonale Prüfung \
            // todo funktioniert noch nicht
            for (int j = 0; j < 6; j++) {
                for (int i = 3; i < 7; i++) {
                    tokenCounter = 0;

                    int rowCounter = 5;
                    int colCounter = i;
                    while (rowCounter >= 0 && colCounter >= 0) {
                        if (board[rowCounter][colCounter].equals(token)) {
                            tokenCounter++;
                            if (tokenCounter == x && rowCounter > 0 && colCounter > 0 && board[rowCounter - 1][colCounter - 1].equals(BLANKSPACE)) {
                                if (rowCounter < 3 && colCounter < 4 && board[rowCounter + x][colCounter + x].equals(BLANKSPACE)) {
                                    if (x == 3 && rowCounter < 2  && (rowCounter == 3 || !board[rowCounter + x + 1][colCounter + x].equals(BLANKSPACE)) && (!board[rowCounter][colCounter - 1].equals(BLANKSPACE))) {
                                        score = Integer.MAX_VALUE / 2;
                                    }
                                    score += x * 5;
                                }
                                score += x;
                            }
                        }  else {
                            tokenCounter = 0;
                        }
                        rowCounter--;
                        colCounter--;
                    }
                }
            }

        }
        return score;
    }
}
