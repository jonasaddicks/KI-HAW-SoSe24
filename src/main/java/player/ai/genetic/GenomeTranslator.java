package player.ai.genetic;

public class GenomeTranslator {
    public static void main(String[] args) {

        byte[] genomeArray = new byte[]{
                0, 0, 0, 0, 0, 0, 10, -10,

                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,

                100
        };
        String genomeString = "+voB9QrrF/zg8AID/usBBAT94f4K9+b9BQMJ8fIJCPYHEQ8LAvb5Eerz7/wG9f/2Cvbw";


        Genome genome = new Genome(Genome.decodeGenome(genomeString));

        genome.printGenome();
        System.out.println(genome.getEncodedGenome());
    }
}
