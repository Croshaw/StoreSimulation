package me.ero.storesimulationapp.drawer;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import me.ero.storesimulationapp.simulation.store_api.human.Employee;
import me.ero.storesimulationapp.simulation.store_api.util.Pair;

import java.util.ArrayList;

public class EmployeeDrawer extends Drawer<Employee, StoreReceiptDrawer> {
    private StoreReceiptDrawer currentStoreReceiptDrawer;
    private final ArrayList<Pair<Double,Double>> coords;
    private final int maxQueueLength;
    private final double sx;
    private final double sy;
    public EmployeeDrawer(Employee source, double sx, double sy, int maxQueueLength) {
        super(source, sx, sy, maxQueueLength * 10 + maxQueueLength * 2, maxQueueLength * 10 + maxQueueLength * 2, Color.GREEN);
        coords = new ArrayList<>();
        for(int i = 0; i < maxQueueLength; i++) {
            coords.add(new Pair<>(0d, i*10d + i * 2d));
        }
        this.sx = sx;
        this.sy = sy;
        source.getQueue().forEach(x -> add(new StoreReceiptDrawer(x, sx, sy)));
        this.maxQueueLength = maxQueueLength;
    }
    @Override
    public void pause() {
        super.pause();
        if(currentStoreReceiptDrawer != null)
            currentStoreReceiptDrawer.pause();
    }
    @Override
    public void resume() {
        super.pause();
        if(currentStoreReceiptDrawer != null)
            currentStoreReceiptDrawer.resume();
    }
    @Override
    public boolean isFixit() {
        boolean isFixit = true;
        if(currentStoreReceiptDrawer != null)
            isFixit = currentStoreReceiptDrawer.isFixit();
        return super.isFixit() && isFixit;
    }
    @Override
    public void setSpeed(double value) {
        super.setSpeed(value);
        if(currentStoreReceiptDrawer != null)
            currentStoreReceiptDrawer.setSpeed(value);
    }

    @Override
    public boolean mouseMovedEvent(MouseEvent event) {
        if(currentStoreReceiptDrawer != null && currentStoreReceiptDrawer.mouseMovedEvent(event)) {
            drawTooltip = false;
            return true;
        }
        return super.mouseMovedEvent(event);
    }

    private void syncWithSource() {
        var queue = source.getQueue();
        ArrayList<StoreReceiptDrawer> toRemove = new ArrayList<>();
        for(var drawer : childrens) {
            boolean contains = false;
            for(var storeReceipt : queue) {
                if (drawer.source == storeReceipt) {
                    contains = true;
                    break;
                }
            }
            if(!contains)
                toRemove.add(drawer);
        }
        toRemove.forEach(this::remove);
        for(var storeReceipt : queue) {
            boolean contains = false;
            for(var drawer : childrens) {
                if(drawer.source == storeReceipt) {
                    contains = true;
                    break;
                }
            }
            if(!contains)
                add(new StoreReceiptDrawer(storeReceipt, sx, sy));
        }

        if(currentStoreReceiptDrawer != null && source.getCurrentTask() == null) {
            currentStoreReceiptDrawer = null;
        }
    }
    @Override
    protected void drawChildren(GraphicsContext g) {
        syncWithSource();
        super.drawChildren(g);

        if(currentStoreReceiptDrawer != null) {
            currentStoreReceiptDrawer.draw(g);
        }
    }
    @Override
    public void remove(StoreReceiptDrawer drawer) {
        currentStoreReceiptDrawer = drawer;
        currentStoreReceiptDrawer.move(rectangle.getX() + (rectangle.getWidth() / 2 - currentStoreReceiptDrawer.rectangle.getWidth()/2),
                rectangle.getY() + (rectangle.getHeight() - currentStoreReceiptDrawer.rectangle.getHeight()));
        super.remove(drawer);
        for(int i = 0; i < childrens.size(); i++) {
            var pair = coords.get(i);
            childrens.get(i).move(pair.getFirst(), pair.getSecond());
        }
        if(drawer != currentStoreReceiptDrawer) {
            drawer.move(800, 700);
        }
    }
    @Override
    public void add(StoreReceiptDrawer drawer) {
        super.add(drawer);
        var pair = coords.get(childrens.size()-1);
        drawer.move(0, rectangle.getY()+ rectangle.getHeight()+drawer.rectangle.getHeight()+2);
        drawer.move(rectangle.getX() - drawer.rectangle.getWidth()-2, rectangle.getY()+ rectangle.getHeight()+drawer.rectangle.getHeight()+2);
        drawer.move(pair.getFirst(), pair.getSecond());
    }

    @Override
    public void move(double toX, double toY) {
        super.move(toX, toY);
        coords.clear();
        for(int i = 0; i < maxQueueLength; i++) {
            coords.add(new Pair<>(toX-12, toY + i*10d + i * 2d));
        }
        for(int i = 0; i < childrens.size(); i++) {
            var pair = coords.get(i);
            childrens.get(i).move(pair.getFirst(), pair.getSecond());
        }
        if(currentStoreReceiptDrawer != null)
            currentStoreReceiptDrawer.move(rectangle.getX() + (rectangle.getWidth() / 2 - currentStoreReceiptDrawer.rectangle.getWidth()/2),rectangle.getY() + (rectangle.getHeight() - currentStoreReceiptDrawer.rectangle.getHeight()));
    }

    @Override
    public void drawTooltip(GraphicsContext g) {
        if(currentStoreReceiptDrawer != null)
            currentStoreReceiptDrawer.drawTooltip(g);
        super.drawTooltip(g);
    }
}
