package player.ai.genetic;

public class TrainingProperties {
    public static final int POPULATION_SIZE = 100;
    public static final int NR_BENCHMARK_GAMES = 20;
    public static final int NR_FITNESS_GAMES = 20;
    public static final int NR_GENERATIONS = 10;

    public static final double ELITIST_SHARE = 0.1;
    public static final int TOURNAMENT_SIZE = 5;

    public static final double GENOME_MUTATION_PROBABILITY = 0.4;
    public static final double GEN_MUTATION_PROBABILITY = 0.1;
    public static final int MUTATION_RANGE = 7;
}
