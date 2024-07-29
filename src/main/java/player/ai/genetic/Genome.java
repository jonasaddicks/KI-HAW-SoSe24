package player.ai.genetic;

import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

public class Genome {

    private final static int genomeLength = 51;

    //TODO
    private static final byte[][] posScore;
    private final byte[] genome;
    private byte[][] posScoreFirst;

    private static int populationSize = 0;
    private final int populationID;

    //TODO
    static {
        posScore = new byte[][]
                {
                {-24, -15, 3, 8, 1, -11, -20},
                {-13, -15, 8, 13, 12, -21, -11},
                {-4, -7, 15, 15, 11, -2, -4},
                {8, 4, 17, 24, 22, 2, 6},
                {0, -9, -3, 4, 2, -6, -9},
                {-9, -13, 4, 13, -1, -14, -21},
        };
    }



    public Genome() {
        this.genome = generateRandomGenome();
        populationID = populationSize++;
        updatePosScore();
    }

    public Genome(byte[] genome) {
        this.genome = Arrays.copyOf(genome, genomeLength);
        populationID = populationSize++;
        updatePosScore();
    }

    public Genome(Genome genome) {
        this.genome = Arrays.copyOf(genome.genome, genome.genome.length);
        populationID = populationSize++;
        updatePosScore();
    }

    public int getPopulationID() {
        return this.populationID;
    }

    public static int getGenomeLength() {
        return genomeLength;
    }

    public byte[] getGenome() {
        return this.genome;
    }

    public boolean isNullGenome() {
        for (int i = 0; i < this.genome.length; i++) {
            if (this.genome[i] != 0) {return false;}
        }
        return true;
    }



    private byte[] generateRandomGenome() {
        Random random = new Random();
        byte[] genome = new byte[genomeLength];
        random.nextBytes(genome);
        return genome;
    }

    private void updatePosScore() {
        byte[][] posScoreFirst = new byte[6][7];
        int counter = 8;
        for (int i = 0; i < posScoreFirst.length; i++) {
            for (int j = 0; j < posScoreFirst[i].length; j++) {
                posScoreFirst[i][j] = genome[counter];
                counter++;
            }
        }
        this.posScoreFirst = posScoreFirst;
    }

    public void mutate(int mutationRange, double mutationProbability) {
        Random mutateRandom = new Random();
        for (int i = 0; i < genomeLength * mutationProbability; i++) {
            int genToBeMutated = mutateRandom.nextInt(genomeLength);
            int mutation = mutateRandom.nextInt(2 * mutationRange) - mutationRange;
            int currentGen = genome[genToBeMutated];

            while (currentGen + mutation > Byte.MAX_VALUE || currentGen + mutation < Byte.MIN_VALUE) {
                mutation = mutateRandom.nextInt(2 * mutationRange) - mutationRange;
            }
            genome[genToBeMutated] += mutation;
        }
        updatePosScore();
    }



    public byte posScoreWeightPlayer() {
        return genome[0];
    }

    public byte posScoreWeightOpponent() {
        return genome[1];
    }

    public byte majorWeightPlayer() {
        return genome[2];
    }

    public byte majorWeightOpponent() {
        return genome[3];
    }

    public byte minorWeightPlayer() {
        return genome[4];
    }

    public byte minorWeightOpponent() {
        return genome[5];
    }

    public byte winWeightPlayer() {
        return genome[6];
    }

    public byte winWeightOpponent() {
        return genome[7];
    }

    public byte winEvaluation() {
        return genome[50];
    }

    public byte[][] posScore() {
        //TODO
        return this.posScore;
//        return this.posScoreFirst;
    }



    public static String encodeGenome(Genome genome) {
        // Base64-Encoding
        return encodeGenome(genome.genome);
    }

    public static String encodeGenome(byte[] input) {
        // Base64-Encoding
        return Base64.getEncoder().encodeToString(input);
    }

    public static byte[] decodeGenome(String input) {
        // Base64-Decoding
        return Base64.getDecoder().decode(input);
    }



    public String getEncodedGenome() {
        return encodeGenome(this.genome);
    }

    @Override
    public String toString() {
        return String.format("%s-id%d", encodeGenome(genome), populationID);
    }

    public void printGenome() {
        System.out.printf("PosScore1:%n%s%n%nweightPos1: %d, weightPos2: %d%nweightMajor1: %d, weightMajor2: %d%nweightMinor1: %d, weightMinor2: %d%nweightWin1: %d, weightWin2: %d%nwin: %d%n%n",
                Arrays.deepToString(this.posScore()),
                this.posScoreWeightPlayer(),
                this.posScoreWeightOpponent(),
                this.majorWeightPlayer(),
                this.majorWeightOpponent(),
                this.minorWeightPlayer(),
                this.minorWeightOpponent(),
                this.winWeightPlayer(),
                this.winWeightOpponent(),
                this.winEvaluation());
    }
}
