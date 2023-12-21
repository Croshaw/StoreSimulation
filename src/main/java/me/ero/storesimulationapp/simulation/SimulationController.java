package me.ero.storesimulationapp.simulation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.Canvas;
import me.ero.storesimulationapp.drawer.StoreDrawer;
import me.ero.storesimulationapp.simulation.store_api.Product;
import me.ero.storesimulationapp.simulation.store_api.StoreReceipt;
import me.ero.storesimulationapp.simulation.store_api.human.Buyer;
import me.ero.storesimulationapp.simulation.store_api.human.Employee;
import me.ero.storesimulationapp.simulation.store_api.human.Salesman;
import me.ero.storesimulationapp.simulation.store_api.store.SimpleStore;
import me.ero.storesimulationapp.simulation.store_api.store.Store;
import me.ero.storesimulationapp.simulation.store_api.store.SuperStore;
import me.ero.storesimulationapp.simulation.store_api.util.FileHelper;
import me.ero.storesimulationapp.simulation.store_api.util.adapters.DurationTypeAdapter;
import me.ero.storesimulationapp.simulation.store_api.util.adapters.LocalDateTimeTypeAdapter;
import me.ero.storesimulationapp.simulation.store_api.util.adapters.LocalDateTypeAdapter;
import me.ero.storesimulationapp.simulation.store_api.util.adapters.LocalTimeTypeAdapter;

import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class SimulationController {
    private static Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .registerTypeAdapter(LocalTime.class, new LocalTimeTypeAdapter())
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .create();
    public static final String curPath = System.getProperty("user.dir");
    private transient final Random random;
    private final boolean isSimpleStore;
    private final Store store;
    private long seed;
    private long secondsStep;
    private final Duration durationSimulation;
    private boolean isPause;
    private transient final StoreDrawer storeDrawer;
    private transient final Canvas canvas;
    private final Duration from;
    private final Duration to;
    private final long serviceDurFrom;
    private final long serviceDurTo;
    private LocalDateTime lastGeneratedRequest;
    private transient final Timeline simulationTimeline;
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

        HashMap<DayOfWeek, Float> flowDensityByDay = new HashMap<>();
        flowDensityByDay.put(DayOfWeek.MONDAY, 1f);
        flowDensityByDay.put(DayOfWeek.TUESDAY, 1f);
        flowDensityByDay.put(DayOfWeek.WEDNESDAY, 1f);
        flowDensityByDay.put(DayOfWeek.THURSDAY, 1f);
        flowDensityByDay.put(DayOfWeek.FRIDAY, .8f);
        flowDensityByDay.put(DayOfWeek.SATURDAY, .6f);
        flowDensityByDay.put(DayOfWeek.SUNDAY, .6f);
        if(isSimpleStore)
                store = new SimpleStore(7000, 7);
        else
            store = new SuperStore(random.nextInt(5, 9), 7,7000);
        generateEmployees();
        preGenerateProducts();
        double tW = canvas.getWidth()*3/4;
        double tH = canvas.getHeight()*3/4;
        storeDrawer = new StoreDrawer(store, (canvas.getWidth() - tW)/2, (canvas.getHeight() - tH)/2,tW, tH);
        canvas.setOnMouseMoved(storeDrawer::mouseMovedEvent);
        isPause = false;
        simulationTimeline = new Timeline(
                new KeyFrame(javafx.util.Duration.ONE, actionEvent -> storeDrawer.draw(canvas.getGraphicsContext2D())),
                new KeyFrame(javafx.util.Duration.ONE, actionEvent -> storeDrawer.drawTooltip(canvas.getGraphicsContext2D())),
                new KeyFrame(javafx.util.Duration.millis(20)),
                new KeyFrame(javafx.util.Duration.ZERO, actionEvent -> clearRect()),
                new KeyFrame(javafx.util.Duration.ZERO, actionEvent -> hz())
        );
        simulationTimeline.setCycleCount(Timeline.INDEFINITE);
    }
    public void pause() {
        isPause = true;
    }
    public void resume() {
        isPause = false;
    }
    public void stop() {
        simulationTimeline.stop();
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
                        1500, Duration.ofDays(random.nextLong(1, 9))));
            }
        } else {
            for (int i = 0; i < countEmployees; i++) {
                store.hire(new Employee(FileHelper.getRandomString("%s\\src\\main\\java\\me\\ero\\storesimulationapp\\simulation\\surnames.txt".formatted(curPath), random),
                        FileHelper.getRandomString("%s\\src\\main\\java\\me\\ero\\storesimulationapp\\simulation\\names.txt".formatted(curPath), random),
                        FileHelper.getRandomString("%s\\src\\main\\java\\me\\ero\\storesimulationapp\\simulation\\patronymics.txt".formatted(curPath), random), 1500));
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
    private void tryGenerateStoreReceipt() {
        if(lastGeneratedRequest == null) {
            lastGeneratedRequest = store.getCurrentDateTime();
            store.addToQueue(getStoreReceipt(random, store.getProducts(), serviceDurFrom, serviceDurTo));
            return;
        }
        long range = (long) (random.nextLong(from.toSeconds(), to.toSeconds()+1));// * store.getReputation());
        while(Duration.between(lastGeneratedRequest, store.getCurrentDateTime()).toSeconds() >= range) {
            lastGeneratedRequest = lastGeneratedRequest.plusSeconds(range);
            if(!store.addToQueue(getStoreReceipt(random, store.getProducts(), serviceDurFrom, serviceDurTo)))
                return;
            range = (long) (random.nextLong(from.toSeconds(), to.toSeconds()+1));// * store.getReputation());
        }
    }
    private void clearRect() {
        var rec = storeDrawer.getRectangle();
        canvas.getGraphicsContext2D().clearRect(0, 0, (canvas.getWidth() - rec.getWidth())/2, canvas.getHeight());
        canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), (canvas.getHeight() - rec.getHeight())/2);
        canvas.getGraphicsContext2D().clearRect(0, canvas.getHeight() - (canvas.getHeight() - rec.getHeight())/2, canvas.getWidth(), (canvas.getHeight() - rec.getHeight())/2);
        canvas.getGraphicsContext2D().clearRect(canvas.getWidth() - (canvas.getWidth() - rec.getWidth())/2, 0, (canvas.getWidth() - rec.getWidth())/2, canvas.getHeight());
    }
    private void hz() {
        if(isPause || !storeDrawer.isFixit() || isDone())
            return;
        if(store.isWork()) {
            tryGenerateStoreReceipt();
            if (random.nextFloat() > 0.8)
                store.startAdvertising();
            if (random.nextFloat() > 0.8)
                store.startDiscounts(random);
            if (isSimpleStore && (random.nextFloat() > 0.88 || store.getCountOfEmployees() < 1)) {
                generateEmployees();
            }
        } else {
            lastGeneratedRequest = store.getCurrentDateTime();
        }
        store.iterDiscounts(secondsStep);
        store.work(secondsStep);
    }
    public void simulate() {
        simulationTimeline.play();
    }
    public boolean isDone() {
        return store.getCurrentDuration().compareTo(durationSimulation) >= 0;
    }
    public String getReport() {
        if(store == null)
            return "";
        return store.toString();
    }
    public void setSpeed(double value) {
        storeDrawer.setSpeed(value);
    }
    public double getSpeed() {
        return storeDrawer.getSpeed();
    }
    public void setStep(long value) {
        secondsStep = value;
    }
    private static StoreReceipt getStoreReceipt(Random random, HashSet<Product> products, long serviceDurFrom, long serviceDurTo) {
        StoreReceipt storeReceipt = new StoreReceipt(getRandomBuyer(random), Duration.ofSeconds(random.nextLong(serviceDurFrom, serviceDurTo)));
        double randPriceReceipt = random.nextDouble(30, 9001);
        ArrayList<Product> product = new ArrayList<>(products);
        while (storeReceipt.getTotalPrice() < randPriceReceipt) {
            storeReceipt.addProduct(product.get(random.nextInt(0, products.size())));
        }
        return storeReceipt;
    }
    private static Buyer getRandomBuyer(Random random) {
        return new Buyer(FileHelper.getRandomString("%s\\src\\main\\java\\me\\ero\\storesimulationapp\\simulation\\surnames.txt".formatted(curPath), random),
                FileHelper.getRandomString("%s\\src\\main\\java\\me\\ero\\storesimulationapp\\simulation\\names.txt".formatted(curPath), random),
                FileHelper.getRandomString("%s\\src\\main\\java\\me\\ero\\storesimulationapp\\simulation\\patronymics.txt".formatted(curPath), random));
    }
    public static String serialize(SimulationController simulationController) {
        return gson.toJson(simulationController);
    }
}
