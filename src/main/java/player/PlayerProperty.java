package player;

/**
 * Enum representing the two player roles used by the game.
 * <p>
 * Each enum constant provides a printable token (Unicode circle) and a numeric id.
 * The API exposes accessor methods that return the token string and numeric id
 * for a given {@code PlayerProperty} value.
 */
public enum PlayerProperty {
    PLAYER1,
    PLAYER2;

    private final String tokenPlayer1 = new String(Character.toChars(0x0001F535));
    private final String tokenPlayer2 = new String(Character.toChars(0x0001F534));

    /**
     * Returns the printable token string associated with the supplied {@code PlayerProperty}.
     * <p>
     * Example:
     * <pre>
     * {@code String token = PlayerProperty.PLAYER1.getToken(PlayerProperty.PLAYER1);}
     * </pre>
     *
     * @param playerProperty the enum constant for which to return the token
     * @return a {@link String} containing the Unicode token for the requested player
     */
    public String getToken(PlayerProperty playerProperty) {
        return switch (playerProperty) {
            case PLAYER1 -> tokenPlayer1;
            case PLAYER2 -> tokenPlayer2;
        };
    }

    /**
     * Returns the numeric identifier associated with the supplied {@code PlayerProperty}.
     * Typical values are 1 for PLAYER1 and 2 for PLAYER2.
     *
     * @param playerProperty the enum constant for which to return the id
     * @return integer id for the requested player (1 or 2)
     */
    public int getID(PlayerProperty playerProperty) {
        return switch (playerProperty) {
            case PLAYER1 -> 1;
            case PLAYER2 -> 2;
        };
    }
}
