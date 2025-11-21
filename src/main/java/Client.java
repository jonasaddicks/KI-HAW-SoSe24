import game.GameProperties;
import game.GameRules;
import player.Player;
import player.ai.genetic.Genome;
import player.ai.genetic.GenomeLoader;
import player.ai.genetic.TrainingGround;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Objects;

public class Client {
    public static void main(String[] args) {
        double timeStampStart = (double) System.currentTimeMillis() / 1000;

        try {
            Genome fittestGenome = GenomeLoader.getLatestGenome(new File(ResourceLoadHelper.loadResource("evolutionary/genome.selection/fittestSelection")));
            File aiVsAi = new File(ResourceLoadHelper.loadResource("evolutionary/genome.selection/aiVsAiSelection"));
            Genome genome1 = GenomeLoader.getCompetingGenome1(aiVsAi);
            Genome genome2 = GenomeLoader.getCompetingGenome2(aiVsAi);



            if (GameProperties.GAME_MODE == 3) { // TRAINING

                fittestGenome.printGenome();

                File stats = Paths.get(ResourceLoadHelper.loadResource("evolutionary/stats/genStats")).toFile();
                File fittestSelection = Paths.get(ResourceLoadHelper.loadResource("evolutionary/genome.selection/fittestSelection")).toFile();
                File benchmarkSelection = Paths.get(ResourceLoadHelper.loadResource("evolutionary/genome.selection/benchmarkSelection")).toFile();
                File generationSave = Paths.get(ResourceLoadHelper.loadResource("evolutionary/generation")).toFile();

                TrainingGround trainingGround = new TrainingGround(fittestGenome, stats, fittestSelection, benchmarkSelection, generationSave);
                TrainingSupervisor supervisor = new TrainingSupervisor(trainingGround);
                supervisor.start();
                trainingGround.train();
            } else { // NO TRAINING

                GameRules game  = new GameRules(genome1, genome2);

                if (GameProperties.GAME_MODE == 4 || GameProperties.GAME_MODE == 5) {
                    File stats = Paths.get(ResourceLoadHelper.loadResource("aiBenchmark/stats/stats")).toFile();

                    if (GameProperties.PLAYER1_STARTS) {

                        if (GameProperties.GAME_MODE == 4) {
                            appendToFile(stats, String.format("player1: %s", genome1.getEncodedGenome()));
                            appendToFile(stats, String.format("player2: %s%n", genome2.getEncodedGenome()));
                        } else {
                            appendToFile(stats, String.format("player1: %s", "humanPlayer"));
                            appendToFile(stats, String.format("player2: %s%n", genome1.getEncodedGenome()));
                        }

                    } else {

                        if (GameProperties.GAME_MODE == 4) {
                            appendToFile(stats, String.format("player1: %s", genome2.getEncodedGenome()));
                            appendToFile(stats, String.format("player2: %s%n", genome1.getEncodedGenome()));
                        } else {
                            appendToFile(stats, String.format("player1: %s", genome1.getEncodedGenome()));
                            appendToFile(stats, String.format("player2: %s%n", "humanPlayer"));
                        }

                    }
                    benchmarkGame(game, stats);
                } else {
                    game.run();
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        double timeStampEnd = (double) System.currentTimeMillis() / 1000;
        System.out.printf("%fs", timeStampEnd - timeStampStart);
    }

    private static void benchmarkGame(GameRules game, File stats) {

        int player1won = 0, player1lost = 0;
        int player2won = 0, player2lost = 0;

        for (int i = 1; i <= GameProperties.NR_BENCHMARK_GAMES; i++) {

            System.out.printf("Round %d: %n", i);

            Player hasWon = game.run();
            if (Objects.nonNull(hasWon)) {
                if (hasWon.getID() == 1) {
                    player1won++;
                    player2lost++;
                } else {
                    player2won++;
                    player1lost++;
                }
            }

            double player1Winrate = (double) player1won / (player1won + player1lost);
            double player2Winrate = (double) player2won / (player2won + player2lost);
            double drawRate = (double) (i - player1won - player1lost) / i;
            String gameResult = String.format("Game %d ----- player1: %d won, %d lost, %f wr ----- player2: %d won, %d lost, %f wr ----- %f dr",
                    i,
                    player1won, player1lost, player1Winrate,
                    player2won, player2lost, player2Winrate,
                    drawRate
                    );
            appendToFile(stats, gameResult);
        }
    }

    private static void appendToFile(File file, String s) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.newLine();
            writer.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}