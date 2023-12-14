package store_api.store;

import store_api.util.Pair;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;

public class SimpleStore extends Store {
    public SimpleStore(HashMap<DayOfWeek, Pair<LocalTime, Duration>> workSchedule, double costAdvertising) {
        super(workSchedule, 3, costAdvertising);
    }

    @Override
    public String toString() {
        return "Магазин\n" + super.toString() + "Кол-во уволенных сотрудников: %d".formatted(getFireEmployeesCount());
    }
}
