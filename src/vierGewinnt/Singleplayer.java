package vierGewinnt;

import java.util.Scanner;

public class Singleplayer extends Gamemode{

    private final Player player;
    private final Board board;
    private final Scanner scanner;
    private int gameCounter;
    private final AI playerAI;
    private final boolean beginning;

    public Singleplayer(Board board, AI playerAI, Boolean beginning) {
        this.board = board;
        this.playerAI = playerAI;
        this.beginning = beginning;
        scanner = new Scanner(System.in);
        player = new Player();
        if (beginning) {
            this.playerAI.setToken(tokenPlayer1);
            this.playerAI.setID(1);
            this.player.setToken(tokenPlayer2);
            this.player.setID(2);
        } else {
            this.playerAI.setToken(tokenPlayer1);
            this.playerAI.setID(1);
            this.player.setToken(tokenPlayer2);
            this.player.setID(2);
        }
        playerAI.setOponent(player);
        player.setOponent(playerAI);
        gameCounter = 1;
    }

    public void play() {
        System.out.println("Welcome to 4Gewinnt vierGewinnt.Singleplayer Mode!\n" +
                "Choose your column by entering 1-7.");
        if (beginning) {
            System.out.println("Turn: 1 -- Your Turn:");
            board.printBoard();
            while (!board.getIsGameFinished()) {
                if (gameCounter % 2 == 0 && scanner.hasNext()) {
                    System.out.printf("Turn: %d -- vierGewinnt.AI's Turn\n", gameCounter + 2);
                    if (!board.placeToken(Integer.parseInt(scanner.next()), player)) {
                        System.out.println("Column is either full or you entered an invalid column! Please pick another one.");
                        gameCounter--;
                    }
                } else {
                    System.out.printf("Turn: %d -- Your Turn\n", gameCounter + 2);
                    if (!board.placeToken(playerAI.makeMove(board), playerAI)) {
                        System.out.println("Column is either full or you entered an invalid column! Please pick another one.");
                        gameCounter--;
                    }
                }
                gameCounter++;
            }
        } else {
            while (!board.getIsGameFinished()) {
                if (gameCounter % 2 == 0 && scanner.hasNext()) {
                    System.out.printf("Turn: %d -- Your Turn\n", gameCounter++);
                    //gameCounter++;
                    if (!board.placeToken(Integer.parseInt(scanner.next()), player)) {
                        System.out.println("Column is either full or you entered an invalid column! Please pick another one.");
                        gameCounter--;
                    }
                    board.printBoard();
                } else {
                    System.out.printf("Turn: %d -- vierGewinnt.AI's Turn\n", gameCounter++);
                    if (!board.placeToken(playerAI.makeMove(board), playerAI)) {
                        System.out.println("Column is either full or you entered an invalid column! Please pick another one.");
                        gameCounter--;
                    }
                    board.printBoard();
                }
                //board.printBoard();
            }
            //TODO
//            if (board.isFull()) {
//                System.out.println("Draw!");
//            } else if (gameCounter % 2 != 0) {
//                System.out.println("You win!");
//            } else {
//                System.out.println("You lose!");
//            }
        }
    }
}
