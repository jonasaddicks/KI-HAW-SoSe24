package player.ai.genetic;

public class TrainingProperties {
    public static final int POPULATION_SIZE = 500;
    public static final int NR_BENCHMARK_GAMES = 300;
    public static final int NR_FITNESS_GAMES = 300;
    public static final int NR_GENERATIONS = 1000;

    public static final double ELITIST_SHARE = 0.1;
    public static final int TOURNAMENT_SIZE = 5;

    public static final double GENOME_MUTATION_PROBABILITY = 0.8;
    public static final double GEN_MUTATION_PROBABILITY = 0.2;
    public static final int MUTATION_RANGE = 2;
}
