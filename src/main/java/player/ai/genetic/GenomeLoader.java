package player.ai.genetic;

import java.io.*;

/**
 * Utility class for loading {@link Genome} objects from a file.
 *
 * The file is expected to contain Base64-encoded genomes, each genome stored
 * on its own line. This class provides methods to:
 * - Load the latest genome written to the file
 * - Load specific "competing" genomes by line index
 *
 * This class cannot be instantiated.
 */
public class GenomeLoader {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private GenomeLoader(){}

    /**
     * Loads the most recently written genome from the given file.
     * <p>
     * This method reads the file backwards until it finds a line break and then
     * decodes the last line as a Base64 genome representation.
     *
     * @param file the file containing Base64-encoded genomes (one genome per line)
     * @return a {@link Genome} instance created from the last encoded genome
     *         in the file, or {@code null} if loading failed
     */
    public static Genome getLatestGenome(File file) {
        RandomAccessFile fileHandler = null;

        try {
            fileHandler = new RandomAccessFile(file, "r" );
            long fileLength = fileHandler.length() - 1;
            StringBuilder sb = new StringBuilder();

            for (long filePointer = fileLength; filePointer != -1; filePointer--){
                fileHandler.seek(filePointer);
                int readByte = fileHandler.readByte();

                if (readByte == 0xA ) {
                    if(filePointer == fileLength) {
                        continue;
                    }
                    break;

                } else if (readByte == 0xD) {
                    if (filePointer == fileLength - 1) {
                        continue;
                    }
                    break;
                }

                sb.append((char) readByte);
            }

            return new Genome(Genome.decodeGenome(sb.reverse().toString()));

        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            if (fileHandler != null)
                try {
                    fileHandler.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return null;
    }

    /**
     * Loads the first competing genome (first line in file).
     *
     * @param file the file containing the genomes
     * @return the first {@link Genome} in the file, or {@code null} if not found
     */
    public static Genome getCompetingGenome1(File file) {
        return getCompetingGenomeNr(file, 0);
    }

    /**
     * Loads the second competing genome (second line in file).
     *
     * @param file the file containing the genomes
     * @return the second {@link Genome} in the file, or {@code null} if not found
     */
    public static Genome getCompetingGenome2(File file) {
        return getCompetingGenomeNr(file, 1);
    }

    /**
     * Loads the third competing genome (third line in file).
     *
     * @param file the file containing the genomes
     * @return the third {@link Genome} in the file, or {@code null} if not found
     */
    public static Genome getCompetingGenome3(File file) {
        return getCompetingGenomeNr(file, 2);
    }

    /**
     * Loads a genome from the specified line number in the file.
     *
     * @param file the file containing Base64-encoded genomes
     * @param nr the zero-based line index of the genome to load
     * @return the {@link Genome} on the specified line, or {@code null} if it does not exist
     */
    private static Genome getCompetingGenomeNr(File file, int nr) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int currentLine = 0;
            while ((line = br.readLine()) != null) {
                if (currentLine == nr) {
                    return new Genome(Genome.decodeGenome(line));
                }
                currentLine++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
