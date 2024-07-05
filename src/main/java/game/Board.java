package game;

import player.Player;

import java.util.Arrays;
import java.util.Objects;
import java.util.Stack;

import static game.GameProperties.COLS;
import static game.GameProperties.ROWS;

public class Board {
    private static final Token BLANKSPACE = new Token(null, new String(Character.toChars(0x000026AB)), -1, -1);

    private final String printSeparator;
    private final String printBottom;

    private final Token[][] board = new Token[ROWS][COLS];
    private Stack<Token> winningConditions = new Stack<>();
    private int[] currentHeightMap = new int[COLS];

    private int heightSum = 0;
    private boolean isGameFinished = false;
    private Player hasWon = null;

    private static final int[][] directions = {
            {0, 1},     //horizontal
            {1, 0},     //vertical
            {1, 1},     //diagonal up
            {1, -1},    //diagonal down
    };

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

        StringBuilder sb = new StringBuilder();
        sb.append("----");
        for(int i = 0; i < COLS; i++) {
            if (i % 5 == 0) {
                sb.append("------");
            } else {
                sb.append("-----");
            }
        }
        sb.append(String.format("%n"));
        printSeparator = sb.toString();
        sb.delete(0, sb.length());

        sb.append("  |");
        for(int i = 1; i <= COLS; i++) {
            if (i >= 10) {
                if (i % 5 == 0) {
                    sb.append(String.format("  %d |", i));
                } else {
                    sb.append(String.format(" %d |", i));
                }
            } else {
                if (i % 3 == 0) {
                    sb.append(String.format("  %d  |", i));
                } else {
                    sb.append(String.format("  %d |", i));
                }
            }
        }
        sb.append(String.format("%n"));
        printBottom = sb.toString();

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

    public void resetBoard(){
        clearBoard();
        this.winningConditions = new Stack<>();
        this.currentHeightMap = new int[COLS];
        this.heightSum = 0;
        this.isGameFinished = false;
        this.hasWon = null;
    }

    private void clearBoard() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                board[i][j] = BLANKSPACE;
            }
        }
    }



    public boolean placeToken(int col, Player owner) {
        if (col < 1 || col > COLS || currentHeightMap[col - 1] > ROWS - 1) {
            return false;
        }

        int innerCol = col - 1;
        int innerRow = currentHeightMap[innerCol];

        Token placedToken = new Token(owner, owner.getToken(), innerRow, innerCol);
        owner.addToken(placedToken);

        board[innerRow][innerCol] = placedToken;
        if ((innerRow >= winSectorRowsMin && innerRow <= winSectorRowsMax) || (innerCol >= winSectorColsMin && innerCol <= winSectorColsMax)) {
            winningConditions.add(placedToken);
        }

        currentHeightMap[innerCol]++;
        heightSum++;

        checkGameIsFinished();
        return true;
    }

    public void removeToken(int col) {
        int innerCol = col - 1;
        int innerRow = currentHeightMap[innerCol] - 1;

        Token token = board[innerRow][innerCol];
        token.owner.removeLastToken();

        board[innerRow][innerCol] = BLANKSPACE;
        if ((innerRow >= winSectorRowsMin && innerRow <= winSectorRowsMax) || (innerCol >= winSectorColsMin && innerCol <= winSectorColsMax)) {
            winningConditions.removeLast();
        }

        currentHeightMap[innerCol]--;
        heightSum--;
        hasWon = null;
    }

    private void checkGameIsFinished(){
        isGameFinished = winningConditions.stream().anyMatch(this::checkWinningConditions) || isFull();
    }

    public boolean getIsGameFinished() {
        return this.isGameFinished;
    }

    public Player getHasWon() {
        return hasWon;
    }



    public Stack<Token> getWinningConditions() {
        return winningConditions;
    }

    private boolean checkWinningConditions(Token token) {
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

    public int getMajorThreats(Player player) {
        return Arrays.stream(board).flatMap(Arrays::stream)
                .filter(t -> Objects.nonNull(t.owner) && t.owner.getID() == player.getID())
                .mapToInt(t -> this.getThreats(t, 0))
                .sum();
    }

    public int getMinorThreats(Player player) {
        return Arrays.stream(board).flatMap(Arrays::stream)
                .filter(t -> Objects.nonNull(t.owner) &&  t.owner.getID() == player.getID())
                .mapToInt(t -> this.getThreats(t, 1))
                .sum();
    }

    private int getThreats(Token token, int majorMinor) {
        int dir1 = 0, dir2 = 0;
        for (int[] direction : directions) {
            dir1 += checkInDirectionThreats(token, direction[0], direction[1], majorMinor);
            dir2 += checkInDirectionThreats(token, -direction[0], -direction[1], majorMinor);
        }
        return dir1 + dir2;
    }

    private int checkInDirectionThreats(Token token, int deltaRow, int deltaCol, int majorMinor) {
        int row = token.row;
        int col = token.col;
        int playerID = token.owner.getID();
        int nullToken = 0, playerToken = 0;

        for (int i = 0; row < ROWS && row >= 0 && col < COLS && col >= 0 && i < 4; i++) {
            if (Objects.isNull(board[row][col].owner)) {
                nullToken++;
            } else {
                if (board[row][col].owner.getID() == playerID) {
                    playerToken++;
                }
            }
            row += deltaRow;
            col += deltaCol;
        }

        return switch (majorMinor) {
            case 0 -> (nullToken == 1 && playerToken == 3) ? 1 : 0;
            case 1 -> (nullToken == 2 && playerToken == 2) ? 1 : 0;
            default -> 0;
        };
    }



    public void printBoard() {
        for (int i = ROWS - 1; i >= 0; i--) {
            System.out.print(printSeparator);
            System.out.printf("%d ", i + 1);
            for (int j = 0; j < COLS; j++) {
                System.out.print("| " + board[i][j] + " ");
            }
            System.out.printf("|%n");
        }
        System.out.printf(printSeparator);
        System.out.print(printBottom);
    }

    private boolean isFull() {
        return heightSum == ROWS * COLS;
    }



    public static class Token {
        public Player owner;
        public String tokenRepresentation;
        public int row;
        public int col;

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
