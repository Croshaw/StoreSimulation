package me.ero.storesimulationapp.simulation.store_api.human;

import me.ero.storesimulationapp.simulation.store_api.util.DurationUtils;

import java.time.Duration;

public class Salesman extends Employee {
    private final Duration durationOfHire;
    public Salesman(String surname, String name, String patronymic, double dailySalary, Duration durationOfHire) {
        super(surname, name, patronymic, dailySalary);
        this.durationOfHire = durationOfHire;
    }
    public boolean shouldBeFire() {
        return super.getCurrentDuration().compareTo(durationOfHire) >=0;
    }

    @Override
    public String toString() {
        return super.toString() + "\nОсталость работать: %s".formatted(DurationUtils.toStringWithDay(durationOfHire.minus(getCurrentDuration())));
    }
}
