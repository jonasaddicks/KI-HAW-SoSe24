package player;

import game.Board;

import java.util.Stack;
import java.util.stream.Stream;

public abstract class Player {

    private final String token;
    private final int id;
    private boolean beginningPlayer;
    private int gamesWon;

    protected final Board board;
    private Player opponent;
    private Stack<Board.Token> tokensPlayed;



    public Player(PlayerProperty playerProperty, Board board, boolean beginningPlayer) {
        this(playerProperty, board, null, beginningPlayer);
    }

    public Player(PlayerProperty playerProperty, Board board, Player opponent, boolean beginningPlayer) {
        this.token = playerProperty.getToken(playerProperty);
        this.id = playerProperty.getID(playerProperty);
        this.beginningPlayer = beginningPlayer;
        this.gamesWon = 0;

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

    public boolean isBeginningPlayer() {
        return this.beginningPlayer;
    }

    public void setBeginningPlayer(boolean beginningPlayer) {
        this.beginningPlayer = beginningPlayer;
    }

    public void gameWon() {
        this.gamesWon++;
    }

    public int getGamesWon() {
        return this.gamesWon;
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

    public void clearTokens() {
        this.tokensPlayed.clear();
    }



    public abstract boolean makeMove();

    @Override
    public String toString() {
        return token;
    }
}
