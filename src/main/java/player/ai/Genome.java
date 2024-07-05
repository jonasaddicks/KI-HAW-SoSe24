package player.ai;

import java.util.Arrays;

public class Genome {

    private final static int genomeLength = 92;

    private int[] genome;
    private int[][] posScoreFirst;
    private int[][] posScoreSecond;



    public Genome() {
        genome = generateRandomGenome();
        updatePosScore();
    }

    public Genome(int[] genome) {
        this.genome = Arrays.copyOf(genome, genomeLength);
        updatePosScore();
    }

    private int[] generateRandomGenome() {
        int[] genome = new int[genomeLength];
        //TODO
        return genome;
    }

    private void updatePosScore() {
        int[][] posScoreFirst = new int[6][7];
        int counter = 8;
        for (int i = 0; i < posScoreFirst.length; i++) {
            for (int j = 0; j < posScoreFirst[i].length; j++) {
                posScoreFirst[i][j] = genome[counter];
                counter++;
            }
        }
        this.posScoreFirst = posScoreFirst;

        int[][] posScoreSecond = new int[6][7];
        for (int i = 0; i < posScoreSecond.length; i++) {
            for (int j = 0; j < posScoreSecond[i].length; j++) {
                posScoreSecond[i][j] = genome[counter];
                counter++;
            }
        }
        this.posScoreSecond = posScoreSecond;
    }



    public int posScoreWeightPlayer() {
        return genome[0];
    }

    public int posScoreWeightOpponent() {
        return genome[1];
    }

    public int majorWeightPlayer() {
        return genome[2];
    }

    public int majorWeightOpponent() {
        return genome[3];
    }

    public int minorWeightPlayer() {
        return genome[4];
    }

    public int minorWeightOpponent() {
        return genome[5];
    }

    public int winWeightPlayer() {
        return genome[6];
    }

    public int winWeightOpponent() {
        return genome[7];
    }

    public int[][] posScoreFirst() {
        return this.posScoreFirst;
    }

    public int[][] posScoreSecond() {
        return this.posScoreSecond;
    }



    @Override
    public String toString() {
        //TODO
        return null;
    }
}
