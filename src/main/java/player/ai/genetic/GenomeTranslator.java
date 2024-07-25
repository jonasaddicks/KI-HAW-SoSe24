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
        String genomeString = "/fH+9v0G9Avy9vkD++T07PkA/AcB8/n7/AwE+fDvBv0H+wYH+en4A/X0+/707fXw8/Tj";

        Genome genome = new Genome(Genome.decodeGenome(genomeString));

        genome.printGenome();
        System.out.println(genome.getEncodedGenome());
    }
}
