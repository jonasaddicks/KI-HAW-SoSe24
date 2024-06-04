package player;

public enum PlayerProperty {
    PLAYER1,
    PLAYER2;

    private final String tokenPlayer1 = new String(Character.toChars(0x0001F535));
    private final String tokenPlayer2 = new String(Character.toChars(0x0001F534));

    public String getToken(PlayerProperty playerProperty) {
        return switch (playerProperty) {
            case PLAYER1 -> tokenPlayer1;
            case PLAYER2 -> tokenPlayer2;
        };
    }

    public int getID(PlayerProperty playerProperty) {
        return switch (playerProperty) {
            case PLAYER1 -> 1;
            case PLAYER2 -> 2;
        };
    }
}
