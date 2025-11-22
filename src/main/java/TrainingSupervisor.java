import player.ai.genetic.TrainingGround;

import java.util.Scanner;

/**
 * Supervises the execution of a {@link TrainingGround} instance by listening for user input
 * in a separate thread and signalling an interrupt when requested.
 *
 * <p>
 * This class is intended to run in parallel to the training process. It blocks on standard
 * input until the user presses Enter. Once input is received, it calls
 * {@link TrainingGround#flagInterrupt()} to request a controlled stop of the training.
 * </p>
 */
public class TrainingSupervisor extends Thread {

    TrainingGround trainingGround;

    public TrainingSupervisor(TrainingGround trainingGround) {
        this.trainingGround = trainingGround;
    }

    /**
     * Waits for a single line of input from standard input and then signals
     * the associated {@link TrainingGround} to interrupt its execution.
     *
     * <p>
     * This method blocks until the user presses Enter. After the input is received,
     * {@link TrainingGround#flagInterrupt()} is invoked to trigger a controlled
     * interruption of the running training process.
     * </p>
     */
    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        trainingGround.flagInterrupt();
    }
}
