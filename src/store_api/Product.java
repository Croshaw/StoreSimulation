package store_api;

import java.time.Duration;
import java.util.Random;

public class Product {
    private final String name;
    private double price;
    private float priceMultiplier;
    private Duration durationDiscount;
    private Duration currentDurationDiscount;
    public Product(String name, double price) {
        this(name, price, 1f);
    }
    public Product(String name, double price, float priceMultiplier) {
        if(name.isBlank() || name.isEmpty())
            throw new RuntimeException("Name must not be empty or blank");
        this.name = name;
        setPrice(price);
        setPriceMultiplier(priceMultiplier);
    }
    public String getName() {
        return name;
    }
    public double getFinalPrice() {
        return price * priceMultiplier;
    }
    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        if(price <= 0)
            throw new RuntimeException("Price must be greater then 0");
        this.price = price;
    }
    public float getPriceMultiplier() {
        return priceMultiplier;
    }
    private void setPriceMultiplier(float priceMultiplier) {
        if(priceMultiplier > 1f || priceMultiplier <= 0)
            throw new RuntimeException("The price multiplier must be in range (0;1]");
        this.priceMultiplier = priceMultiplier;
    }
    public void setDiscount(float priceMultiplier, Duration durationDiscount) {
        setPriceMultiplier(priceMultiplier);
        this.durationDiscount = durationDiscount;
        this.currentDurationDiscount = Duration.ZERO;
    }
    public void life(long secondsStep) {
        if(currentDurationDiscount != null && durationDiscount!= null) {
            currentDurationDiscount = currentDurationDiscount.plusSeconds(secondsStep);
            if (currentDurationDiscount.compareTo(durationDiscount) > 0)
                resetDiscount();
        }
    }
    public void resetDiscount() {
        setPriceMultiplier(1f);
        durationDiscount = null;
        currentDurationDiscount = null;
    }
    public boolean isDiscount() {
        return durationDiscount != null;
    }
    public static Product getProductFromString(String string, Random random) {
        String[] strs = string.split(" ");
        StringBuilder name = new StringBuilder();
        for(int i = 0; i < strs.length-1;i++) {
            name.append(strs[i]);
            if(i != strs.length-1)
                name.append(" ");
        }
        strs = strs[strs.length-1].split("-");
        double priceFrom = Double.parseDouble(strs[0]);
        double priceTo = Double.parseDouble(strs[1]);
        return new Product(name.toString(), random.nextDouble(Math.min(priceFrom, priceTo), Math.max(priceFrom, priceTo) + 1));
    }
}
