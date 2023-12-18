package me.ero.storesimulationapp.simulation.store_api.store;

import javafx.animation.AnimationTimer;
import javafx.animation.TranslateTransition;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import me.ero.storesimulationapp.simulation.store_api.Product;
import me.ero.storesimulationapp.simulation.store_api.StoreReceipt;
import me.ero.storesimulationapp.simulation.store_api.human.Employee;
import me.ero.storesimulationapp.simulation.store_api.human.Salesman;
import me.ero.storesimulationapp.simulation.store_api.util.DurationUtils;
import me.ero.storesimulationapp.simulation.store_api.util.Pair;

import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Store {
    private final HashMap<DayOfWeek, Pair<LocalTime, Duration>> workSchedule;
    private final LocalDateTime createdDateTime;
    private Duration currentDuration;
    private final HashSet<Employee> employees;
    private final ArrayList<Employee> fireEmployees;
    private final HashSet<Product> products;
    private final HashMap<LocalDate, Integer> numbersOfLostBuyers;
    private final int maxQueueLength;
    private float reputation;
    private final double costAdvertising;
    private int countAdvertising;
    private final int maxEmployeeCount;
    public Store(HashMap<DayOfWeek, Pair<LocalTime, Duration>> workSchedule, int maxQueueLength, double costAdvertising, int maxEmployeeCount) {
        this.workSchedule = workSchedule;
        employees = new HashSet<>();
        fireEmployees = new ArrayList<>();
        this.maxQueueLength = maxQueueLength;
        createdDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(0,0,0,0));
        currentDuration = Duration.ZERO;
        products = new HashSet<>();
        reputation = 1f;
        this.costAdvertising = costAdvertising;
        countAdvertising = 0;
        numbersOfLostBuyers = new HashMap<>();
        this.maxEmployeeCount = maxEmployeeCount;
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
            if(!product.isDiscount())
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
            return minEmployee.addToQueue(storeReceipt, getCurrentDate());
        else {
            if(reputation > .2f)
                reputation -= .2f;
            if(!numbersOfLostBuyers.containsKey(getCurrentDate()))
                numbersOfLostBuyers.put(getCurrentDate(), 0);
            numbersOfLostBuyers.put(getCurrentDate(), numbersOfLostBuyers.remove(getCurrentDate())+1);
        }
        return false;
    }
    public void work(long secondsStep) {
        currentDuration = currentDuration.plusSeconds(secondsStep);
        if(isWork()) {
            ArrayList<Employee> shouldBeFire = new ArrayList<>();
            for (var employee : employees) {
                if(employee instanceof Salesman salesman)
                    if(salesman.shouldBeFire()) {
                        shouldBeFire.add(salesman);
                        continue;
                    }
                employee.work(secondsStep, getCurrentDateTime());
            }
            shouldBeFire.forEach(this::fire);
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
    private int getTotalNumberServedBuyers() {
        int c = 0;
        for(var employee : employees)
            c += employee.getTotalNumberServedBuyers();
        for(var employee : fireEmployees)
            c += employee.getTotalNumberServedBuyers();
        return c;
    }
    private int getTotalNumberLostBuyers() {
        int c = 0;
        for(var temp : numbersOfLostBuyers.values())
            c += temp;
        return c;
    }
    public int getAverageQueuesLength() {
        int avg = 0;
        if(!employees.isEmpty()) {
            for (var employee : employees)
                avg += employee.getAverageQueueLength();
            avg /= employees.size();
        }
        if(!fireEmployees.isEmpty()) {
            int avg2 = 0;
            for (var employee : fireEmployees)
                avg2 += employee.getAverageQueueLength();
            avg2 /= fireEmployees.size();
            return (avg+avg2)/2;
        }
        return avg;
    }
    protected int getFireEmployeesCount() {
        return fireEmployees.size();
    }
    public Duration getAverageWaitingDurationInQueues() {
        Duration avgDuration = Duration.ZERO;
        int count = 0;
        for(var employee : employees) {
            Duration temp = employee.getAverageWaitingDurationInQueue();
            if(temp.compareTo(Duration.ZERO) == 0)
                continue;
            avgDuration = avgDuration.plus(temp);
            count++;
        }
        for(var employee : fireEmployees) {
            Duration temp = employee.getAverageWaitingDurationInQueue();
            if(temp.compareTo(Duration.ZERO) == 0)
                continue;
            avgDuration = avgDuration.plus(temp);
            count++;
        }

        if(count > 0)
            avgDuration = avgDuration.dividedBy(count);

        return avgDuration;
    }
    public Duration getAverageTimeSpent() {
        Duration avgDuration = Duration.ZERO;
        int count = 0;
        for(var employee : employees) {
            Duration temp = employee.getAverageTimeSpent();
            if(temp.compareTo(Duration.ZERO) == 0)
                continue;
            avgDuration = avgDuration.plus(temp);
            count++;
        }
        for(var employee : fireEmployees) {
            Duration temp = employee.getAverageTimeSpent();
            if(temp.compareTo(Duration.ZERO) == 0)
                continue;
            avgDuration = avgDuration.plus(temp);
            count++;
        }

        if(count > 0)
            avgDuration = avgDuration.dividedBy(count);

        return avgDuration;
    }
    public HashSet<Employee> getEmployees() {
        return employees;
    }
    public int getMaxEmployeesCount() {
        return 7;
    }
    public int getMaxQueueLength() {
        return maxQueueLength;
    }
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Кол-во обслуженных покупателей за всё время: %d\n".formatted(getTotalNumberServedBuyers()));

        stringBuilder.append("Кол-во потерянных покупателей за всё время: %d\n".formatted(getTotalNumberLostBuyers()));

        stringBuilder.append("Средняя длина очереди: %d\n".formatted(getAverageQueuesLength()));
        //stringBuilder.append("Среднее время обслуживания: %s\n".formatted(getAverageServiceDuration()));

        stringBuilder.append("Среднее время ожидания в очереди: %s\n".formatted(DurationUtils.toString(getAverageWaitingDurationInQueues())));

        stringBuilder.append("Средняя занятость персонала: %s\n".formatted(DurationUtils.toString(getAverageTimeSpent())));

        stringBuilder.append("Затраты на рекламу: %.2f руб.\n".formatted(costAdvertising*countAdvertising));

        stringBuilder.append("Общая прибыль: %.2f руб.\n".formatted(getTotalProfit()));

        return stringBuilder.toString();
    }
}
