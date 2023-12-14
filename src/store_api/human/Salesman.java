package store_api.human;

import store_api.Removable;

import java.time.Duration;
import java.time.LocalDateTime;

public class Salesman extends Employee {
    private final Duration durationOfHire;
    private final Removable callback;
    public Salesman(String surname, String name, String patronymic, Duration durationOfHire, Removable callback) {
        super(surname, name, patronymic);
        this.durationOfHire = durationOfHire;
        this.callback = callback;
    }
    public boolean shouldBeFire() {
        return super.getCurrentDuration().compareTo(durationOfHire) >=0;
    }
}
