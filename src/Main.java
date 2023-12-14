import java.time.Duration;

public class Main {
    public static void main(String[] args) {
        SimulationController simulationController = new SimulationController(123455, 360, Duration.ofDays(7));
        simulationController.simulate();
        System.out.println(simulationController.getReport());
    }
}