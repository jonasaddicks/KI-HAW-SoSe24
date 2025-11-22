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

/**
 * Application entry point for running the game client.
 *
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Load genomes and configuration resources from bundled files.</li>
 *   <li>Start either a training pipeline (when GAME_MODE == 3) or run regular games / benchmarks.</li>
 *   <li>Persist basic run metadata and benchmark statistics to disk.</li>
 *   <li>Measure and print the elapsed runtime of the client process.</li>
 * </ul>
 * <p>
 * The client dispatches to {@link TrainingGround} for training mode and to {@link GameRules}
 * for non-training modes. Genome loading is delegated to {@link GenomeLoader}.
 * </p>
 *
 * <p><b>Note:</b> This class uses {@link ResourceLoadHelper#loadResource(String)} to resolve
 * resource paths; callers must ensure resources exist on the classpath or accessible location.
 * </p>
 */
public class Client {

    /**
     * Main entry point for the client application.
     *
     * <p>Program flow:
     * <ol>
     *   <li>Start runtime timer.</li>
     *   <li>Load the fittest genome and competing genomes from files.</li>
     *   <li>If GAME_MODE==3, initialize and run a {@link TrainingGround} in supervised mode.</li>
     *   <li>Otherwise, initialize a {@link GameRules} instance and either run a benchmark
     *       suite (modes 4/5) or a single interactive game.</li>
     *   <li>Print elapsed runtime in seconds to standard output.</li>
     * </ol>
     * </p>
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        double timeStampStart = (double) System.currentTimeMillis() / 1000;

        try {
            // Load the most recently selected fittest genome from persistent selection file
            Genome fittestGenome = GenomeLoader.getLatestGenome(new File(ResourceLoadHelper.loadResource("evolutionary/genome.selection/fittestSelection")));

            // Load previously saved AI-vs-AI selection file and extract competing genomes
            File aiVsAi = new File(ResourceLoadHelper.loadResource("evolutionary/genome.selection/aiVsAiSelection"));
            Genome genome1 = GenomeLoader.getCompetingGenome1(aiVsAi);
            Genome genome2 = GenomeLoader.getCompetingGenome2(aiVsAi);



            if (GameProperties.GAME_MODE == 3) { // TRAINING

                // Diagnostic print of the seed genome used for training
                fittestGenome.printGenome();

                // Resolve files used by the training pipeline (stats, fittest selection, benchmarks, generation counter)
                File stats = Paths.get(ResourceLoadHelper.loadResource("evolutionary/stats/genStats")).toFile();
                File fittestSelection = Paths.get(ResourceLoadHelper.loadResource("evolutionary/genome.selection/fittestSelection")).toFile();
                File benchmarkSelection = Paths.get(ResourceLoadHelper.loadResource("evolutionary/genome.selection/benchmarkSelection")).toFile();
                File generationSave = Paths.get(ResourceLoadHelper.loadResource("evolutionary/generation")).toFile();

                // Initialize training ground and a supervisor thread, then run training synchronously
                TrainingGround trainingGround = new TrainingGround(fittestGenome, stats, fittestSelection, benchmarkSelection, generationSave);
                TrainingSupervisor supervisor = new TrainingSupervisor(trainingGround);
                supervisor.start();
                trainingGround.train();
            } else { // NON-TRAINING MODES (game or benchmark)

                GameRules game  = new GameRules(genome1, genome2);

                // Modes 4 and 5 run benchmark flows and persist metadata about players
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
                    // Execute benchmark suite and log results
                    benchmarkGame(game, stats);
                } else {
                    // Interactive or single run game loop
                    game.run();
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        double timeStampEnd = (double) System.currentTimeMillis() / 1000;
        System.out.printf("%fs", timeStampEnd - timeStampStart);
    }

    /**
     * Runs a benchmark series between the configured players inside {@code game} and writes summary lines to the stats file.
     *
     * <p>For each round:
     * <ul>
     *   <li>Invoke {@link GameRules#run()} to execute one match.</li>
     *   <li>Accumulate wins/losses statistics for both players.</li>
     *   <li>Compute and persist per-round win rates and draw rate.</li>
     * </ul>
     *
     * @param game  a configured {@link GameRules} instance that will be reused across rounds (caller must reset or call {@link GameRules#newGame()} inside GameRules logic)
     * @param stats file to which per-round results are appended
     */
    private static void benchmarkGame(GameRules game, File stats) {

        int player1won = 0, player1lost = 0;
        int player2won = 0, player2lost = 0;

        for (int i = 1; i <= GameProperties.NR_BENCHMARK_GAMES; i++) {

            System.out.printf("Round %d: %n", i);

            // Run a single match and inspect the winner (may be null for draw)
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

            // Compute current statistics
            double player1Winrate = (double) player1won / (player1won + player1lost);
            double player2Winrate = (double) player2won / (player2won + player2lost);
            double drawRate = (double) (i - player1won - player1lost) / i;
            String gameResult = String.format("Game %d ----- player1: %d won, %d lost, %f wr ----- player2: %d won, %d lost, %f wr ----- %f dr",
                    i,
                    player1won, player1lost, player1Winrate,
                    player2won, player2lost, player2Winrate,
                    drawRate
                    );

            // Append per-round summary to the stats file
            appendToFile(stats, gameResult);
        }
    }

    /**
     * Appends the supplied string as a new line to the given file.
     * <p>
     * The method opens a {@link BufferedWriter} in append mode and writes a newline
     * followed by the provided content. IOExceptions are logged to stderr.
     * </p>
     *
     * @param file target file to which the line will be appended
     * @param s    content to append
     */
    private static void appendToFile(File file, String s) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.newLine();
            writer.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}