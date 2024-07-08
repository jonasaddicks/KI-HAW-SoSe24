package player.ai.genetic;

import java.util.ArrayList;
import java.util.List;

import static player.ai.genetic.TrainingProperties.POPULATION_SIZE;

public class TrainingGround {

    private TrainingGround() {}

    public static void initTraining(Genome genome) {
        List<Genome> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(new Genome(genome));
        }
    }
}
