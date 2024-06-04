package game;

import player.AIPlayer;
import player.HumanPlayer;
import player.Player;
import player.PlayerProperty;

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

            if(counter % 2 == 0) {
                player2.makeMove();
            } else {
                player1.makeMove();
            }
            board.printBoard();

            counter++;
        }
    }
}
