package me.ero.storesimulationapp.simulation.store_api.util;

import java.time.Duration;

public class DurationUtils {
    public static String toString(Duration duration) {
        return String.format("%02d:%02d:%02d",
                duration.toHours(),
                duration.toMinutesPart(),
                duration.toSecondsPart());
    }
    public static String toStringWithDay(Duration duration) {
        return String.format("%02d:%02d:%02d:%02d",
                duration.toDays(),
                duration.toHours() - duration.toDays()*24,
                duration.toMinutesPart(),
                duration.toSecondsPart());
    }
}
