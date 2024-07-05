package player.ai;

import java.util.Arrays;

public class Genome {

    private final static int genomeLength = 92;

    private byte[] genome;
    private byte[][] posScoreFirst;
    private byte[][] posScoreSecond;



    public Genome() {
        genome = generateRandomGenome();
        updatePosScore();
    }

    public Genome(byte[] genome) {
        this.genome = Arrays.copyOf(genome, genomeLength);
        updatePosScore();
    }

    private byte[] generateRandomGenome() {
        byte[] genome = new byte[genomeLength];
        //TODO
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

        byte[][] posScoreSecond = new byte[6][7];
        for (byte i = 0; i < posScoreSecond.length; i++) {
            for (byte j = 0; j < posScoreSecond[i].length; j++) {
                posScoreSecond[i][j] = genome[counter];
                counter++;
            }
        }
        this.posScoreSecond = posScoreSecond;
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

    public byte[][] posScoreFirst() {
        return this.posScoreFirst;
    }

    public byte[][] posScoreSecond() {
        return this.posScoreSecond;
    }



    @Override
    public String toString() {
        //TODO
        return null;
    }
}
