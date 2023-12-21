package me.ero.storesimulationapp.simulation.store_api.human;

import me.ero.storesimulationapp.simulation.store_api.StoreReceipt;
import me.ero.storesimulationapp.simulation.store_api.util.DurationUtils;
import me.ero.storesimulationapp.simulation.store_api.util.Pair;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class Employee extends People {
    private Duration currentDuration;
    private StoreReceipt currentTask;
    private final Map<LocalDate, Duration> timeSpentMap;
    private final Map<LocalDate, Double> salaryMap;
    private final Queue<StoreReceipt> currentStoreReceiptsQueue;
    private final Map<LocalDate, ArrayList<StoreReceipt>> completedStoreReceipts;
    private final Map<LocalDate, ArrayList<StoreReceipt>> dropStoreReceipts;
    private final Map<LocalDate, Pair<Integer,Integer>> averageQueueSize;
    private final double dailySalary;
    public Employee(String surname, String name, String patronymic, double dailySalary) {
        super(surname, name, patronymic);
        currentDuration = Duration.ZERO;
        timeSpentMap = new HashMap<>();
        salaryMap = new HashMap<>();
        currentStoreReceiptsQueue = new ArrayDeque<>();
        completedStoreReceipts = new HashMap<>();
        currentTask = null;
        averageQueueSize = new HashMap<>();
        dropStoreReceipts = new HashMap<>();
        this.dailySalary = dailySalary;
    }
    public boolean addToQueue(StoreReceipt storeReceipt, LocalDate date) {
        if(!currentStoreReceiptsQueue.contains(storeReceipt)) {
            boolean res = currentStoreReceiptsQueue.offer(storeReceipt);
            if(res) {
                if (!averageQueueSize.containsKey(date))
                    averageQueueSize.put(date, new Pair<>(9999, 0));
                var pair = averageQueueSize.get(date);
                if(pair.getFirst() > getQueueLength())
                    pair.setFirst(getQueueLength());
                if(pair.getSecond() < getQueueLength())
                    pair.setSecond(getQueueLength());
            }
            return res;
        }
        return false;
    }
    public void freeQueue(LocalDate date) {
        if(!dropStoreReceipts.containsKey(date))
            dropStoreReceipts.put(date, new ArrayList<>());
        var list = dropStoreReceipts.get(date);
        while(!currentStoreReceiptsQueue.isEmpty()) {
            list.add(currentStoreReceiptsQueue.poll());
        }
    }
    public void pay(LocalDate date) {
        if(!salaryMap.containsKey(date))
            salaryMap.put(date, 1500d);
    }
    public double getPaymentByDate(LocalDate date) {
        return salaryMap.getOrDefault(date, 0d);
    }
    public double getTotalSalary() {
        double totalSalary = 0;
        for(var key : salaryMap.keySet())
            totalSalary += getPaymentByDate(key);
        return totalSalary;
    }
    public void work(long secondsStep, LocalDateTime dateTime) {
        work(secondsStep, dateTime, false);
    }
    private void work(long secondsStep, LocalDateTime dateTime, boolean isFinalize) {
        if(currentTask == null && (currentStoreReceiptsQueue.isEmpty() || isFinalize)) {
            currentDuration = currentDuration.plusSeconds(secondsStep);
            return;
        }

        if(currentTask == null) {
            currentTask = currentStoreReceiptsQueue.poll();
        }
        long remainder = currentTask.life(secondsStep);
        currentDuration = currentDuration.plusSeconds(secondsStep - remainder);
        if(!currentStoreReceiptsQueue.isEmpty()) {
            for(var t : currentStoreReceiptsQueue)
                t.waiting(secondsStep - remainder);
        }

        if(!timeSpentMap.containsKey(dateTime.toLocalDate()))
            timeSpentMap.put(dateTime.toLocalDate(), Duration.ZERO);
        timeSpentMap.put(dateTime.toLocalDate(), timeSpentMap.get(dateTime.toLocalDate()).plusSeconds(secondsStep-remainder));

        if(currentTask.isDone()) {
            if(!completedStoreReceipts.containsKey(dateTime.toLocalDate()))
                completedStoreReceipts.put(dateTime.toLocalDate(), new ArrayList<>());
            completedStoreReceipts.get(dateTime.toLocalDate()).add(currentTask);
            currentTask = null;
        }
        if(remainder > 0)
            work(remainder, dateTime.plusSeconds(remainder));
    }
    public void finalize(LocalDateTime dateTime) {
        while(isServe()) {
            work(60, dateTime, true);
        }
        currentStoreReceiptsQueue.clear();
    }
    public int getQueueLength() {
        return currentStoreReceiptsQueue.size();
    }
    public boolean isServe() {
        return currentTask != null;
    }
    public int getTotalNumberServedBuyers() {
        int c = 0;
        for(var temp : completedStoreReceipts.values()) {
            c += temp.size();
        }
        return c;
    }
    public double getTotalProfitByDate(LocalDate date) {
        double profit = 0;
        if(!completedStoreReceipts.containsKey(date))
            return profit;
        for(var t : completedStoreReceipts.get(date))
                profit+=t.getTotalPrice()*0.9;
        return profit;
    }
    public double getTotalProfit() {
        double profit = 0;
        for(var date : completedStoreReceipts.keySet())
            profit += getTotalProfitByDate(date);
        return profit;
    }
    public Duration getAverageWaitingDurationInQueue() {
        Duration avgDuration = Duration.ZERO;
        int count = 0;
        for(var t : completedStoreReceipts.values()) {
            for(var temp : t) {
                if(temp.getWaitingDuration().compareTo(Duration.ZERO) == 0)
                    continue;
                avgDuration = avgDuration.plus(temp.getWaitingDuration());
                count++;
            }
        }
        if(count > 0)
            avgDuration = avgDuration.dividedBy(count);

        return avgDuration;
    }
    public Duration getAverageTimeSpent() {
        Duration avgDuration = Duration.ZERO;
        int count = 0;
        for(var dur : timeSpentMap.values()){
            if(dur.compareTo(Duration.ZERO) == 0)
                continue;
            avgDuration = avgDuration.plus(dur);
            count++;
        }
        if(count > 0)
            avgDuration = avgDuration.dividedBy(count);

        return avgDuration;
    }
    public int getAverageQueueLength() {
        int avg = 0;
        if(averageQueueSize.isEmpty())
            return avg;
        for(var t : averageQueueSize.values())
            avg += (t.getFirst() + t.getSecond())/2;
        return avg/averageQueueSize.size();
    }
    public StoreReceipt getCurrentTask() {
        return currentTask;
    }
    public Queue<StoreReceipt> getQueue() {
        return currentStoreReceiptsQueue;
    }
    public Duration getCurrentDuration() {
        return currentDuration;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("%s\n".formatted(super.toString()));
        sb.append("Дневная зарплата: %.2f руб.\n".formatted(dailySalary));
        sb.append("Зарплата за все дни: %.2f руб.\n".formatted(getTotalSalary()));
        sb.append("Текущий доход: %.2f руб.\n".formatted(getTotalProfit()));
        sb.append("Среднее время ожидания в очереди: %s\n".formatted(DurationUtils.toString(getAverageWaitingDurationInQueue())));
        sb.append("Среднее время работы: %s\n".formatted(DurationUtils.toString(getAverageTimeSpent())));
        sb.append("Среднее длина очереди: %d\n".formatted(getAverageQueueLength()));
        sb.append("Текущий клиент: \n\t%s".formatted(getCurrentTask() == null ? "Пусто" : getCurrentTask().toString().replace("\n", "\n\t")));
        return sb.toString();
    }
}
