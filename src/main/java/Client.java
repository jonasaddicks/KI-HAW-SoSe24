import game.GameProperties;
import game.GameRules;
import player.Player;
import player.ai.AIPlayer;
import player.ai.genetic.Genome;
import player.ai.genetic.GenomeLoader;
import player.ai.genetic.TrainingGround;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

import static player.ai.genetic.TrainingProperties.NR_BENCHMARK_GAMES;

public class Client {
    public static void main(String[] args) {
        double timeStampStart = (double) System.currentTimeMillis() / 1000;

        try {
            Genome fittestGenome = GenomeLoader.getLatestGenome(new File(ResourceLoadHelper.loadResource("evolutionary/genome.selection/fittestSelection")));
            File aiVsAi = new File(ResourceLoadHelper.loadResource("evolutionary/genome.selection/aiVsAiSelection"));
            Genome genom1 = GenomeLoader.getCompetingGenome1(aiVsAi);
            Genome genom2 = GenomeLoader.getCompetingGenome2(aiVsAi);



            if (GameProperties.GAMEMODE == 3) { // TRAINING

                fittestGenome.printGenome();

                File stats = new File("src\\main\\resources\\evolutionary\\stats\\genStats");
                File fittestSelection = new File("src\\main\\resources\\evolutionary\\genome.selection\\fittestSelection");
                File benchmarkSelection = Paths.get(ResourceLoadHelper.loadResource("evolutionary/genome.selection/benchmarkSelection")).toFile();
                File generationSave = new File("src\\main\\resources\\evolutionary\\generation");

                TrainingGround trainingGround = new TrainingGround(fittestGenome, stats, fittestSelection, benchmarkSelection, generationSave);
                TrainingSupervisor supervisor = new TrainingSupervisor(trainingGround);
                supervisor.start();
                trainingGround.train();
            } else { // NO TRAINING

                GameRules game  = new GameRules(genom1, genom2);

                if (GameProperties.GAMEMODE == 4 || GameProperties.GAMEMODE == 5) {
                    File stats = new File("src\\main\\resources\\aiBenchmark\\stats\\stats");

                    if (GameProperties.PLAYER1_STARTS) {

                        if (GameProperties.GAMEMODE == 4) {
                            appendToFile(stats, String.format("player1: %s", genom1.getEncodedGenome()));
                            appendToFile(stats, String.format("player2: %s%n", genom2.getEncodedGenome()));
                        } else {
                            appendToFile(stats, String.format("player1: %s", "humanPlayer"));
                            appendToFile(stats, String.format("player2: %s%n", genom1.getEncodedGenome()));
                        }

                    } else {

                        if (GameProperties.GAMEMODE == 4) {
                            appendToFile(stats, String.format("player1: %s", genom2.getEncodedGenome()));
                            appendToFile(stats, String.format("player2: %s%n", genom1.getEncodedGenome()));
                        } else {
                            appendToFile(stats, String.format("player1: %s", genom1.getEncodedGenome()));
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