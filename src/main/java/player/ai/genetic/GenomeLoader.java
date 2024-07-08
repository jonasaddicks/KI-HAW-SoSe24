package player.ai.genetic;

import java.io.*;

public class GenomeLoader {

    private GenomeLoader(){}

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

    public static Genome getCompetingGenome1(File file) {
        return getCompetingGenomeNr(file, 0);
    }

    public static Genome getCompetingGenome2(File file) {
        return getCompetingGenomeNr(file, 1);
    }

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
