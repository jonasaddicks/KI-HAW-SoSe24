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

    public void flagInterrupt() {
        this.interruptFlag = true;
    }

    public void train() {
        runTraining();
    }

    private void initTraining(Genome fittestGenome) {
        for (int i = 0; i < POPULATION_SIZE; i++) {
            GenomeFitness newGenomeFitness = new GenomeFitness(new Genome(fittestGenome));
            newGenomeFitness.gamesWon++;
            newGenomeFitness.gamesLost++;
            newGenomeFitness.gamesPlayed += 2;
            this.population.add(newGenomeFitness);
        }
    }

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

    private void selectFirstStage() {
        int killShare = (int) (POPULATION_SIZE - POPULATION_SIZE * ELITIST_SHARE);
        for (int i = POPULATION_SIZE - 1; i >= killShare; i--) {
            population.set(i, population.get(i - killShare));
        }
    }

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

    private void mutate() {
        int populationToMutate = (int) (POPULATION_SIZE * GENOME_MUTATION_PROBABILITY);
        int elitistBound = (int) (POPULATION_SIZE * ELITIST_SHARE);
        Random mutateRandom = new Random();

        for (int i = 0; i < populationToMutate; i++) {
            this.population.get(mutateRandom.nextInt(population.size() - elitistBound) + elitistBound).genome.mutate(MUTATION_RANGE, GEN_MUTATION_PROBABILITY);
        }
    }

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

        private double getFitness() {
            double winRate = (double) gamesWon / gamesPlayed;
            double drawRate = (double) (gamesPlayed - gamesWon - gamesLost) / gamesPlayed;
            int fitness = (int) ((winRate + drawRate / 2) * 1000);
            return fitness / 1000d;
        }

        @Override
        public int compareTo(GenomeFitness o) {
            return (int) (4096 * o.getFitness() - 4096 * this.getFitness());
        }

        @Override
        public String toString() {
            return String.format("%s, wins: %d, lost: %d, played: %d", genome, gamesWon, gamesLost, gamesPlayed);
        }
    }



    private class TrainingSession extends Thread {

        private final GenomeFitness mainGenome;

        private TrainingSession(GenomeFitness mainGenome) {
            this.mainGenome = mainGenome;
        }

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



    public int getStartingGeneration() {
        try {
            return Integer.parseInt(Files.readString(generationSave.toPath()));
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void setStartingGeneration() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(generationSave))) {
            writer.write(Integer.toString(this.generationNr));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFittest() {
        appendToFile(fittestSelection, population.getFirst().genome.getEncodedGenome());
    }

    private void logFittest(String log) {
        System.out.printf("%s%n", log);
        appendToFile(stats, log);
    }

    private void appendToFile(File file, String s) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.newLine();
            writer.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
