import game.GameProperties;
import game.GameRules;
import player.ai.genetic.Genome;
import player.ai.genetic.GenomeLoader;
import player.ai.genetic.TrainingGround;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;

public class Client {
    public static void main(String[] args) {
        //TODO debug
        double timeStampStart = (double) System.currentTimeMillis() / 1000;

        try {
            Genome fittestGenome = GenomeLoader.getLatestGenome(new File(ResourceLoadHelper.loadResource("genome.selection/fittestSelection")));


            //TODO debug
            System.out.printf("PosScore1:%n%s%n%nPosScore2:%n%s%n%nweightPos1: %d, weightPos2: %d%nweightMajor1: %d, weightMajor2: %d%nweightMinor1: %d, weightMinor2: %d%nweightWin1: %d, weightWin2: %d%nwin: %d%n%n",
                    Arrays.deepToString(fittestGenome.posScoreFirst()),
                    Arrays.deepToString(fittestGenome.posScoreSecond()),
                    fittestGenome.posScoreWeightPlayer(),
                    fittestGenome.posScoreWeightOpponent(),
                    fittestGenome.majorWeightPlayer(),
                    fittestGenome.majorWeightOpponent(),
                    fittestGenome.minorWeightPlayer(),
                    fittestGenome.minorWeightOpponent(),
                    fittestGenome.winWeightPlayer(),
                    fittestGenome.winWeightOpponent(),
                    fittestGenome.winEvaluation());


            if (GameProperties.GAMEMODE == 3) { // TRAINING
                TrainingGround trainingGround = new TrainingGround(fittestGenome);
                trainingGround.train();
            } else { // NO TRAINING
                GameRules game  = new GameRules(fittestGenome, null);
                game.run();
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        //TODO debug
        double timeStampEnd = (double) System.currentTimeMillis() / 1000;
        System.out.printf("%fs", timeStampEnd - timeStampStart);
    }
}