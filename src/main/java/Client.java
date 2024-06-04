import game.GameRules;

public class Client {
    public static void main(String[] args) {
        GameRules game = GameRules.getInstance();
        game.run();
    }
}