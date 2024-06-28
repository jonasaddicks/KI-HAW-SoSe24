package player;

import game.Board;

import java.util.Stack;
import java.util.stream.Stream;

public abstract class Player {

    private final String token;
    private final int id;
    protected final Board board;
    private Player opponent;
    private Stack<Board.Token> tokensPlayed;



    public Player(PlayerProperty playerProperty, Board board) {
        this(playerProperty, board, null);
    }

    public Player(PlayerProperty playerProperty, Board board, Player opponent) {
        this.token = playerProperty.getToken(playerProperty);
        this.id = playerProperty.getID(playerProperty);
        this.board = board;
        this.opponent = opponent;
        tokensPlayed = new Stack<>();
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

    public void removeLastToken() {
        this.tokensPlayed.removeLast();
    }

    public void addToken(Board.Token token) {
        this.tokensPlayed.push(token);
    }

    public Stream<Board.Token> getPlayersTokens() {
        return this.tokensPlayed.stream();
    }



    public abstract boolean makeMove();

    @Override
    public String toString() {
        return token;
    }
}
