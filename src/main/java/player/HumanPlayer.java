package player;

import game.Board;

import java.util.Scanner;

/**
 * Represents a human-controlled player.
 *
 * This implementation reads the next move from standard input (console)
 * and tries to place a token on the board accordingly.
 *
 * Note: This class assumes that user input is provided in a correct format
 * and that the Board implementation validates whether the chosen column is valid.
 */
public class HumanPlayer extends Player {

    private final Scanner PROMPT = new Scanner(System.in);

    /**
     * Constructs a human player without an explicitly defined opponent.
     *
     * @param playerProperty  The properties of this player (e.g. ID, color, symbol).
     * @param board           The game board instance.
     * @param beginningPlayer True if this player starts the game.
     */
    public HumanPlayer(PlayerProperty playerProperty, Board board, boolean beginningPlayer) {
        super(playerProperty, board, beginningPlayer);
    }

    /**
     * Constructs a human player with a predefined opponent.
     *
     * @param playerProperty  The properties of this player (e.g. ID, color, symbol).
     * @param board           The game board instance.
     * @param opponent        The opponent player.
     * @param beginningPlayer True if this player starts the game.
     */
    public HumanPlayer(PlayerProperty playerProperty, Board board, Player opponent, boolean beginningPlayer) {
        super(playerProperty, board, opponent, beginningPlayer);
    }

    /**
     * Prompts the user for input, parses the next line as an integer
     * and attempts to place a token on the board.
     *
     * @return true if the token was successfully placed, false if the input was invalid
     *         or the board rejected the move (e.g. column full, invalid column).
     */
    @Override
    public boolean makeMove() {
        int move;
        try {
            move = Integer.parseInt(PROMPT.nextLine().trim());
        } catch (NumberFormatException e) {
            return false;
        }
        return board.placeToken(move, this);
    }
}
