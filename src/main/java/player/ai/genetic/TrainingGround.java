package player.ai.genetic;

import game.GameRules;
import player.ai.AIPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static player.ai.genetic.TrainingProperties.NR_FITNESS_GAMES;
import static player.ai.genetic.TrainingProperties.POPULATION_SIZE;

public class TrainingGround {

    private List<GenomeFitness> population;



    public TrainingGround(Genome fittestGenome) {
        this.population = new ArrayList<>();
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
        evaluateFitness();
        for (GenomeFitness genomeFitness : population) {
            //TODO debug
            System.out.printf("%s --- %f%n", genomeFitness, genomeFitness.getFitness());
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
                //TODO debug
                System.out.printf("%d starting game %d%n", mainGenome.genome.getPopulationID(), i);
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
            //TODO debug
            System.out.printf("%d finished%n", mainGenome.genome.getPopulationID());
        }
    }
}
