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

    private final File stats;
    private final File generationSave;

    private List<GenomeFitness> population;
    private int generationNr;


    public TrainingGround(Genome fittestGenome, File stats, File generationSave) {
        this.stats = stats;
        this.generationSave = generationSave;

        this.population = new ArrayList<>();
        this.generationNr = getStartingGeneration() + 1;
        initTraining(fittestGenome);
    }

    public void train() {
        runTraining();
    }

    private void initTraining(Genome fittestGenome) {
        for (int i = 0; i < POPULATION_SIZE; i++) {
            this.population.add(new GenomeFitness(new Genome(fittestGenome)));
        }
    }

    private void runTraining() {
        int maxGeneration = generationNr + NR_GENERATIONS;
        for (int i = generationNr; i < maxGeneration; i++) {

            evaluateFitness(); //simulated games
            //TODO debug
            for (GenomeFitness f : population) {
                System.out.printf("%s - %f%n", f, f.getFitness());
            }

            //TODO save fittest
            //TODO benchmark fittest
            //TODO log fittest
            System.out.printf("%ngen %d --- %s%n%n", this.generationNr, population.getFirst().genome.getEncodedGenome());
//            if (stop) {
//                generationNr++;
//                break;
//            }

            selectFirstStage(); //elitist
            selectSecondStage(); //tournament and crossover
            mutate();

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

    private void selectSecondStage() {
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
        //TODO
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
            return (double) gamesWon / (double) (gamesLost + gamesWon);
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
}
