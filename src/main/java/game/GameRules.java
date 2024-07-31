package game;

import player.ai.AIPlayer;
import player.HumanPlayer;
import player.Player;
import player.PlayerProperty;
import player.ai.genetic.Genome;

import java.util.Objects;

public class GameRules {

    private Board board;

    private Player player1;
    private Player player2;



    public GameRules(Genome genome1, Genome genome2) {
        this.board = new Board();

        switch (GameProperties.GAMEMODE) {
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
            }
        }
    }



    public Player run() {
        if (GameProperties.GAMEMODE == 3) {
            return runTraining();
        } else {
            return runGame();
        }
    }

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



    public void newGame() {
        resetGame();
        switchPlayers();
    }

    private void resetGame() {
        this.board.resetBoard();
        this.player1.clearTokens();
        this.player2.clearTokens();
    }

    private void switchPlayers() {
        Player tempPlayer = player1;
        player1 = player2;
        player2 = tempPlayer;
    }
}