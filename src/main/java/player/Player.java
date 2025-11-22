package player;

import game.Board;

import java.util.Stack;
import java.util.stream.Stream;

/**
 * Abstract base class for a game participant.
 * <p>
 * A {@code Player} encapsulates identity (token, id), turn-related flags, and a history
 * of placed tokens. Concrete subclasses must implement move decision logic by overriding
 * {@link #makeMove()} (e.g. {@link player.HumanPlayer} and {@link player.ai.AIPlayer}).
 *
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Expose immutable player identity: token string and numeric id.</li>
 *   <li>Track whether the player starts the match (beginningPlayer).</li>
 *   <li>Maintain a stack of tokens placed by this player for quick undo/inspection.</li>
 *   <li>Provide accessors for opponent reference and game board.</li>
 * </ul>
 *
 * <p>
 * Note: This class uses {@link java.util.Stack} for token history and is not synchronized;
 * if Player instances are accessed concurrently additional synchronization is required.
 * </p>
 */
public abstract class Player {

    private final String token;
    private final int id;
    private boolean beginningPlayer;
    private int gamesWon;

    protected Board board;
    private Player opponent;
    private Stack<Board.Token> tokensPlayed;

    /**
     * Construct a Player without an explicit opponent reference.
     *
     * @param playerProperty  enum-like property that provides token and id
     * @param board           shared game board instance
     * @param beginningPlayer whether this player begins the match
     */
    public Player(PlayerProperty playerProperty, Board board, boolean beginningPlayer) {
        this(playerProperty, board, null, beginningPlayer);
    }

    /**
     * Construct a Player with an opponent reference.
     *
     * @param playerProperty  enum-like property that provides token and id
     * @param board           shared game board instance
     * @param opponent        the opposing player (may be {@code null} during initial wiring)
     * @param beginningPlayer whether this player begins the match
     */
    public Player(PlayerProperty playerProperty, Board board, Player opponent, boolean beginningPlayer) {
        this.token = playerProperty.getToken(playerProperty);
        this.id = playerProperty.getID(playerProperty);
        this.beginningPlayer = beginningPlayer;
        this.gamesWon = 0;

        this.board = board;
        this.opponent = opponent;
        tokensPlayed = new Stack<>();
    }



    /**
     * Returns the printable token string for this player.
     *
     * @return token string (immutable)
     */
    public String getToken() {
        return token;
    }

    /**
     * Returns the numeric identifier of this player.
     *
     * @return player id
     */
    public int getID() {
        return this.id;
    }

    /**
     * Indicates whether this player is set to begin the match.
     *
     * @return true if beginning player, false otherwise
     */
    public boolean isBeginningPlayer() {
        return this.beginningPlayer;
    }

    /**
     * Set the flag indicating whether this player should begin the next match.
     *
     * @param beginningPlayer true to mark as beginning player, false otherwise
     */
    public void setBeginningPlayer(boolean beginningPlayer) {
        this.beginningPlayer = beginningPlayer;
    }

    /**
     * Increment the internal counter recording games won by this player.
     * Call this method when the player has been determined as the winner of a match.
     */
    public void gameWon() {
        this.gamesWon++;
    }

    /**
     * Returns the number of games this player has won.
     *
     * @return games won counter
     */
    public int getGamesWon() {
        return this.gamesWon;
    }

    /**
     * Returns the opponent player reference.
     *
     * @return opponent player or {@code null} if not assigned
     */
    public Player getOpponent() {
        return this.opponent;
    }

    /**
     * Assigns or updates the opponent reference for this player.
     *
     * @param opponent opponent player instance
     */
    public void setOpponent(Player opponent) {
        this.opponent = opponent;
    }

    /**
     * Removes the most recently recorded token from this player's token history.
     * This method delegates to {@link Stack#removeElementAt(int)} indirectly via {@link Stack#remove}.
     * It is typically called after {@link Board#removeToken(int)} to keep history consistent.
     */
    public void removeLastToken() {
        this.tokensPlayed.removeLast();
    }

    /**
     * Record a token as having been placed by this player.
     * Typical call site: immediately after a successful {@link Board#placeToken(int, Player)}.
     *
     * @param token token instance placed on the board
     */
    public void addToken(Board.Token token) {
        this.tokensPlayed.push(token);
    }

    /**
     * Provides a stream of tokens placed by this player in insertion order (bottom to top of the stack).
     * Consumers can use this stream for heuristic evaluation or printing.
     *
     * @return stream of {@link Board.Token} instances owned by this player
     */
    public Stream<Board.Token> getPlayersTokens() {
        return this.tokensPlayed.stream();
    }

    /**
     * Clears recorded token history for this player.
     * Typically used when resetting the board for a new match.
     */
    public void clearTokens() {
        this.tokensPlayed.clear();
    }



    /**
     * Concrete subclasses must implement move logic. Implementations should attempt to
     * place a token on the board and return {@code true} on success or {@code false} on failure.
     *
     * @return {@code true} if a move was successfully executed; {@code false} otherwise
     */
    public abstract boolean makeMove();

    /**
     * Returns a string representation used for logging and display.
     *
     * @return token string
     */
    @Override
    public String toString() {
        return token;
    }
}
