package me.ero.storesimulationapp.simulation.store_api.human;

import java.time.Duration;

public class Salesman extends Employee {
    private final Duration durationOfHire;
    public Salesman(String surname, String name, String patronymic, Duration durationOfHire) {
        super(surname, name, patronymic);
        this.durationOfHire = durationOfHire;
    }
    public boolean shouldBeFire() {
        return super.getCurrentDuration().compareTo(durationOfHire) >=0;
    }
}
