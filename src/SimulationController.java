import store_api.Product;
import store_api.StoreReceipt;
import store_api.human.Buyer;
import store_api.human.Employee;
import store_api.human.Salesman;
import store_api.store.SimpleStore;
import store_api.store.Store;
import store_api.store.SuperStore;
import store_api.util.FileHelper;
import store_api.util.Pair;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class SimulationController {
    public static final String curPath = System.getProperty("user.dir");
    private Random random;
    private boolean isSimpleStore;
    private final Store store;
    private long seed;
    private HashMap<DayOfWeek, Float> flowDensityByDay;
    private final long secondsStep;
    private final Duration durationSimulation;
    public SimulationController(long seed, long secondsStep, Duration durationSimulation) {
        random = new Random(seed);
        this.secondsStep = secondsStep;
        this.durationSimulation = durationSimulation;
        isSimpleStore = random.nextBoolean();
        HashMap<DayOfWeek, Pair<LocalTime, Duration>> workSchedule = new HashMap<>();
        workSchedule.put(DayOfWeek.MONDAY, new Pair<>(LocalTime.of(8, 0, 0), Duration.ofHours(11)));
        workSchedule.put(DayOfWeek.TUESDAY, new Pair<>(LocalTime.of(8, 0, 0), Duration.ofHours(11)));
        workSchedule.put(DayOfWeek.WEDNESDAY, new Pair<>(LocalTime.of(8, 0, 0), Duration.ofHours(11)));
        workSchedule.put(DayOfWeek.THURSDAY, new Pair<>(LocalTime.of(8, 0, 0), Duration.ofHours(11)));
        workSchedule.put(DayOfWeek.FRIDAY, new Pair<>(LocalTime.of(8, 0, 0), Duration.ofHours(11)));
        workSchedule.put(DayOfWeek.SATURDAY, new Pair<>(LocalTime.of(9, 0, 0), Duration.ofHours(9)));
        workSchedule.put(DayOfWeek.SUNDAY, new Pair<>(LocalTime.of(10, 0, 0), Duration.ofHours(7)));

        flowDensityByDay = new HashMap<>();
        flowDensityByDay.put(DayOfWeek.MONDAY, 1f);
        flowDensityByDay.put(DayOfWeek.TUESDAY, 1f);
        flowDensityByDay.put(DayOfWeek.WEDNESDAY, 1f);
        flowDensityByDay.put(DayOfWeek.THURSDAY, 1f);
        flowDensityByDay.put(DayOfWeek.FRIDAY, .8f);
        flowDensityByDay.put(DayOfWeek.SATURDAY, .6f);
        flowDensityByDay.put(DayOfWeek.SUNDAY, .6f);

        if(isSimpleStore)
                store = new SimpleStore(workSchedule, 7000);
        else
            store = new SuperStore(workSchedule, random.nextInt(5, 9), 7000);
        generateEmployees();
        preGenerateProducts();
    }
    private void generateEmployees() {
        if(store.getCountOfEmployees() == 7)
            return;
        int countEmployees = random.nextInt(1, 8-store.getCountOfEmployees());
        if(isSimpleStore) {
            for (int i = 0; i < countEmployees; i++) {
                store.hire(new Salesman(FileHelper.getRandomString("%s\\src\\surnames.txt".formatted(curPath), random),
                        FileHelper.getRandomString("%s\\src\\names.txt".formatted(curPath), random),
                        FileHelper.getRandomString("%s\\src\\patronymics.txt".formatted(curPath), random),
                        Duration.ofDays(random.nextLong(1, 9)), store::fire));
            }
        } else {
            for (int i = 0; i < countEmployees; i++) {
                store.hire(new Employee(FileHelper.getRandomString("%s\\src\\surnames.txt".formatted(curPath), random),
                        FileHelper.getRandomString("%s\\src\\names.txt".formatted(curPath), random),
                        FileHelper.getRandomString("%s\\src\\patronymics.txt".formatted(curPath), random)));
            }
        }
    }
    private void preGenerateProducts() {
        int countProducts = random.nextInt(10, 31);
        for(int i = 0;i < countProducts; i++) {
            if(!store.addProduct(Product.getProductFromString(FileHelper.getRandomString("%s\\src\\products.txt".formatted(curPath), random), random)))
                i--;
        }
    }
    private Deque<Pair<LocalDateTime, StoreReceipt>> preGenerateStoreReceipts() {
        Deque<Pair<LocalDateTime, StoreReceipt>> storeReceipts = new ArrayDeque<>();
        LocalDateTime startDateTime = store.getCurrentDateTime();
        Duration currentDuration = store.getCurrentDuration();
        Duration from = Duration.ZERO;
        Duration to = Duration.ofMinutes(30);
        while(currentDuration.compareTo(durationSimulation) < 1) {
            if(!store.isWork(startDateTime.plus(currentDuration))) {
                currentDuration = currentDuration.plusHours(1);
                continue;
            }
            Duration randDur = Duration.ofSeconds(random.nextLong(from.getSeconds(), to.getSeconds() + 1));
            currentDuration = currentDuration.plus(randDur);
            StoreReceipt storeReceipt = new StoreReceipt(new Buyer(FileHelper.getRandomString("%s\\src\\surnames.txt".formatted(curPath), random),
                    FileHelper.getRandomString("%s\\src\\names.txt".formatted(curPath), random),
                    FileHelper.getRandomString("%s\\src\\patronymics.txt".formatted(curPath), random)), Duration.ofMinutes(random.nextInt(1, 8)));
            double randPriceReceipt = random.nextDouble(30, 9001);
            ArrayList<Product> products = new ArrayList<>();
            products.addAll(store.getProducts());
            while (storeReceipt.getTotalPrice() < randPriceReceipt) {
                storeReceipt.addProduct(products.get(random.nextInt(0, products.size())));
            }
            storeReceipts.offerLast(new Pair<>(startDateTime.plus(currentDuration), storeReceipt));
        }
        return storeReceipts;
    }

    public void simulate() {
        Deque<Pair<LocalDateTime, StoreReceipt>> storeReceipts = preGenerateStoreReceipts();
        while(!isDone()) {
            if(storeReceipts.isEmpty())
                storeReceipts = preGenerateStoreReceipts();
            if(store.isWork()) {
                Duration durOffset = Duration.ofSeconds((long) ((1 / store.getReputation() - 1) * 1544.872));
                while(storeReceipts.getFirst().getFirst().plus(durOffset).isBefore(store.getCurrentDateTime())) {
                    store.addToQueue(storeReceipts.pollFirst().getSecond());
                    durOffset = Duration.ofSeconds((long) ((1 / store.getReputation() - 1) * 1544.872));
                }
                if (random.nextFloat() > 0.8)
                    store.startAdvertising();
                if (random.nextFloat() > 0.8)
                    store.startDiscounts(random);
                if (isSimpleStore && (random.nextFloat() > 0.88 || store.getCountOfEmployees() < 1)) {
                    generateEmployees();
                }
            }
            store.iterDiscounts(secondsStep);
            store.work(secondsStep);
        }

    }
    public boolean isDone() {
        return store.getCurrentDuration().compareTo(durationSimulation) >= 0;
    }

    public String getReport() {
        if(store == null)
            return "";
        return store.toString();
    }
}
