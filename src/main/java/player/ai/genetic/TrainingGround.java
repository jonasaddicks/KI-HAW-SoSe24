package player.ai.genetic;

import game.GameRules;
import player.ai.AIPlayer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static player.ai.genetic.TrainingProperties.*;


/**
 * Orchestrates an evolutionary training loop for {@link Genome}-based AI players.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Maintain a population of candidate genomes and their fitness metadata.</li>
 *   <li>Execute selection, crossover, mutation and fitness evaluation phases.</li>
 *   <li>Persist generation metadata, fittest selections and benchmark results to files.</li>
 *   <li>Support interruption via an interrupt flag to gracefully stop training.</li>
 * </ul>
 *
 * <p><b>Concurrency model:</b> Fitness evaluation is parallelized using a per-genome
 * {@link TrainingSession} thread. The class itself is not thread-safe beyond the
 * use of {@code volatile} for the interrupt flag; concurrent access to methods that
 * read or modify internal collections must be externally synchronized if needed.</p>
 *
 * <p>This class depends on constants defined in {@link TrainingProperties} such as:
 * {@code POPULATION_SIZE}, {@code ELITIST_SHARE}, {@code TOURNAMENT_SIZE},
 * {@code NR_GENERATIONS}, {@code NR_FITNESS_GAMES}, {@code NR_BENCHMARK_GAMES},
 * {@code GENOME_MUTATION_PROBABILITY}, {@code MUTATION_RANGE} and {@code GEN_MUTATION_PROBABILITY}.</p>
 */
public class TrainingGround {

    private volatile boolean interruptFlag;

    private final File stats;
    private final File fittestSelection;
    private final File generationSave;

    private final Genome benchmark1;
    private final Genome benchmark2;
    private final Genome benchmark3;

    private List<GenomeFitness> population;
    private int generationNr;


    /**
     * Constructs a training ground and initializes the population using clones of the provided fittestGenome.
     *
     * @param fittestGenome     The seed genome used to initialize the entire population (cloned for each member).
     * @param stats             File to append generation statistics and benchmark logs.
     * @param fittestSelection  File to append the encoded fittest genome for each generation.
     * @param benchmarkSelection File that contains up to three benchmark genomes (one per line) to be loaded and used for benchmarking.
     * @param generationSave    File used to persist the current generation number between runs.
     */
    public TrainingGround(Genome fittestGenome, File stats, File fittestSelection, File benchmarkSelection, File generationSave) {
        this.interruptFlag = false;

        this.stats = stats;
        this.fittestSelection = fittestSelection;
        this.generationSave = generationSave;

        this.benchmark1 = GenomeLoader.getCompetingGenome1(benchmarkSelection);
        this.benchmark2 = GenomeLoader.getCompetingGenome2(benchmarkSelection);
        this.benchmark3 = GenomeLoader.getCompetingGenome3(benchmarkSelection);

        this.population = new ArrayList<>();
        this.generationNr = getStartingGeneration() + 1;
        initTraining(fittestGenome);
    }

    /**
     * Request a graceful interruption of the running training loop.
     * Setting this flag to {@code true} will cause the outer training loop
     * to stop after the current generation completes.
     */
    public void flagInterrupt() {
        this.interruptFlag = true;
    }

    /**
     * Public entry point that begins the training process.
     * Delegates to {@link #runTraining()} which performs the iterative evolutionary steps.
     */
    public void train() {
        runTraining();
    }

    /**
     * Initializes the population by cloning the provided fittest genome.
     * Each created {@link GenomeFitness} instance receives a seeded history so
     * it does not start with zero games, which helps avoid division by zero
     * or unstable early fitness calculations.
     *
     * @param fittestGenome seed genome used for cloning population members
     */
    private void initTraining(Genome fittestGenome) {
        for (int i = 0; i < POPULATION_SIZE; i++) {
            GenomeFitness newGenomeFitness = new GenomeFitness(new Genome(fittestGenome));
            newGenomeFitness.gamesWon++;
            newGenomeFitness.gamesLost++;
            newGenomeFitness.gamesPlayed += 2;
            this.population.add(newGenomeFitness);
        }
    }

    /**
     * Main training loop that executes a fixed number of generations or stops early
     * if {@link #flagInterrupt()} is invoked. Each generation runs the following pipeline:
     * <ol>
     *   <li>selectFirstStage (elitist selection)</li>
     *   <li>crossover (tournament selection & crossover)</li>
     *   <li>mutate (random mutations)</li>
     *   <li>evaluateFitness (parallel simulated matches)</li>
     * </ol>
     * After evolution steps the fittest genome is saved and benchmarked.
     */
    private void runTraining() {
        int maxGeneration = generationNr + NR_GENERATIONS;
        for (int i = generationNr; !this.interruptFlag && i < maxGeneration; i++) {

            selectFirstStage(); //elitist
            crossover(); //tournament and crossover
            mutate(); //mutate over population
            evaluateFitness(); //simulated games

            saveFittest();
            benchmarkFittest();

            setStartingGeneration();
            this.generationNr++;
        }
    }



    /**
     * Evaluates fitness for every genome in the population by launching one {@link TrainingSession}
     * per genome and waiting for all sessions to complete. After completion the population is sorted
     * using {@link GenomeFitness#compareTo} so that index 0 refers to the fittest.
     */
    private void evaluateFitness() {
        List<Thread> trainingSessions = new ArrayList<>();
        for (int i = 0; i < population.size(); i++) {
            TrainingSession session = new TrainingSession(population.get(i));
            trainingSessions.add(session);
            session.start();
        }

        for (Thread session : trainingSessions) {
            try {
                session.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        population.sort(GenomeFitness::compareTo);
    }

    /**
     * Performs an elitist selection by copying the top portion of the population over
     * the bottom portion to be replaced. The number of killed individuals is derived
     * from {@code POPULATION_SIZE} and {@code ELITIST_SHARE}.
     *
     * The implementation shifts winners downwards into the positions of the killed entries.
     */
    private void selectFirstStage() {
        int killShare = (int) (POPULATION_SIZE - POPULATION_SIZE * ELITIST_SHARE);
        for (int i = POPULATION_SIZE - 1; i >= killShare; i--) {
            population.set(i, population.get(i - killShare));
        }
    }

    /**
     * Rebuilds a new population by:
     * <ol>
     *   <li>copying the elite top fraction (preserved without modification)</li>
     *   <li>filling the remainder via pairwise tournament-selected crossover offspring</li>
     * </ol>
     */
    private void crossover() {
        List<GenomeFitness> newPopulation = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE * ELITIST_SHARE; i++) {
            newPopulation.add(new GenomeFitness(population.get(i).genome));
        }

        while (newPopulation.size() < POPULATION_SIZE) {
            Genome parent1 = tournament();
            Genome parent2 = tournament();
            crossover(parent1, parent2, newPopulation);
        }
        this.population = newPopulation;
    }

    /**
     * Selects one parent genome using a tournament selection drawn from the non-elite portion.
     * The method randomly samples competitors and returns the winner with the best fitness.
     *
     * @return selected parent genome for crossover
     */
    private Genome tournament() {
        int elitistBound = (int) (POPULATION_SIZE * ELITIST_SHARE);
        Random selectRandom = new Random();
        GenomeFitness winner = population.get(selectRandom.nextInt(population.size() - elitistBound) + elitistBound);

        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            GenomeFitness competitor = population.get(selectRandom.nextInt(population.size() - elitistBound) + elitistBound);
            if (winner.compareTo(competitor) > 0) {
                winner = competitor;
            }
        }
        return winner.genome;
    }

    /**
     * Performs two-point (single-cut) crossover between parent genomes to produce two children,
     * which are appended to {@code newPopulation}.
     *
     * @param parent1       first parent genome
     * @param parent2       second parent genome
     * @param newPopulation destination list receiving offspring as {@link GenomeFitness}
     */
    private void crossover(Genome parent1, Genome parent2, List<GenomeFitness> newPopulation) {
        int cutoff = new Random().nextInt(Genome.getGenomeLength() + 1);

        byte[] child1 = new byte[Genome.getGenomeLength()];
        System.arraycopy(parent1.getGenome(), 0, child1, 0, cutoff);
        System.arraycopy(parent2.getGenome(), cutoff, child1, cutoff, Genome.getGenomeLength() - cutoff);

        byte[] child2 = new byte[Genome.getGenomeLength()];
        System.arraycopy(parent2.getGenome(), 0, child2, 0, cutoff);
        System.arraycopy(parent1.getGenome(), cutoff, child2, cutoff, Genome.getGenomeLength() - cutoff);

        newPopulation.add(new GenomeFitness(new Genome(child1)));
        newPopulation.add(new GenomeFitness(new Genome(child2)));
    }

    /**
     * Mutates a subset of the population (excluding the elite fraction) using the configured probabilities.
     * The indices selected for mutation are sampled uniformly from the non-elite region.
     */
    private void mutate() {
        int populationToMutate = (int) (POPULATION_SIZE * GENOME_MUTATION_PROBABILITY);
        int elitistBound = (int) (POPULATION_SIZE * ELITIST_SHARE);
        Random mutateRandom = new Random();

        for (int i = 0; i < populationToMutate; i++) {
            this.population.get(mutateRandom.nextInt(population.size() - elitistBound) + elitistBound).genome.mutate(MUTATION_RANGE, GEN_MUTATION_PROBABILITY);
        }
    }

    /**
     * Benchmarks the currently fittest genome (population.get(0)) against up to three preloaded benchmark genomes.
     * Results are persisted to the stats file via {@link #logFittest(String)}.
     */
    private void benchmarkFittest() {
        Genome fittestGenome = population.getFirst().genome;
        String benchmark1result = benchmarkGame(benchmark1);
        String benchmark2result = benchmarkGame(benchmark2);
        String benchmark3result = benchmarkGame(benchmark3);

        String log = String.format("%d | %s | %s | %s | %s;",
                generationNr,
                fittestGenome.getEncodedGenome(),
                benchmark1result,
                benchmark2result,
                benchmark3result
        );
        logFittest(log);
    }

    /**
     * Runs a series of benchmark games of the fittest genome against a provided opponent genome.
     * Uses {@link GameRules} to instantiate games and counts wins/losses/draws across {@code NR_BENCHMARK_GAMES}.
     *
     * @param opponent the opponent genome to benchmark against; may be {@code null} in which case benchmarking returns zeroed summary
     * @return a formatted summary string containing played, won, lost, win ratio and draw ratio
     */
    private String benchmarkGame(Genome opponent) {
        Genome fittestGenome = population.getFirst().genome;

        GameRules game = new GameRules(fittestGenome, opponent);
        int played = 0, won = 0, lost = 0;

        for (int i = 0; i < NR_BENCHMARK_GAMES; i++) {
            AIPlayer hasWon = (AIPlayer) game.run();
            played++;

            if (Objects.nonNull(hasWon)) {
                if (hasWon.getGenome().getPopulationID() == fittestGenome.getPopulationID()) {
                    won++;
                } else {
                    lost++;
                }
            }
            game.newGame();
        }
        return String.format("%d | %d | %d | %f | %f",
                played,
                won,
                lost,
                (double) won / (won + lost),
                (double) (played - won - lost) / played
        );
    }



    /**
     * Container holding a genome and its aggregated match statistics.
     * Implements {@link Comparable} so that collections can be sorted by descending fitness.
     */
    private class GenomeFitness implements Comparable<GenomeFitness> {

        private Genome genome;

        private int gamesPlayed;
        private int gamesWon;
        private int gamesLost;

        private GenomeFitness(Genome genome) {
            this.genome = genome;
            this.gamesPlayed = 0;
            this.gamesWon = 0;
            this.gamesLost = 0;
        }

        /**
         * Computes a normalized fitness value based on win and draw ratios:
         * fitness = winRate + drawRate / 2.
         * The value is returned as a double between 0 and 1 (approximately).
         *
         * @return computed fitness
         */
        private double getFitness() {
            double winRate = (double) gamesWon / gamesPlayed;
            double drawRate = (double) (gamesPlayed - gamesWon - gamesLost) / gamesPlayed;
            int fitness = (int) ((winRate + drawRate / 2) * 1000);
            return fitness / 1000d;
        }

        /**
         * Compare two GenomeFitness instances so that higher fitness sorts first.
         * The comparison scales fitness values to integers to avoid floating point ordering issues.
         *
         * @param o other GenomeFitness instance
         * @return negative if this is fitter than o, positive if less fit
         */
        @Override
        public int compareTo(GenomeFitness o) {
            return (int) (4096 * o.getFitness() - 4096 * this.getFitness());
        }

        @Override
        public String toString() {
            return String.format("%s, wins: %d, lost: %d, played: %d", genome, gamesWon, gamesLost, gamesPlayed);
        }
    }



    /**
     * Thread that executes a series of fitness games for a single {@link GenomeFitness} instance.
     * Each training session pairs the main genome with randomly selected competing genomes from the population.
     */
    private class TrainingSession extends Thread {

        private final GenomeFitness mainGenome;

        private TrainingSession(GenomeFitness mainGenome) {
            this.mainGenome = mainGenome;
        }

        /**
         * Runs NR_FITNESS_GAMES matches where the main genome alternates starting positions
         * against randomly selected opponents from the population. Updates gamesPlayed, gamesWon,
         * and gamesLost counters for both participants.
         */
        @Override
        public void run() {
            for (int i = 1; i <= NR_FITNESS_GAMES; i++) {
                GenomeFitness competingGenome = population.get(new Random().nextInt(population.size()));
                //GenomeFitness competingGenome = new GenomeFitness(benchmark3);


                GameRules game;
                if (i % 2 == 0) {
                    game = new GameRules(mainGenome.genome, competingGenome.genome);
                } else {
                    game = new GameRules(competingGenome.genome, mainGenome.genome);
                }

                AIPlayer hasWon = (AIPlayer) game.run();
                mainGenome.gamesPlayed++;
                competingGenome.gamesPlayed++;

                if (Objects.nonNull(hasWon)) {
                    if (hasWon.getGenome().getPopulationID() == mainGenome.genome.getPopulationID()) {
                        mainGenome.gamesWon++;
                        competingGenome.gamesLost++;
                    } else {
                        competingGenome.gamesWon++;
                        mainGenome.gamesLost++;
                    }
                }
            }
        }
    }



    /**
     * Reads the starting generation number from the generationSave file.
     * If reading fails the method returns -1.
     *
     * @return parsed starting generation or -1 on error
     */
    public int getStartingGeneration() {
        try {
            return Integer.parseInt(Files.readString(generationSave.toPath()));
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Persists the current generation number to {@link #generationSave}.
     * Uses a BufferedWriter in try-with-resources to ensure proper closure.
     */
    private void setStartingGeneration() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(generationSave))) {
            writer.write(Integer.toString(this.generationNr));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Appends the encoded fittest genome of the current generation to the configured fittest selection file.
     */
    private void saveFittest() {
        appendToFile(fittestSelection, population.getFirst().genome.getEncodedGenome());
    }

    /**
     * Logs the fittest genome's benchmark summary to console and appends it to the stats file.
     *
     * @param log formatted log line
     */
    private void logFittest(String log) {
        System.out.printf("%s%n", log);
        appendToFile(stats, log);
    }

    /**
     * Appends a line to the specified file. The method writes a newline then the supplied string.
     * Uses try-with-resources to close the writer and prints stack traces on IOExceptions.
     *
     * @param file target file to append to
     * @param s    content to append
     */
    private void appendToFile(File file, String s) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.newLine();
            writer.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
