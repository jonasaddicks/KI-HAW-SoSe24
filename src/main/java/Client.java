import game.GameRules;
import player.ai.genetic.GenomeLoader;

import java.io.File;
import java.net.URISyntaxException;

public class Client {
    public static void main(String[] args) {
        try {
            GameRules game = new GameRules(GenomeLoader.getLatestGenome(new File(ResourceLoadHelper.loadResource("genome.selection/fittestSelection"))), null);
            game.run();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


}