package player.ai.genetic;

import java.util.Arrays;

public class GenomeTranslator {
    public static void main(String[] args) {

        byte[] genome = new byte[]{
                0, 0, 0, 0, 0, 0, 10, -10,

                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,

                100
        };

        Genome fittestGenome = new Genome(genome);

        System.out.printf("PosScore1:%n%s%n%nweightPos1: %d, weightPos2: %d%nweightMajor1: %d, weightMajor2: %d%nweightMinor1: %d, weightMinor2: %d%nweightWin1: %d, weightWin2: %d%nwin: %d%n%n",
                Arrays.deepToString(fittestGenome.posScore()),
                fittestGenome.posScoreWeightPlayer(),
                fittestGenome.posScoreWeightOpponent(),
                fittestGenome.majorWeightPlayer(),
                fittestGenome.majorWeightOpponent(),
                fittestGenome.minorWeightPlayer(),
                fittestGenome.minorWeightOpponent(),
                fittestGenome.winWeightPlayer(),
                fittestGenome.winWeightOpponent(),
                fittestGenome.winEvaluation());

        System.out.println(fittestGenome.getEncodedGenome());
    }
}
