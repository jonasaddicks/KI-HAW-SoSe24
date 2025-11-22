package game;

import player.Player;

import java.util.Arrays;
import java.util.Objects;
import java.util.Stack;

import static game.GameProperties.COLS;
import static game.GameProperties.ROWS;

/**
 * Represents the game board for a two-dimensional, column-based token game.
 * <p>
 * The board is internally represented as a ROWS x COLS matrix of {@link Token} objects.
 * This class provides methods to place and remove tokens, inspect win conditions,
 * compute threat heuristics for a player, and print the current board state.
 * <p>
 * Threading and concurrency are not considered — this class is not synchronized.
 */
public class Board {
    private static final Token BLANKSPACE = new Token(null, new String(Character.toChars(0x000026AB)), -1, -1);

    private static final String printSeparator;
    private static final String printBottom;

    private final Token[][] board = new Token[ROWS][COLS];
    private Stack<Token> winningConditions = new Stack<>();
    private int[] currentHeightMap = new int[COLS];

    private int heightSum = 0;
    private boolean isGameFinished = false;
    private Player hasWon = null;

    /**
     * Direction vectors used for line inspection.
     * Each entry is a pair {deltaRow, deltaCol}:
     * <ul>
     *   <li>{0, 1}   — horizontal (right)</li>
     *   <li>{1, 0}   — vertical (up)</li>
     *   <li>{1, 1}   — diagonal up-right</li>
     *   <li>{1, -1}  — diagonal up-left</li>
     * </ul>
     */
    private static final int[][] directions = {
            {0, 1},     //horizontal
            {1, 0},     //vertical
            {1, 1},     //diagonal up
            {1, -1},    //diagonal down
    };

    private static final int winSectorRowsMin;
    private static final int winSectorRowsMax;
    private static final int winSectorColsMin;
    private static final int winSectorColsMax;



    // Static initializer builds the printable separators and determines the win-sector bounds based on board dimensions.
    static {
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



    /**
     * Constructs a new Board and fills it with {@link #BLANKSPACE} tokens.
     * All height counters and internal state are initialized to represent an empty board.
     */
    public Board() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                board[i][j] = BLANKSPACE;
            }
        }
    }

    /**
     * Returns the internal token matrix representing the board state.
     * <p>
     * Warning: This method exposes the internal array directly.
     *
     * @return the token matrix (rows x cols)
     */
    public Token[][] getBoard() {
        return board;
    }

    /**
     * Resets the board to an initial empty state. Clears tokens, resets
     * column heights, stack of winning-condition tokens, and game state flags.
     */
    public void resetBoard(){
        clearBoard();
        this.winningConditions = new Stack<>();
        this.currentHeightMap = new int[COLS];
        this.heightSum = 0;
        this.isGameFinished = false;
        this.hasWon = null;
    }

    /**
     * Helper to fill the entire board with {@link #BLANKSPACE}.
     * Called during initialization and reset operations.
     */
    private void clearBoard() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                board[i][j] = BLANKSPACE;
            }
        }
    }


    /**
     * Attempts to place a token into the specified 1-based column on behalf of the given player.
     * <p>
     * The token will land on top of the current stack in that column and the player's internal
     * token list will be updated via {@code owner.addToken(placedToken)}.
     * If the placed token falls within the precomputed win sector, it is appended to {@link #winningConditions}
     * for later win evaluation. The method updates internal counters and triggers a win/full check.
     *
     * @param col   1-based column index where the token should be placed (valid range: 1 .. COLS)
     * @param owner the player placing the token
     * @return {@code true} if the token was legally placed; {@code false} if the column index is invalid
     *         or the column is already full
     */
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

    /**
     * Removes the topmost token from the specified 1-based column.
     * <p>
     * The method assumes the column contains at least one token; callers should ensure
     * the operation is legal. The token's owner will be instructed to remove the last token
     * from their internal storage, and board state is updated accordingly.
     *
     * @param col 1-based column index from which to remove the top token
     */
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

    /**
     * Recomputes the {@link #isGameFinished} flag by checking whether any token in
     * {@link #winningConditions} forms a winning line, or the board is full.
     */
    private void checkGameIsFinished(){
        isGameFinished = winningConditions.stream().anyMatch(this::checkWinningConditions) || isFull();
    }

    /**
     * Returns whether the game has finished.
     *
     * @return {@code true} if the game ended by a win or a full board; {@code false} otherwise
     */
    public boolean getIsGameFinished() {
        return this.isGameFinished;
    }

    /**
     * Returns the player who has won the game, or {@code null} if no winner exists.
     *
     * @return winning player or {@code null}
     */

    public Player getHasWon() {
        return hasWon;
    }



    /**
     * Returns the stack of tokens that were placed in the "winning sector".
     * <p>
     * Note: This exposes the internal stack object which may be mutated by callers.
     *
     * @return the stack of candidate tokens for win detection
     */
    public Stack<Token> getWinningConditions() {
        return winningConditions;
    }

    /**
     * Checks if the given token forms a winning line. The method inspects each
     * direction vector in {@link #directions} by counting contiguous tokens for
     * the token's owner in both the forward and backward directions. If a contiguous
     * run of 4 or more tokens is found, {@link #hasWon} is set to the owning player
     * and the method returns {@code true}.
     *
     * @param token the token to inspect as a potential part of a winning line
     * @return {@code true} if the token participates in a winning run; {@code false} otherwise
     */
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

    /**
     * Counts contiguous tokens belonging to the same player as {@code token} starting
     * at {@code token} and stepping by {@code deltaRow}/{@code deltaCol} until a boundary
     * or a non-matching token is encountered.
     *
     * @param token    the starting token
     * @param deltaRow row step per iteration
     * @param deltaCol column step per iteration
     * @return the number of contiguous matching tokens in the given direction
     */
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

    /**
     * Computes "major" threat count for the supplied player across the board.
     * Major threats are aggregated by scanning every token on the board that belongs
     * to the player and summing the result of {@link #getThreats(Token, int)} with majorMinor=0.
     *
     * @param player the player for whom to compute major threats
     * @return the total number of major threats found
     */
    public int getMajorThreats(Player player) {
        return Arrays.stream(board).flatMap(Arrays::stream)
                .filter(t -> Objects.nonNull(t.owner) && t.owner.getID() == player.getID())
                .mapToInt(t -> this.getThreats(t, 0))
                .sum();
    }

    /**
     * Computes "minor" threat count for the supplied player across the board.
     * Minor threats are aggregated by scanning every token on the board that belongs
     * to the player and summing the result of {@link #getThreats(Token, int)} with majorMinor=1.
     *
     * @param player the player for whom to compute minor threats
     * @return the total number of minor threats found
     */
    public int getMinorThreats(Player player) {
        return Arrays.stream(board).flatMap(Arrays::stream)
                .filter(t -> Objects.nonNull(t.owner) &&  t.owner.getID() == player.getID())
                .mapToInt(t -> this.getThreats(t, 1))
                .sum();
    }

    /**
     * Aggregates directional threat counts for a given token by invoking
     * {@link #checkInDirectionThreats(Token, int, int, int)} for each direction
     * and its inverse.
     *
     * @param token      the token from which to evaluate threats
     * @param majorMinor 0 to compute major-threat patterns, 1 to compute minor-threat patterns
     * @return aggregated threat count for the token
     */
    private int getThreats(Token token, int majorMinor) {
        int dir1 = 0, dir2 = 0;
        for (int[] direction : directions) {
            dir1 += checkInDirectionThreats(token, direction[0], direction[1], majorMinor);
            dir2 += checkInDirectionThreats(token, -direction[0], -direction[1], majorMinor);
        }
        return dir1 + dir2;
    }

    /**
     * Examines up to four consecutive positions starting at {@code token} and stepping by
     * {@code deltaRow}/{@code deltaCol}. The method counts how many positions are empty
     * (null owner) and how many belong to the same player as {@code token}, then returns
     * an indicator (0 or 1) based on the selected major/minor threat pattern:
     * <ul>
     *     <li>major (majorMinor == 0): exactly 3 player tokens and 1 empty → returns 1</li>
     *     <li>minor (majorMinor == 1): exactly 2 player tokens and 2 empty → returns 1</li>
     *     <li>otherwise: returns 0</li>
     * </ul>
     *
     * @param token      the reference token
     * @param deltaRow   row step per iteration
     * @param deltaCol   column step per iteration
     * @param majorMinor 0 for major-threat pattern, 1 for minor-threat pattern
     * @return 0 or 1 indicating whether the inspected window matches the threat pattern
     */
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



    /**
     * Prints the current board to standard output using the precomputed separators
     * and bottom labels. The printed representation shows row numbers at the start
     * of each printed line and token text via {@link Token#toString()}.
     */
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

    /**
     * Determines whether the board is full (no empty cells remain).
     *
     * @return {@code true} if the board contains ROWS * COLS tokens; {@code false} otherwise
     */
    private boolean isFull() {
        return heightSum == ROWS * COLS;
    }



    /**
     * Immutable holder for a token placed on the board.
     * <p>
     * Fields are package-private for direct access by {@link Board} logic and printing.
     * The Token class exposes a {@link #toString()} method for display.
     */
    public static class Token {
        public Player owner;
        public String tokenRepresentation;
        public int row;
        public int col;

        /**
         * Constructs a token instance.
         *
         * @param owner               the owning player (may be {@code null} for blank)
         * @param tokenRepresentation printable representation of the token
         * @param row                 0-based row index
         * @param col                 0-based column index
         */
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
