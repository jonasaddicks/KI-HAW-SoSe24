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
        String genomeString = "/v0AAAD59QXk8AINAekB/An/8f4Q99zuCQcH9vAKC/8H/w8N/PP4Dvno9/8I9fb2Cvz6";

        Genome genome = new Genome(Genome.decodeGenome(genomeString));

        genome.printGenome();
        System.out.println(genome.getEncodedGenome());
    }
}
