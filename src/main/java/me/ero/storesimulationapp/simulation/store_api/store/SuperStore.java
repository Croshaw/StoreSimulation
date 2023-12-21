package me.ero.storesimulationapp.simulation.store_api.store;

public class SuperStore extends Store {

    public SuperStore(int maxQueueLength, int maxEmployeeCount, double costAdvertising) {
        super(maxQueueLength, costAdvertising, maxEmployeeCount);
    }

    @Override
    public String toString() {
        return "Супермаркет\n" + super.toString();
    }
}
