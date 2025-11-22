package player.ai.genetic;

import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

/**
 * Represents a fixed-length genetic encoding used by AI players.
 * <p>
 * The {@code Genome} stores a byte array of length {@code genomeLength} that encodes
 * weighted evaluation parameters and positional scores used by the AI. A derived
 * 2D byte matrix {@code posScoreFirst} is extracted from the genome for convenient
 * access to board-position scores.
 * <p>
 * Instances carry a monotonically increasing population identifier useful for
 * debugging and tracing individuals across evolutionary operations.
 */
public class Genome {

    private final static int genomeLength = 51;

    private final byte[] genome;
    private byte[][] posScoreFirst;

    private static int populationSize = 0;
    private final int populationID;



    /**
     * Constructs a new {@code Genome} initialized with random bytes.
     * The instance receives a unique {@link #populationID} and computes its
     * positional score cache.
     */
    public Genome() {
        this.genome = generateRandomGenome();
        populationID = populationSize++;
        updatePosScore();
    }

    /**
     * Constructs a new {@code Genome} by copying the provided byte array.
     * The source array is defensively copied to ensure the internal representation
     * uses exactly {@link #genomeLength} bytes.
     *
     * @param genome the source genome bytes; only the first {@code genomeLength} bytes are used
     */
    public Genome(byte[] genome) {
        this.genome = Arrays.copyOf(genome, genomeLength);
        populationID = populationSize++;
        updatePosScore();
    }

    /**
     * Copy constructor that clones another {@link Genome} instance.
     * Produces a deep copy of the underlying genome bytes and computes a new
     * population identifier for the created instance.
     *
     * @param genome the genome to copy
     */
    public Genome(Genome genome) {
        this.genome = Arrays.copyOf(genome.genome, genome.genome.length);
        populationID = populationSize++;
        updatePosScore();
    }

    /**
     * Returns the population identifier assigned to this genome instance.
     *
     * @return unique population ID
     */
    public int getPopulationID() {
        return this.populationID;
    }

    /**
     * Returns the configured genome length (constant).
     *
     * @return genome length in bytes
     */
    public static int getGenomeLength() {
        return genomeLength;
    }

    /**
     * Returns a defensive reference to the internal genome byte array.
     * Note: The returned array is the direct internal array reference; callers
     * must not modify it to preserve instance invariants.
     *
     * @return internal genome byte array (length {@link #genomeLength})
     */
    public byte[] getGenome() {
        return this.genome;
    }

    /**
     * Tests whether the genome is a "null" genome, defined as all bytes equal to zero.
     *
     * @return {@code true} if every byte in the genome is zero; {@code false} otherwise
     */
    public boolean isNullGenome() {
        for (int i = 0; i < this.genome.length; i++) {
            if (this.genome[i] != 0) {return false;}
        }
        return true;
    }



    /**
     * Generates a random genome of length {@link #genomeLength} using {@link Random#nextBytes(byte[])}.
     * The returned byte array is freshly allocated.
     *
     * @return byte array filled with random values
     */
    private byte[] generateRandomGenome() {
        Random random = new Random();
        byte[] genome = new byte[genomeLength];
        random.nextBytes(genome);
        return genome;
    }

    /**
     * Extracts and updates the cached positional score table {@link #posScoreFirst} from the raw genome.
     * <p>
     * Extraction rule: starting at genome index 8, fill a 6x7 byte matrix row-major.
     * The method overwrites the internal {@code posScoreFirst} reference.
     */
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

    /**
     * Applies mutation to the genome in-place.
     * <p>
     * Mutation iterates {@code genomeLength * mutationProbability} times. In each iteration
     * a random gene index is selected and a signed integer offset in the range
     * {@code [-mutationRange, mutationRange)} is generated. The offset is applied to the
     * selected byte while ensuring the result remains within {@link Byte#MIN_VALUE} .. {@link Byte#MAX_VALUE}.
     * After mutation completes the positional score cache is refreshed via {@link #updatePosScore()}.
     *
     * @param mutationRange       maximum absolute change applied to a single byte (exclusive upper bound)
     * @param mutationProbability proportion of the genome (0..1) to attempt mutations on (used as multiplier)
     */
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



    /**
     * Weight used for positional scoring for the player.
     *
     * @return byte value stored at genome index 0
     */
    public byte posScoreWeightPlayer() {
        return genome[0];
    }

    /**
     * Weight used for positional scoring for the opponent.
     *
     * @return byte value stored at genome index 1
     */
    public byte posScoreWeightOpponent() {
        return genome[1];
    }

    /**
     * Major-threat evaluation weight for the player.
     *
     * @return byte value stored at genome index 2
     */
    public byte majorWeightPlayer() {
        return genome[2];
    }

    /**
     * Major-threat evaluation weight for the opponent.
     *
     * @return byte value stored at genome index 3
     */
    public byte majorWeightOpponent() {
        return genome[3];
    }

    /**
     * Minor-threat evaluation weight for the player.
     *
     * @return byte value stored at genome index 4
     */
    public byte minorWeightPlayer() {
        return genome[4];
    }

    /**
     * Minor-threat evaluation weight for the opponent.
     *
     * @return byte value stored at genome index 5
     */
    public byte minorWeightOpponent() {
        return genome[5];
    }

    /**
     * Win evaluation weight for the player.
     *
     * @return byte value stored at genome index 6
     */
    public byte winWeightPlayer() {
        return genome[6];
    }

    /**
     * Win evaluation weight for the opponent.
     *
     * @return byte value stored at genome index 7
     */
    public byte winWeightOpponent() {
        return genome[7];
    }

    /**
     * General win evaluation parameter stored at the last genome index.
     *
     * @return byte value stored at genome index {@code genomeLength - 1}
     */
    public byte winEvaluation() {
        return genome[50];
    }

    /**
     * Returns the cached positional score table extracted from the genome.
     *
     * @return a 6x7 byte matrix representing positional scores
     */
    public byte[][] posScore() {
        return this.posScoreFirst;
    }



    /**
     * Encodes a {@link Genome} instance into a Base64 string using its internal bytes.
     *
     * @param genome genome instance to encode
     * @return Base64-encoded representation of the genome bytes
     */
    public static String encodeGenome(Genome genome) {
        // Base64-Encoding
        return encodeGenome(genome.genome);
    }

    /**
     * Encodes a raw byte array into a Base64 string.
     *
     * @param input raw bytes to encode
     * @return Base64-encoded string
     */
    public static String encodeGenome(byte[] input) {
        // Base64-Encoding
        return Base64.getEncoder().encodeToString(input);
    }

    /**
     * Decodes a Base64-encoded genome string into raw bytes.
     *
     * @param input Base64 string representing a genome
     * @return decoded byte array
     */
    public static byte[] decodeGenome(String input) {
        // Base64-Decoding
        return Base64.getDecoder().decode(input);
    }



    /**
     * Returns the Base64-encoded representation of this genome instance.
     *
     * @return Base64 string of internal genome bytes
     */
    public String getEncodedGenome() {
        return encodeGenome(this.genome);
    }

    /**
     * Returns a compact textual representation containing the Base64-encoded genome
     * and the population identifier.
     *
     * @return formatted string {@code "<base64>-id<populationID>"}
     */
    @Override
    public String toString() {
        return String.format("%s-id%d", encodeGenome(genome), populationID);
    }

    /**
     * Prints a human-readable summary of the genome to standard output.
     * The output includes:
     * <ul>
     *     <li>the positional score matrix</li>
     *     <li>the positional and threat weights for player and opponent</li>
     *     <li>the win evaluation parameter</li>
     * </ul>
     * This method is intended for debugging and inspection.
     */
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
