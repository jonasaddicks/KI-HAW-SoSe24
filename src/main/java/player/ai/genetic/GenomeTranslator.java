package player.ai.genetic;

public class GenomeTranslator {
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
