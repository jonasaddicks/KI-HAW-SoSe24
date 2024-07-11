package player.ai.genetic;

public class TrainingProperties {
    public static final int POPULATION_SIZE = 1000;
    public static final int NR_BENCHMARK_GAMES = 100;
    public static final int NR_FITNESS_GAMES = 100;
    public static final int NR_GENERATIONS = 1000;

    public static final double ELITIST_SHARE = 0.05;
    public static final int TOURNAMENT_SIZE = 10;

    public static final double GENOME_MUTATION_PROBABILITY = 0.4;
    public static final double GEN_MUTATION_PROBABILITY = 0.05;
    public static final int MUTATION_RANGE = 3;
}