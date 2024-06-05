package game;

import datatypes.Pair;
import player.Player;

import java.util.HashMap;
import java.util.Objects;

import static game.GameProperties.COLS;
import static game.GameProperties.ROWS;

public class Board {
    private static final Token BLANKSPACE = new Token(null, new String(Character.toChars(0x000026AB)), -1, -1);

    private final Token[][] board = new Token[ROWS][COLS];
    private final HashMap<Pair<Integer, Integer>, Token> winningConditions = new HashMap<>();
    private final int[] currentHeightMap = new int[COLS];

    private int heightSum = 0;
    private boolean isGameFinished = false;
    private Player hasWon = null;

    private final int winSectorRowsMin;
    private final int winSectorRowsMax;
    private final int winSectorColsMin;
    private final int winSectorColsMax;



    public Board() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                board[i][j] = BLANKSPACE;
            }
        }

        boolean sizeRows = ROWS > 6;
        winSectorRowsMin = sizeRows ? 3 : 2;
        winSectorRowsMax = sizeRows ? ROWS - 4 : 2;
        boolean sizeCols = COLS > 6;
        winSectorColsMin = sizeCols ? 3 : 2;
        winSectorColsMax = sizeCols ? COLS - 4 : 2;
    }

    public Token[][] getBoard() {
        return board;
    }

    public boolean placeToken(int col, Player owner) {
        if (col < 1 || col > COLS || currentHeightMap[col - 1] > ROWS - 1) {
            return false;
        }

        int innerCol = col - 1;
        int innerRow = currentHeightMap[innerCol];

        Token placedToken = new Token(owner, owner.getToken(), innerRow, innerCol);
        board[innerRow][innerCol] = placedToken;
        if ((innerRow >= winSectorRowsMin && innerRow <= winSectorRowsMax) || (innerCol >= winSectorColsMin && innerCol <= winSectorColsMax)) {
            winningConditions.put(new Pair<>(innerRow, innerCol), placedToken);
        }

        currentHeightMap[innerCol]++;
        heightSum++;

        checkGameIsFinished();
        return true;
    }

    public void removeToken(int col) {
        int innerCol = col - 1;
        int innerRow = currentHeightMap[innerCol] - 1;
        board[innerRow][innerCol] = BLANKSPACE;
        winningConditions.remove(new Pair<>(innerRow, innerCol));

        currentHeightMap[innerCol]--;
        heightSum--;
        hasWon = null;
    }

    private void checkGameIsFinished(){
        isGameFinished = isFull() || winningConditions.values().stream().anyMatch(this::checkWinningConditions);
    }

    public boolean getIsGameFinished() {
        return this.isGameFinished;
    }

    public Player getHasWon() {
        return hasWon;
    }

    private boolean checkWinningConditions(Token token) {
        int[][] directions = {
                {0, 1},     //horizontal
                {1, 0},     //vertical
                {1, 1},     //diagonal up
                {1, -1},    //diagonal down
        };

        for (int[] direction : directions) {
            int dir1 = checkInDirection(token, direction[0], direction[1]);
            int dir2 = checkInDirection(token, -direction[0], -direction[1]);
            if (dir1 + dir2 - 1 >= 4) {
                hasWon = token.owner;
                return true;
            }
        }
        return false;
    }

    private int checkInDirection(Token token, int deltaRow, int deltaCol) {
        int row = token.row;
        int col = token.col;
        int playerID = token.owner.getID();
        int countLine = 0;

        while (row < ROWS && row >= 0 && col < COLS && col >= 0 && Objects.nonNull(board[row][col].owner) && board[row][col].owner.getID() == playerID) {
            countLine++;

            row += deltaRow;
            col += deltaCol;
        }
        return countLine;
    }



    public void printBoard() {
        //TODO dynamisieren, cleanen
        for (int i = ROWS - 1; i >= 0; i--) {
            System.out.printf("----------------------------------------%n%d ", i + 1);
            for (int j = 0; j < COLS; j++) {
                System.out.print("| " + board[i][j] + " ");
            }
            System.out.println("|");
        }
        System.out.println("----------------------------------------");
        System.out.println("  |  1 |  2 |  3  |  4 |  5 |  6  |  7");
        System.out.println();
    }

    private boolean isFull() {
        return heightSum == ROWS * COLS;
    }



    public static class Token {
        public Player owner;
        public String tokenRepresentation;
        int row;
        int col;

        public Token(Player owner, String tokenRepresentation, int row, int col) {
            this.owner = owner;
            this.tokenRepresentation = tokenRepresentation;
            this.row = row;
            this.col = col;
        }

        @Override
        public String toString(){
            return tokenRepresentation;
        }
    }
}
