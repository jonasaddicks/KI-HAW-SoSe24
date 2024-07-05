package game;

import player.ai.AIPlayer;
import player.HumanPlayer;
import player.Player;
import player.PlayerProperty;
import player.ai.Genome;

import java.util.ArrayList;
import java.util.Objects;

public class GameRules {

    private static GameRules INSTANCE;

    private Board board;
    private Player player1;
    private Player player2;
    private Genome fittestGenome = new Genome();//TODO



    private GameRules() {
        this.board = new Board();

        //TODO save and read best genome
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
            case 1 : { //SINGLE PLAYER
                if (GameProperties.PLAYER1_STARTS) {
                    player1 = new HumanPlayer(PlayerProperty.PLAYER1, board, true);
                    player2 = new AIPlayer(PlayerProperty.PLAYER2, board, player1, false, fittestGenome);
                    player1.setOpponent(player2);
                } else {
                    player2 = new HumanPlayer(PlayerProperty.PLAYER1, board, false);
                    player1 = new AIPlayer(PlayerProperty.PLAYER2, board, player2, true, fittestGenome);
                    player2.setOpponent(player1);
                }
                break;
            }
            case 2 : { //AIONLY
                if (GameProperties.PLAYER1_STARTS) {
                    player1 = new AIPlayer(PlayerProperty.PLAYER1, board, true, fittestGenome);
                    player2 = new AIPlayer(PlayerProperty.PLAYER2, board, player1, false, fittestGenome);
                    player1.setOpponent(player2);
                } else {
                    player2 = new AIPlayer(PlayerProperty.PLAYER1, board, false, fittestGenome);
                    player1 = new AIPlayer(PlayerProperty.PLAYER2, board, player2, true, fittestGenome);
                    player2.setOpponent(player1);
                }
                break;
            }
            case 3 : { //TRAIN
                //TODO
            }
        }
    }

    public static GameRules getInstance() {
        return INSTANCE == null ? INSTANCE = new GameRules() : INSTANCE;
    }

    public void run() {
        ArrayList<String> stats = new ArrayList<>();
        int wins, even = 0, player1bias = 0;
        for (int i = 1; i <= 300; i++) {

            int counter = 1;
            player1.setBeginningPlayer(true);
            player2.setBeginningPlayer(false);

            while (!board.getIsGameFinished()) {

                System.out.printf("Turn: %d%n", counter);
                board.printBoard();

                if (counter % 2 == 0) {
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
            System.out.printf("Game over - %s has won!%n%n", Objects.nonNull(board.getHasWon()) ? board.getHasWon() : "no one");

            if (Objects.nonNull(board.getHasWon())) {
                board.getHasWon().gameWon();
                if (board.getHasWon().isBeginningPlayer()) {player1bias++;}
            }
            else {even++;}

            wins = player1.getGamesWon() + player2.getGamesWon();
            stats.add(String.format("%d: --- wins %s: %d, wins %s: %d, no winner: %d   ---   winrate %s: %f, winrate %s: %f   ---   clean winrate %s: %f, clean winrate %s: %f ### player1bias: %d, %f%n",
                    i,
                    player1.getToken(),
                    player1.getGamesWon(),
                    player2.getToken(),
                    player2.getGamesWon(),
                    even,
                    player1.getToken(),
                    (float)player1.getGamesWon()/i,
                    player2.getToken(),
                    (float)player2.getGamesWon()/i,
                    player1.getToken(),
                    wins != 0 ? (float) player1.getGamesWon() / wins : 0f,
                    player2.getToken(),
                    wins != 0 ? (float) player2.getGamesWon() / wins : 0f,
                    player1bias,
                    wins != 0 ? (float) player1bias / wins : 0f
            ));
            System.out.print(stats.getLast());

            Player tempPlayer = player1;
            player1 = player2;
            player2 = tempPlayer;

            this.resetGame();
        }

        for (String stat : stats) {
            System.out.print(stat);
        }
    }

    private void resetGame() {
        this.board.resetBoard();
        this.player1.clearTokens();
        this.player2.clearTokens();
    }
}