package game;

import player.ai.AIPlayer;
import player.HumanPlayer;
import player.Player;
import player.PlayerProperty;
import player.ai.genetic.Genome;

import java.util.Objects;

/**
 * Encapsulates the game setup and turn execution logic according to configured game properties.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Create and initialize players (human or AI) according to {@link GameProperties}.</li>
 *   <li>Run a single game or a training run depending on the selected mode.</li>
 *   <li>Manage turn-taking, printing, and final result reporting.</li>
 *   <li>Provide utilities to start a new game, reset state, and swap player roles.</li>
 * </ul>
 * <p>
 * This class holds a {@link Board} instance and two {@link Player} references which represent
 * the two participants in a match. Construction requires two {@link Genome} instances used by
 * AI players; these may be ignored for modes that do not use AI.
 */
public class GameRules {

    private final Board board;

    private Player player1;
    private Player player2;



    /**
     * Constructs game rules and instantiates players according to {@link GameProperties#GAME_MODE}.
     * <p>
     * The constructor accepts two {@link Genome} instances that are supplied to AI players when applicable.
     * Different game modes:
     * <ul>
     *   <li>0 — Multiplayer: two {@link HumanPlayer} instances are created.</li>
     *   <li>1, 5 — Single player: one {@link HumanPlayer} and one {@link AIPlayer} are created; {@code genome1} used for AI.</li>
     *   <li>2, 4 — AI-only: two {@link AIPlayer} instances are created; both genomes are printed to stdout.</li>
     *   <li>3 — Training: two {@link AIPlayer} instances are created for headless training runs.</li>
     * </ul>
     *
     * @param genome1 primary genome passed to the first AI participant (if used)
     * @param genome2 secondary genome passed to the second AI participant (if used)
     */
    public GameRules(Genome genome1, Genome genome2) {
        this.board = new Board();

        switch (GameProperties.GAME_MODE) {
            case 0 : { //MULTIPLAYER
                if (GameProperties.PLAYER1_STARTS) {
                    player1 = new HumanPlayer(PlayerProperty.PLAYER1, board, true);
                    player2 = new HumanPlayer(PlayerProperty.PLAYER2, board, player1, false);
                    player1.setOpponent(player1);
                } else {
                    player2 = new HumanPlayer(PlayerProperty.PLAYER1, board, false);
                    player1 = new HumanPlayer(PlayerProperty.PLAYER2, board, player2, true);
                    player2.setOpponent(player1);
                }
                break;
            }
            case 1, 5 : { //SINGLE PLAYER
                if (GameProperties.PLAYER1_STARTS) {
                    player1 = new HumanPlayer(PlayerProperty.PLAYER1, board, true);
                    player2 = new AIPlayer(PlayerProperty.PLAYER2, board, player1, false, genome1);
                    player1.setOpponent(player2);
                } else {
                    player2 = new HumanPlayer(PlayerProperty.PLAYER1, board, false);
                    player1 = new AIPlayer(PlayerProperty.PLAYER2, board, player2, true, genome1);
                    player2.setOpponent(player1);
                }
                genome1.printGenome();
                break;
            }
            case 2, 4 : { //AIONLY
                if (GameProperties.PLAYER1_STARTS) {
                    player1 = new AIPlayer(PlayerProperty.PLAYER1, board, true, genome1);
                    player2 = new AIPlayer(PlayerProperty.PLAYER2, board, player1, false, genome2);
                    player1.setOpponent(player2);
                } else {
                    player2 = new AIPlayer(PlayerProperty.PLAYER1, board, false, genome2);
                    player1 = new AIPlayer(PlayerProperty.PLAYER2, board, player2, true, genome1);
                    player2.setOpponent(player1);
                }

                System.out.printf("Player %s uses genome:%n", player1.getToken());
                AIPlayer aiPlayer1 = (AIPlayer) player1;
                aiPlayer1.getGenome().printGenome();

                System.out.printf("Player %s uses genome:%n", player2.getToken());
                AIPlayer aiPlayer2 = (AIPlayer) player2;
                aiPlayer2.getGenome().printGenome();
                break;
            }
            case 3 : { //TRAIN
                player1 = new AIPlayer(PlayerProperty.PLAYER1, board, true, genome1);
                player2 = new AIPlayer(PlayerProperty.PLAYER2, board, player1, false, genome2);
                player1.setOpponent(player2);
                break;
            }
        }
    }



    /**
     * Entry point to run either a training run or a normal game based on {@link GameProperties#GAME_MODE}.
     *
     * @return the winning {@link Player} if a player won, otherwise {@code null} when there is no winner
     */
    public Player run() {
        if (GameProperties.GAME_MODE == 3) {
            return runTraining();
        } else {
            return runGame();
        }
    }

    /**
     * Executes a headless training loop where two AI players alternate moves until the board is finished.
     * <p>
     * Turn order is controlled by a simple counter and players are informed whether they start via
     * {@link Player#setBeginningPlayer(boolean)}. If a player returns {@code false} from {@link Player#makeMove()},
     * the counter is decremented so the same player's turn is retried.
     *
     * @return the player who won the training game, or {@code null} if there was no winner
     */
    private Player runTraining() {
        int counter = 1;
        player1.setBeginningPlayer(true);
        player2.setBeginningPlayer(false);

        while (!board.getIsGameFinished()) {

            if (counter % 2 == 0) {
                if (!player2.makeMove()) {
                    counter--;
                }
            } else {
                if (!player1.makeMove()) {
                    counter--;
                }
            }
            counter++;
        }

        return board.getHasWon();
    }

    /**
     * Runs an interactive (console) game loop that prints the board and prompts each player to make a move.
     * <p>
     * Turn number and current board state are printed to standard output each iteration. If a player
     * performs an invalid move (indicated by {@link Player#makeMove()} returning {@code false}), an error
     * message is printed to standard error and the player is allowed to retry.
     *
     * @return the winning {@link Player} or {@code null} when there is no winner
     */
    private Player runGame() {
        int counter = 1;
        player1.setBeginningPlayer(true);
        player2.setBeginningPlayer(false);

        while (!board.getIsGameFinished()) {

            System.out.printf("Turn: %d%n", counter);
            board.printBoard();

            if (counter % 2 == 0) {
                System.out.printf("%s's turn%n%n", player2.getToken());
                if (!player2.makeMove()) {
                    System.err.printf("Invalid move - try again%n%n");
                    counter--;
                }
            } else {
                System.out.printf("%s's turn%n%n", player1.getToken());
                if (!player1.makeMove()) {
                    System.err.printf("Invalid move - try again%n%n");
                    counter--;
                }
            }
            counter++;
        }

        board.printBoard();
        Player winningPlayer;
        System.out.printf("Game over - %s has won!%n%n", Objects.nonNull(winningPlayer = board.getHasWon()) ? board.getHasWon() : "no one");
        this.resetGame();
        switchPlayers();
        return winningPlayer;
    }



    /**
     * Starts a new game by resetting the board and swapping player roles.
     * <p>
     * Swapping players is useful for alternating which entity starts first in consecutive matches.
     */
    public void newGame() {
        resetGame();
        switchPlayers();
    }

    /**
     * Resets internal game state and clears tokens from both players and the board.
     * <p>
     * This method invokes {@link Board#resetBoard()} and {@link Player#clearTokens()} on both players.
     * It is expected that {@code player1} and {@code player2} are non-null when called.
     */
    private void resetGame() {
        this.board.resetBoard();
        this.player1.clearTokens();
        this.player2.clearTokens();
    }

    /**
     * Swaps the player references, effectively toggling which player is in the first turn position.
     * <p>
     * Example: after {@code switchPlayers()}, the previous {@code player1} becomes {@code player2} and vice versa.
     */
    private void switchPlayers() {
        Player tempPlayer = player1;
        player1 = player2;
        player2 = tempPlayer;
    }
}