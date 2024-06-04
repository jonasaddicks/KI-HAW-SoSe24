package vierGewinnt;

public abstract class Gamemode {
    final String tokenPlayer1 = new String(Character.toChars(0x0001F535));
    final String tokenPlayer2 = new String(Character.toChars(0x0001F534));

    abstract void play();
}
