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

import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class SimulationController {
    private Random random;
    private boolean isSimpleStore;
    private final Store store;
    private long seed;
    private HashMap<DayOfWeek, Float> flowDensityByDay;
    private LocalDateTime lastCreatedReceiptDateTime;
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

        lastCreatedReceiptDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(0,0,0));

        if(isSimpleStore)
                store = new SimpleStore(workSchedule, 7000);
        else
            store = new SuperStore(workSchedule, random.nextInt(5, 9), 3, 7000);
        generateEmployees();
        preGenerateProducts();
    }
    private void generateEmployees() {
        if(store.getCountOfEmployees() == 7)
            return;
        int countEmployees = random.nextInt(1, 8-store.getCountOfEmployees());
        if(isSimpleStore) {
            for (int i = 0; i < countEmployees; i++) {
                store.hire(new Salesman(FileHelper.getRandomString("surnames.txt", random),
                        FileHelper.getRandomString("names.txt", random),
                        FileHelper.getRandomString("patronymics.txt", random),
                        Duration.ofDays(random.nextLong(1, 8)), store::fire));
            }
        } else {
            for (int i = 0; i < countEmployees; i++) {
                store.hire(new Employee(FileHelper.getRandomString("surnames.txt", random),
                        FileHelper.getRandomString("names.txt", random),
                        FileHelper.getRandomString("patronymics.txt", random)));
            }
        }
    }
    private void preGenerateProducts() {
        int countProducts = random.nextInt(10, 31);
        for(int i = 0;i < countProducts; i++) {
            if(!store.addProduct(Product.getProductFromString(FileHelper.getRandomString("products.txt", random), random)))
                i--;
        }
    }
    private void generateStoreReceipt() {
        Duration from = Duration.ZERO;
        Duration to = Duration.ofMinutes(30);
        Duration randDur = Duration.ofSeconds(random.nextLong(from.getSeconds(), to.getSeconds()+1));
        while(!store.isWork(lastCreatedReceiptDateTime) && store.isWork()) {
            lastCreatedReceiptDateTime = lastCreatedReceiptDateTime.plusSeconds(600);
        }
        if(Duration.between(store.getCurrentDateTime(), lastCreatedReceiptDateTime).compareTo(randDur) > 0) {
            lastCreatedReceiptDateTime = lastCreatedReceiptDateTime.plus(randDur);
            StoreReceipt storeReceipt = new StoreReceipt(new Buyer(FileHelper.getRandomString("surnames.txt", random),
                    FileHelper.getRandomString("names.txt", random),
                    FileHelper.getRandomString("patronymics.txt", random)), Duration.ofMinutes(random.nextInt(1, 8)));
            double randPriceReceipt = random.nextDouble(30, 9001);
            ArrayList<Product> products = new ArrayList<>();
            products.addAll(store.getProducts());
            while(storeReceipt.getTotalPrice() < randPriceReceipt) {
                storeReceipt.addProduct(products.get(random.nextInt(0, products.size())));
            }
            store.addToQueue(storeReceipt);
        }
    }
    public void simulate() {
        while(durationSimulation.compareTo(store.getCurrentDuration()) > 0) {
            if(store.isWork()) {
                if (random.nextFloat() > 0.8)
                    store.startAdvertising();
                if (random.nextFloat() > 0.8)
                    store.startDiscounts(random);
                generateStoreReceipt();
                if (isSimpleStore && (random.nextFloat() > 0.88 || store.getCountOfEmployees() < 1)) {
                    generateEmployees();
                }
            }
            store.iterDiscounts(secondsStep);
            store.work(secondsStep);
        }
    }
}
