package player.ai.genetic;

public class GenomeTranslator {
    public static void main(String[] args) {

        byte[] genomeArray = new byte[]{
                1, -2, 12, -19, 11, -9, 31, -26,

                -24, -15, 3, 8, 1, -11, -20,
                -13, -15, 8, 13, 12, -21, -11,
                -4, -7, 15, 15, 11, -2, -4,
                8, 4, 17, 24, 22, 2, 6,
                0, -9, -3, 4, 2, -6, -9,
                -9, -13, 4, 13, -1, -14, -21,

                50
        };
        String genomeString = "AfwM7Qv3H+bo8QMIAfXs8/EIDQzr9fz5Dw8L/vwIBBEYFgIGAPf9BAL69/fzBA3/8usy";

        Genome genome = new Genome(genomeArray);

        genome.printGenome();
        System.out.println(genome.getEncodedGenome());
    }
}
