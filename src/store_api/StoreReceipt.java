package store_api;

import store_api.human.Buyer;
import store_api.util.Pair;

import java.time.Duration;
import java.util.ArrayList;

public class StoreReceipt {
    private double fixitPrice;
    private final Buyer buyer;
    private final Duration durationService;
    private Duration waitingDuration;
    private Duration currentDurationService;
    private final ArrayList<Pair<Product, Integer>> products;
    public StoreReceipt(Buyer buyer, Duration durationService) {
        this(buyer, durationService, new ArrayList<>());
    }
    public StoreReceipt(Buyer buyer, Duration durationService, ArrayList<Pair<Product, Integer>> products) {
        this.buyer = buyer;
        this.durationService = durationService;
        currentDurationService = Duration.ZERO;
        this.products = products;
        waitingDuration = Duration.ZERO;
        fixitPrice = -1;
    }
    public Buyer getBuyer() {
        return buyer;
    }
    public long life(long secondsStep) {
        if(currentDurationService.compareTo(Duration.ZERO) == 0)
            fixPrice();
        currentDurationService = currentDurationService.plusSeconds(secondsStep);
        if(currentDurationService.compareTo(durationService) > 0) {
            long dif = currentDurationService.minus(durationService).toSeconds();
            currentDurationService = durationService;
            return dif;
        }
        currentDurationService = currentDurationService.plusSeconds(secondsStep);
        return 0;
    }
    public void waiting(long secondsStep) {
        if(currentDurationService.getSeconds() == 0 && secondsStep > 0)
            waitingDuration = waitingDuration.plusSeconds(secondsStep);
    }
    public void addProduct(Product product) {
        for(var p : products) {
            if(p.getFirst() == product) {
                p.setSecond(p.getSecond()+1);
                return;
            }
        }
        products.add(new Pair<>(product, 1));
    }
    public double getTotalPrice() {
        if(fixitPrice > 0) {
            return fixitPrice;
        }
        double price = 0;
        for(var p : products) {
            price += p.getFirst().getFinalPrice() * p.getSecond();
        }
        return price;
    }
    private void fixPrice() {
        fixitPrice = 0;
        for(var p : products) {
            fixitPrice += p.getFirst().getFinalPrice() * p.getSecond();
        }
    }
    public Duration getSeviceDuration() {
        return durationService;
    }
    public Duration getWaitingDuration() {
        return waitingDuration;
    }
    public boolean isDone() {
        return currentDurationService.compareTo(durationService) == 0;
    }
}
