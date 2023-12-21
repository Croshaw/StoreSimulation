package me.ero.storesimulationapp.simulation.store_api.store;

public class SimpleStore extends Store {
    public SimpleStore(double costAdvertising, int maxEmployeeCount) {
        super(3, costAdvertising, maxEmployeeCount);
    }

    @Override
    public String toString() {
        return "Магазин\n" + super.toString() + "\nКол-во уволенных сотрудников: %d".formatted(getFireEmployeesCount());
    }
}
