package player;

import game.Board;

public abstract class Player {

    private final String token;
    private final int id;
    protected final Board board;
    private Player opponent;



    public Player(PlayerProperty playerProperty, Board board) {
        this(playerProperty, board, null);
    }

    public Player(PlayerProperty playerProperty, Board board, Player opponent) {
        this.token = playerProperty.getToken(playerProperty);
        this.id = playerProperty.getID(playerProperty);
        this.board = board;
        this.opponent = opponent;
    }



    public String getToken() {
        return token;
    }

    public int getID() {
        return this.id;
    }

    public Player getOpponent() {
        return this.opponent;
    }

    public void setOpponent(Player opponent) {
        this.opponent = opponent;
    }



    public abstract boolean makeMove();

    @Override
    public String toString() {
        return token;
    }
}
