package me.ero.storesimulationapp.simulation.store_api.store;

import javafx.scene.layout.Pane;
import me.ero.storesimulationapp.simulation.store_api.util.Pair;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;

public class SimpleStore extends Store {
    public SimpleStore(HashMap<DayOfWeek, Pair<LocalTime, Duration>> workSchedule, double costAdvertising, int maxEmployeeCount) {
        super(workSchedule, 3, costAdvertising, maxEmployeeCount);
    }

    @Override
    public String toString() {
        return "Магазин\n" + super.toString() + "Кол-во уволенных сотрудников: %d".formatted(getFireEmployeesCount());
    }
}
