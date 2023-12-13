package store_api.store;

import store_api.human.Cashier;
import store_api.human.Employee;
import store_api.util.Pair;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;

public class SuperStore extends Store {

    public SuperStore(HashMap<DayOfWeek, Pair<LocalTime, Duration>> workSchedule, int maxQueueLength, int maxDifferenceBetweenMaxAndMinQueue, double costAdvertising) {
        super(workSchedule, maxQueueLength, maxDifferenceBetweenMaxAndMinQueue, costAdvertising);
    }
}
