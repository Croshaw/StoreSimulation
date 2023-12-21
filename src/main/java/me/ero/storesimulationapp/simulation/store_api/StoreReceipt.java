package me.ero.storesimulationapp.simulation.store_api;


import me.ero.storesimulationapp.simulation.store_api.human.Buyer;
import me.ero.storesimulationapp.simulation.store_api.util.DurationUtils;
import me.ero.storesimulationapp.simulation.store_api.util.Pair;

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Покупатель: %s\n".formatted(buyer.toString()));
        sb.append("Время обслуживания: %s\n".formatted(DurationUtils.toString(durationService)));
        sb.append("Текущее время обслуживания: %s\n".formatted(DurationUtils.toString(currentDurationService)));
        sb.append("Время ожидания: %s\n".formatted(DurationUtils.toString(waitingDuration)));
        sb.append("Цена: %.2f\n".formatted(getTotalPrice()));
        sb.append("Продукты: ");
        short s = 0;
        for(var product : products) {
            if(s == 5) {
                s = 0;
                sb.append("\n");
            }
            sb.append("%s %d шт., ".formatted(product.getFirst().getName(), product.getSecond()));
            s++;
        }
        sb.deleteCharAt(sb.length()-1);
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }
}
