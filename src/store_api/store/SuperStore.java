package store_api.store;

import store_api.util.Pair;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;

public class SuperStore extends Store {

    public SuperStore(HashMap<DayOfWeek, Pair<LocalTime, Duration>> workSchedule, int maxQueueLength, double costAdvertising) {
        super(workSchedule, maxQueueLength, costAdvertising);
    }

    @Override
    public String toString() {
        return "Супермаркет\n" + super.toString();
    }
}
