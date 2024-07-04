package player;

import game.Board;

import java.util.Scanner;

public class HumanPlayer extends Player {

    private final Scanner PROMPT = new Scanner(System.in);

    public HumanPlayer(PlayerProperty playerProperty, Board board, boolean beginningPlayer) {
        super(playerProperty, board, beginningPlayer);
    }

    public HumanPlayer(PlayerProperty playerProperty, Board board, Player opponent, boolean beginningPlayer) {
        super(playerProperty, board, opponent, beginningPlayer);
    }

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
