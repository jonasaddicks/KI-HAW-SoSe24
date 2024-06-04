package vierGewinnt;

public class Player {
    private String token;
    private Player oponent;
    private int id;

    public void setOponent(Player oponent){this.oponent=oponent;}

    public Player getOponent(){return oponent;}

    public Player(String token, int id) {
        this.token = token;
        this.id = id;
    }

    public Player() {
        this.token = "N";
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getID() {
        return this.id;
    }
    public void setID(int id) {
        this.id = id;
    }
}
