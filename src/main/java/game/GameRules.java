package game;

import player.AIPlayer;
import player.HumanPlayer;
import player.Player;
import player.PlayerProperty;

import java.util.Objects;

public class GameRules {

    private static GameRules INSTANCE;

    private Board board;
    private Player player1;
    private Player player2;



    private GameRules(){
        this.board = new Board();
        if (GameProperties.PLAYER1_STARTS) {
            player1 = new HumanPlayer(PlayerProperty.PLAYER1, board);
            player2 = GameProperties.SINGLEPLAYER ? new AIPlayer(PlayerProperty.PLAYER2, board) : new HumanPlayer(PlayerProperty.PLAYER2, board);
        } else {
            player2 = new HumanPlayer(PlayerProperty.PLAYER1, board);
            player1 = GameProperties.SINGLEPLAYER ? new AIPlayer(PlayerProperty.PLAYER2, board) : new HumanPlayer(PlayerProperty.PLAYER2, board);
        }
    }

    public static GameRules getInstance() {
        return INSTANCE == null ? INSTANCE = new GameRules() : INSTANCE;
    }

    public void run() {
        int counter = 1;

        while(!board.getIsGameFinished()) {

            System.out.printf("Turn: %d%n", counter);
            board.printBoard();

            if(counter % 2 == 0) {
                System.out.printf("Player2's turn%n%n");
                if (!player2.makeMove()) {
                    System.err.printf("Invalid move - try again%n%n");
                    counter--;
                }
            } else {
                System.out.printf("Player1's turn%n%n");
                if (!player1.makeMove()) {
                    System.err.printf("Invalid move - try again%n%n");
                    counter--;
                }
            }
            counter++;
        }

        board.printBoard();
        System.out.printf("Game over - %s has won!", Objects.nonNull(board.getHasWon()) ? board.getHasWon() : "no one");
    }
}
