package player.ai.genetic;

/**
 * Utility / test application to translate between a raw genome byte array and its
 * Base64-encoded string representation.
 *
 * This class is primarily used for debugging, manual testing and validation of the
 * {@link Genome} encoding/decoding process as well as for visually inspecting
 * the internal weights of a genome.
 *
 * IMPORTANT:
 * This is a test/utility class and should not be used in production logic.
 * For production use, move translation logic into a dedicated service class.
 */
public class GenomeTranslator {

    /**
     * Entry point for manual genome translation and validation.
     *
     * This method demonstrates:
     * - How a raw byte array can represent a genome
     * - How to decode a Base64 genome String into a {@link Genome}
     * - How to print the genome in a human-readable format
     * - How to re-encode the genome into its Base64 representation
     *
     * @param args command line arguments (currently unused)
     */
    public static void main(String[] args) {

        byte[] genomeArray = new byte[]{
                1, -1, 1, -1, 1, -1, 100, -100,

                3, 4, 5, 7, 5, 4, 3,
                4, 6, 8, 10, 8, 6, 4,
                5, 8, 11, 13, 11, 8, 5,
                5, 8, 11, 13, 11, 8, 5,
                4, 6, 8, 10, 8, 6, 4,
                3, 4, 5, 7, 5, 4, 3,

                100
        };
        String genomeString = "Af4M7Qv3H+b0/vLW9vMADBIBCAH9Ag79BPoI+wny/gb//AML9wf0AgX7/gL/9Af8//8F";

        Genome genome = new Genome(Genome.decodeGenome(genomeString));

        genome.printGenome();
        System.out.println(genome.getEncodedGenome());
    }
}
