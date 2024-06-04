package vierGewinnt;

public class Main {
    public static void main(String[] args) {
//        vierGewinnt.Multiplayer multiplayer = new vierGewinnt.Multiplayer(new vierGewinnt.Board());
//        multiplayer.play();
        Singleplayer singleplayer = new Singleplayer(new Board(), new AI(), false);
        singleplayer.play();



    }
}