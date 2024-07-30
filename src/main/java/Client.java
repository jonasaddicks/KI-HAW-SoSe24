import game.GameProperties;
import game.GameRules;
import player.ai.genetic.Genome;
import player.ai.genetic.GenomeLoader;
import player.ai.genetic.TrainingGround;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;

public class Client {
    public static void main(String[] args) {
        double timeStampStart = (double) System.currentTimeMillis() / 1000;

        try {
            Genome fittestGenome = GenomeLoader.getLatestGenome(new File(ResourceLoadHelper.loadResource("evolutionary/genome.selection/fittestSelection")));
            File aiVsAi = new File(ResourceLoadHelper.loadResource("evolutionary/genome.selection/aiVsAiSelection"));
            Genome genom1 = GenomeLoader.getCompetingGenome1(aiVsAi);
            Genome genom2 = GenomeLoader.getCompetingGenome2(aiVsAi);



            if (GameProperties.GAMEMODE == 3) { // TRAINING

                fittestGenome.printGenome();

                File stats = new File("src\\main\\resources\\evolutionary\\stats\\genStats");
                File fittestSelection = new File("src\\main\\resources\\evolutionary\\genome.selection\\fittestSelection");
                File benchmarkSelection = Paths.get(ResourceLoadHelper.loadResource("evolutionary/genome.selection/benchmarkSelection")).toFile();
                File generationSave = new File("src\\main\\resources\\evolutionary\\generation");

                TrainingGround trainingGround = new TrainingGround(fittestGenome, stats, fittestSelection, benchmarkSelection, generationSave);
                TrainingSupervisor supervisor = new TrainingSupervisor(trainingGround);
                supervisor.start();
                trainingGround.train();
            } else { // NO TRAINING

                GameRules game  = new GameRules(genom1, genom2);
                game.run();
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        double timeStampEnd = (double) System.currentTimeMillis() / 1000;
        System.out.printf("%fs", timeStampEnd - timeStampStart);
    }
}