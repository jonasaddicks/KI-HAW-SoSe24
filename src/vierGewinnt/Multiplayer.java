package vierGewinnt;

import java.util.Scanner;

public class Multiplayer extends Gamemode {
    private final Player player1;
    private final Player player2;

    private final Board board;
    private final Scanner scanner;
    private int gameCounter;

    public Multiplayer(Board board) {
        this.board = board;
        player1 = new Player(tokenPlayer1, 1);
        player2 = new Player(tokenPlayer2, 2);
        player1.setOponent(player2);
        player2.setOponent(player1);
        scanner = new Scanner(System.in);
        gameCounter = 0;
    }

    public void play() {
        System.out.println("Welcome to 4Gewinnt vierGewinnt.Multiplayer Mode!\n" +
                "Choose your column by entering 1-7.");
        System.out.println("Turn: 1 -- Player1's Turn:");
        board.printBoard();
        while ((!board.getIsGameFinished())) {
            if (gameCounter % 2 == 0 && scanner.hasNext()) {
                System.out.printf("Turn: %d -- Player2's Turn\n", gameCounter + 2);
                if (!board.placeToken(Integer.parseInt(scanner.next()), player1)) {
                    System.out.println("Column is either full or you entered an invalid column! Please pick another one.");
                    gameCounter--;
                }
                board.printBoard();
            } else if (scanner.hasNext()) {
                System.out.printf("Turn: %d -- Player1's Turn\n", gameCounter + 2);
                if (!board.placeToken(Integer.parseInt(scanner.next()), player2)) {
                    System.out.println("Column is either full or you entered an invalid column! Please pick another one.");
                    gameCounter--;
                }
                board.printBoard();
            }
            gameCounter++;
        }
        System.out.println("winner: " + board.getHasWon().getToken());
    }
}
