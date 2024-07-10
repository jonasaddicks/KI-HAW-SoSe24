package player.ai.genetic;

import java.util.Arrays;

public class GenomeTranslator {
    public static void main(String[] args) {

        Genome fittestGenome = new Genome(Genome.decodeGenome("AP8L7QX7/AP9AAfz/wX/AwgEAAL+/wUAAwAAAgUAAP0AAAMAAAX9AP0FAPr4BgnyAAAAAgQDAfoABAL5APYAAPsAAAAGAvsCAAAD+AEDAwD7APUAAPsAAf8FAP3z"));

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
    }
}
