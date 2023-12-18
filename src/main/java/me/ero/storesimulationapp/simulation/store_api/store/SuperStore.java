package me.ero.storesimulationapp.simulation.store_api.store;

import javafx.scene.layout.Pane;
import me.ero.storesimulationapp.simulation.store_api.util.Pair;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;

public class SuperStore extends Store {

    public SuperStore(HashMap<DayOfWeek, Pair<LocalTime, Duration>> workSchedule, int maxQueueLength, int maxEmployeeCount, double costAdvertising) {
        super(workSchedule, maxQueueLength, costAdvertising, maxEmployeeCount);
    }

    @Override
    public String toString() {
        return "Супермаркет\n" + super.toString();
    }
}
