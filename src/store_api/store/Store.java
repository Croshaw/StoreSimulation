package store_api.store;

import store_api.Product;
import store_api.StoreReceipt;
import store_api.human.Employee;
import store_api.util.Pair;

import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class Store {
    private final HashMap<DayOfWeek, Pair<LocalTime, Duration>> workSchedule;
    private final LocalDateTime createdDateTime;
    private Duration currentDuration;
    private final HashSet<Employee> employees;
    private final ArrayList<Employee> fireEmployees;
    private final HashSet<Product> products;
    private final int maxQueueLength;
    private final int maxDifferenceBetweenMaxAndMinQueue;
    private float reputation;
    private final double costAdvertising;
    private int countAdvertising;
    public Store(HashMap<DayOfWeek, Pair<LocalTime, Duration>> workSchedule, int maxQueueLength, int maxDifferenceBetweenMaxAndMinQueue, double costAdvertising) {
        this.workSchedule = workSchedule;
        employees = new HashSet<>();
        fireEmployees = new ArrayList<>();
        this.maxQueueLength = maxQueueLength;
        this.maxDifferenceBetweenMaxAndMinQueue = maxDifferenceBetweenMaxAndMinQueue;
        createdDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(0,0,0,0));
        currentDuration = Duration.ZERO;
        products = new HashSet<>();
        reputation = 1f;
        this.costAdvertising = costAdvertising;
        countAdvertising = 0;
            }
    public boolean startAdvertising() {
        if(getTotalProfit() - (countAdvertising+1)*costAdvertising > 0){
            countAdvertising++;
            reputation += .1f;
            if(reputation > 1f)
                reputation = 1f;
            return true;
        }
        return false;
    }
    public void startDiscounts(Random random) {
        Duration duration = Duration.ofMinutes(random.nextLong(6*60, 3*24*60));
        int countOfProducts = random.nextInt(1, products.size()/2);
        ArrayList<Product> products1 = new ArrayList<>(products);
        for(int i = 0; i < countOfProducts; i++) {
            Product product = products1.get(random.nextInt(0, products.size()));
            if(product.isDiscount())
                i--;
            else
                product.setDiscount(1f - (random.nextFloat() % .5f), duration);
        }
    }
    public boolean hire(Employee employee) {
        return employees.add(employee);
    }
    public boolean fire(Employee employee) {
        employees.remove(employee);
        employee.freeQueue(getCurrentDate());
        employee.finalize(getCurrentDateTime());
        fireEmployees.add(employee);
        return true;
    }
    public boolean addToQueue(StoreReceipt storeReceipt) {
        Employee minEmployee = getEmployeeWithMinQueueLength();
        if(minEmployee != null && minEmployee.getQueueLength() < maxQueueLength)
            return minEmployee.addToQueue(storeReceipt);
        else {
            if(reputation > .2f)
                reputation -= .2f;
        }
        return false;
    }
    public void work(long secondsStep) {
        currentDuration = currentDuration.plusSeconds(secondsStep);
        if(isWork()) {
            for (var employee : employees)
                employee.work(secondsStep, getCurrentDateTime());
        } else {
            for (var employee : employees)
                employee.finalize(getCurrentDateTime());
        }
    }
    private Employee getEmployeeWithMinQueueLength() {
        Employee minEmployee = null;
        for(var employee : employees) {
            if(minEmployee == null || minEmployee.getQueueLength() > employee.getQueueLength())
                minEmployee = employee;
        }
        return minEmployee;
    }
    private Employee getEmployeeWithMaxQueueLength() {
        Employee maxEmployee = null;
        for(var employee : employees) {
            if(maxEmployee == null || maxEmployee.getQueueLength() < employee.getQueueLength())
                maxEmployee = employee;
        }
        return maxEmployee;
    }
    public void iterDiscounts(long secondsStep) {
        for(var product : products)
            product.life(secondsStep);
    }
    public boolean isWork() {
        return isWork(createdDateTime.plus(currentDuration));
    }
    public boolean isWork(LocalDateTime dateTime) {
        var pair = workSchedule.get(dateTime.getDayOfWeek());
        return dateTime.toLocalTime().isAfter(pair.getFirst()) && dateTime.toLocalTime().isBefore(pair.getFirst().plus(pair.getSecond()));
    }
    public LocalDateTime getCurrentDateTime() {
        return createdDateTime.plus(currentDuration);
    }
    public LocalDate getCurrentDate() {
        return getCurrentDateTime().toLocalDate();
    }
    public float getReputation() {
        float curRep = reputation;
        for(var product : products) {
            if(curRep >= 1f)
                break;
            curRep += (1-product.getPriceMultiplier())*50;
        }
        if(curRep > 1)
            curRep = 1;
        return curRep;
    }
    public boolean addProduct(Product product) {
        return products.add(product);
    }
    public HashSet<Product> getProducts() {
        return products;
    }
    public double getTotalProfit() {
        double profit = 0;
        for(var employee : employees)
            profit += employee.getTotalProfit();
        for(var employee : fireEmployees)
            profit += employee.getTotalProfit();
        return profit;
    }
    public int getCountOfEmployees() {
        return employees.size();
    }
    public Duration getCurrentDuration() {
        return currentDuration;
    }
}
