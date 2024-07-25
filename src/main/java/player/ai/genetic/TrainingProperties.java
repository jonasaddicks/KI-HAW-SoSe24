package player.ai.genetic;

public class TrainingProperties {
    public static final int POPULATION_SIZE = 500;
    public static final int NR_BENCHMARK_GAMES = 300;
    public static final int NR_FITNESS_GAMES = 300;
    public static final int NR_GENERATIONS = 10000;

    public static final double ELITIST_SHARE = 0.05;
    public static final int TOURNAMENT_SIZE = 9;

    public static final double GENOME_MUTATION_PROBABILITY = 0.5;
    public static final double GEN_MUTATION_PROBABILITY = 0.15;
    public static final int MUTATION_RANGE = 4;
}
