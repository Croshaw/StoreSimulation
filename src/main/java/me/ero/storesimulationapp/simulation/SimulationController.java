package me.ero.storesimulationapp.simulation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import me.ero.storesimulationapp.DurationRunnable;
import me.ero.storesimulationapp.drawer.StoreDrawer;
import me.ero.storesimulationapp.simulation.store_api.Product;
import me.ero.storesimulationapp.simulation.store_api.StoreReceipt;
import me.ero.storesimulationapp.simulation.store_api.human.Buyer;
import me.ero.storesimulationapp.simulation.store_api.human.Employee;
import me.ero.storesimulationapp.simulation.store_api.human.Salesman;
import me.ero.storesimulationapp.simulation.store_api.store.SimpleStore;
import me.ero.storesimulationapp.simulation.store_api.store.Store;
import me.ero.storesimulationapp.simulation.store_api.store.SuperStore;
import me.ero.storesimulationapp.simulation.store_api.util.DurationUtils;
import me.ero.storesimulationapp.simulation.store_api.util.FileHelper;
import me.ero.storesimulationapp.simulation.store_api.util.MyExclusionStrategy;
import me.ero.storesimulationapp.simulation.store_api.util.Pair;
import me.ero.storesimulationapp.simulation.store_api.util.adapters.*;

import java.time.*;
import java.util.*;

public class SimulationController {
    public static final String curPath = System.getProperty("user.dir");
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .setExclusionStrategies(new MyExclusionStrategy(Random.class))
            .registerTypeAdapter(LocalTime.class, new LocalTimeTypeAdapter())
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .registerTypeAdapter(Boolean.class, new BooleanTypeAdapter())
            .serializeNulls()
            .create();
    private final Random random;
    private boolean isSimpleStore;
    protected final Store store;
    private long seed;
    private final long secondsStep;
    private final Duration durationSimulation;
    private boolean isPause;
    private Deque<Pair<LocalDateTime, StoreReceipt>> storeReceipts;
    private final StoreDrawer storeDrawer;
    private final Canvas canvas;
    private final Duration from;
    private final Duration to;
    private final long serviceDurFrom;
    private final long serviceDurTo;
    private boolean isStop;
    public SimulationController(long seed, long secondsStep, Duration durationSimulation, long from, long to, long serviceDurFrom, long serviceDurTo, Canvas canvas) {
        random = new Random(seed);
        this.secondsStep = secondsStep;
        this.canvas = canvas;
        this.durationSimulation = durationSimulation;
        this.from = Duration.ofSeconds(from);
        this.to = Duration.ofSeconds(to);
        this.serviceDurFrom = serviceDurFrom;
        this.serviceDurTo = serviceDurTo;
        isSimpleStore = random.nextBoolean();
        HashMap<DayOfWeek, Pair<LocalTime, Duration>> workSchedule = new HashMap<>();
        workSchedule.put(DayOfWeek.MONDAY, new Pair<>(LocalTime.of(8, 0, 0), Duration.ofHours(11)));
        workSchedule.put(DayOfWeek.TUESDAY, new Pair<>(LocalTime.of(8, 0, 0), Duration.ofHours(11)));
        workSchedule.put(DayOfWeek.WEDNESDAY, new Pair<>(LocalTime.of(8, 0, 0), Duration.ofHours(11)));
        workSchedule.put(DayOfWeek.THURSDAY, new Pair<>(LocalTime.of(8, 0, 0), Duration.ofHours(11)));
        workSchedule.put(DayOfWeek.FRIDAY, new Pair<>(LocalTime.of(8, 0, 0), Duration.ofHours(11)));
        workSchedule.put(DayOfWeek.SATURDAY, new Pair<>(LocalTime.of(9, 0, 0), Duration.ofHours(9)));
        workSchedule.put(DayOfWeek.SUNDAY, new Pair<>(LocalTime.of(10, 0, 0), Duration.ofHours(7)));

        HashMap<DayOfWeek, Float> flowDensityByDay = new HashMap<>();
        flowDensityByDay.put(DayOfWeek.MONDAY, 1f);
        flowDensityByDay.put(DayOfWeek.TUESDAY, 1f);
        flowDensityByDay.put(DayOfWeek.WEDNESDAY, 1f);
        flowDensityByDay.put(DayOfWeek.THURSDAY, 1f);
        flowDensityByDay.put(DayOfWeek.FRIDAY, .8f);
        flowDensityByDay.put(DayOfWeek.SATURDAY, .6f);
        flowDensityByDay.put(DayOfWeek.SUNDAY, .6f);
        if(isSimpleStore)
                store = new SimpleStore(workSchedule, 7000, 7);
        else
            store = new SuperStore(workSchedule, random.nextInt(5, 9), 7,7000);
        generateEmployees();
        preGenerateProducts();
        storeReceipts = preGenerateStoreReceipts();
        double tW = canvas.getWidth()*3/4;
        double tH = canvas.getHeight()*3/4;
        storeDrawer = new StoreDrawer(store, (canvas.getWidth() - tW)/2, (canvas.getHeight() - tH)/2,tW, tH);
        canvas.setOnMouseMoved(storeDrawer::mouseMovedEvent);
    }
    public void pause() {
        isPause = true;
        //storeDrawer.pause();
    }
    public void resume() {
        isPause = false;
        //storeDrawer.resume();
    }
    public void stop() {
        isStop = true;
    }
    private void generateEmployees() {
        if(store.getCountOfEmployees() == 7)
            return;
        int countEmployees = random.nextInt(1, 8-store.getCountOfEmployees());
        if(isSimpleStore) {
            for (int i = 0; i < countEmployees; i++) {
                store.hire(new Salesman(FileHelper.getRandomString("%s\\src\\main\\java\\me\\ero\\storesimulationapp\\simulation\\surnames.txt".formatted(curPath), random),
                        FileHelper.getRandomString("%s\\src\\main\\java\\me\\ero\\storesimulationapp\\simulation\\names.txt".formatted(curPath), random),
                        FileHelper.getRandomString("%s\\src\\main\\java\\me\\ero\\storesimulationapp\\simulation\\patronymics.txt".formatted(curPath), random),
                        Duration.ofDays(random.nextLong(1, 9))));
            }
        } else {
            for (int i = 0; i < countEmployees; i++) {
                store.hire(new Employee(FileHelper.getRandomString("%s\\src\\main\\java\\me\\ero\\storesimulationapp\\simulation\\surnames.txt".formatted(curPath), random),
                        FileHelper.getRandomString("%s\\src\\main\\java\\me\\ero\\storesimulationapp\\simulation\\names.txt".formatted(curPath), random),
                        FileHelper.getRandomString("%s\\src\\main\\java\\me\\ero\\storesimulationapp\\simulation\\patronymics.txt".formatted(curPath), random)));
            }
        }
    }
    private void preGenerateProducts() {
        int countProducts = random.nextInt(10, 31);
        for(int i = 0;i < countProducts; i++) {
            if(!store.addProduct(Product.getProductFromString(FileHelper.getRandomString("%s\\src\\main\\java\\me\\ero\\storesimulationapp\\simulation\\products.txt".formatted(curPath), random), random)))
                i--;
        }
    }
    private Deque<Pair<LocalDateTime, StoreReceipt>> preGenerateStoreReceipts() {
        Deque<Pair<LocalDateTime, StoreReceipt>> storeReceipts = new ArrayDeque<>();
        LocalDateTime startDateTime = store.getCurrentDateTime();
        Duration currentDuration = store.getCurrentDuration();
        while(currentDuration.compareTo(durationSimulation) < 1) {
            if(!store.isWork(startDateTime.plus(currentDuration))) {
                currentDuration = currentDuration.plusHours(1);
                continue;
            }
            Duration randDur = Duration.ofSeconds(random.nextLong(from.getSeconds(), to.getSeconds() + 1));
            currentDuration = currentDuration.plus(randDur);
            StoreReceipt storeReceipt = new StoreReceipt(new Buyer(FileHelper.getRandomString("%s\\src\\main\\java\\me\\ero\\storesimulationapp\\simulation\\surnames.txt".formatted(curPath), random),
                    FileHelper.getRandomString("%s\\src\\main\\java\\me\\ero\\storesimulationapp\\simulation\\names.txt".formatted(curPath), random),
                    FileHelper.getRandomString("%s\\src\\main\\java\\me\\ero\\storesimulationapp\\simulation\\patronymics.txt".formatted(curPath), random)), Duration.ofSeconds(random.nextLong(serviceDurFrom, serviceDurTo)));
            double randPriceReceipt = random.nextDouble(30, 9001);
            ArrayList<Product> products = new ArrayList<>(store.getProducts());
            while (storeReceipt.getTotalPrice() < randPriceReceipt) {
                storeReceipt.addProduct(products.get(random.nextInt(0, products.size())));
            }
            storeReceipts.offerLast(new Pair<>(startDateTime.plus(currentDuration), storeReceipt));
        }
        return storeReceipts;
    }
    public void simulate() {
        isStop = false;
        new Thread(() -> {
            while(!isDone()) {
                if(isStop)
                    break;
                storeDrawer.draw(canvas.getGraphicsContext2D());
                storeDrawer.drawTooltip(canvas.getGraphicsContext2D());
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                if(isPause)
                    continue;
                if(!storeDrawer.isFixit())
                    continue;
                if (storeReceipts.isEmpty())
                    storeReceipts = preGenerateStoreReceipts();
                if (store.isWork()) {
                    Duration durOffset = Duration.ofSeconds((long) ((1 / store.getReputation() - 1) * 1544.872));
                    while (storeReceipts.getFirst().getFirst().plus(durOffset).isBefore(store.getCurrentDateTime())) {
                        store.addToQueue(storeReceipts.pollFirst().getSecond());
                        durOffset = Duration.ofSeconds((long) ((1 / store.getReputation() - 1) * 1544.872));
                    }
                    storeDrawer.draw(canvas.getGraphicsContext2D());
                    canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                    if(!storeDrawer.isFixit())
                        continue;
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
            isStop = false;
            while(true) {
                if(isStop)
                    break;
                storeDrawer.draw(canvas.getGraphicsContext2D());
                storeDrawer.drawTooltip(canvas.getGraphicsContext2D());
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            }
        }).start();
    }
    public void setSpeed(double value) {
        storeDrawer.setSpeed(value);
    }
    public boolean isDone() {
        return store.getCurrentDuration().compareTo(durationSimulation) >= 0;
    }
    public Store getStore() {
        return store;
    }
    public String getReport() {
        if(store == null)
            return "";
        return store.toString();
    }
    public double getSpeed() {
        return storeDrawer.getSpeed();
    }

    public static String serialize(SimulationController simulationController) {
        return gson.toJson(simulationController);
    }
    public static SimulationController deserialize (String json) {
        return gson.fromJson(json, SimulationController.class);
    }
}
