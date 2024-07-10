import player.ai.genetic.TrainingGround;

import java.util.Scanner;

public class TrainingSupervisor extends Thread {

    TrainingGround trainingGround;

    public TrainingSupervisor(TrainingGround trainingGround) {
        this.trainingGround = trainingGround;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        trainingGround.flagInterrupt();
    }
}
