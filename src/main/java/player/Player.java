package player;

import game.Board;

public abstract class Player {

    private final String token;
    private final int id;
    protected final Board board;



    public Player(PlayerProperty playerProperty, Board board) {
        this.token = playerProperty.getToken(playerProperty);
        this.id = playerProperty.getID(playerProperty);
        this.board = board;
    }



    public String getToken() {
        return token;
    }

    public int getID() {
        return this.id;
    }



    public abstract void makeMove();
}
