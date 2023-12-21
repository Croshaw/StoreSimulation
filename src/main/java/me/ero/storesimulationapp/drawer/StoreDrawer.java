package me.ero.storesimulationapp.drawer;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import me.ero.storesimulationapp.simulation.store_api.Product;
import me.ero.storesimulationapp.simulation.store_api.store.Store;
import me.ero.storesimulationapp.simulation.store_api.util.DurationUtils;
import me.ero.storesimulationapp.simulation.store_api.util.Pair;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class StoreDrawer extends Drawer<Store, EmployeeDrawer> {
    private final ArrayList<Pair<Double,Double>> coords;
    private final ArrayList<ProductDrawer> productDrawers;
    private final double sx;
    private final double sy;
    public StoreDrawer(Store source, double sx, double sy, double width, double height) {
        super(source, sx, sy, width, height, Color.AQUA);
        this.sx = sx;
        this.sy = sy;
        productDrawers = new ArrayList<>();
        coords = new ArrayList<>();
        double xOffset = 70;
        double yOffset = 30;
        double startX = xOffset + sx;
        double startY = yOffset + sy;
        double size = 10 * source.getMaxQueueLength() + 2 * source.getMaxQueueLength();
        for(int i = 0; i < source.getMaxEmployeesCount(); i++) {
            if(startX-sx + size > width) {
                startX = xOffset + sx;
                startY += size + yOffset;
            }
            coords.add(new Pair<>(startX, startY));
            startX += size + xOffset;
        }
        size = 15;
        xOffset = 5;
        yOffset = 5;
        startX = xOffset;
        startY = sy + height - size;
        var products = source.getProducts();
        products.forEach(x-> productDrawers.add(new ProductDrawer(x, sx,sy)));
        for(int i = 0; i < productDrawers.size(); i++) {
            if(startX + size > width) {
                startX = xOffset;
                startY += yOffset;
            }
            productDrawers.get(i).move(sx+startX, startY);
            startX += size + xOffset;
        }
        var emp = source.getEmployees();
        emp.forEach(x-> add(new EmployeeDrawer(x, sx, sy, ((Store) source).getMaxQueueLength())));
    }
    private void syncWithSource() {
        var employees = source.getEmployees();
        ArrayList<EmployeeDrawer> toRemove = new ArrayList<>();
        for(var drawer : childrens) {
            boolean contains = false;
            for(var employee : employees) {
                if (drawer.source == employee) {
                    contains = true;
                    break;
                }
            }
            if(!contains)
                toRemove.add(drawer);
        }
        toRemove.forEach(this::remove);
        for(var employee : employees) {
            boolean contains = false;
            for(var drawer : childrens) {
                if(drawer.source == employee) {
                    contains = true;
                    break;
                }
            }
            if(!contains)
                add(new EmployeeDrawer(employee, sx, sy, source.getMaxQueueLength()));
        }
    }
    @Override
    public void draw(GraphicsContext g) {
        super.draw(g);
        String dur = "Длительность: %s".formatted(DurationUtils.toStringWithDay(source.getCurrentDuration()));
        Drawer.drawTextOnBackground(g, (sx+rectangle.getWidth()) - Drawer.computeTextWidth(g.getFont(), dur) - 25, sy, Color.BLACK, Color.AQUA, dur);
        dur = "Дата: %s".formatted(source.getCurrentDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        Drawer.drawTextOnBackground(g, (sx+rectangle.getWidth()) - Drawer.computeTextWidth(g.getFont(), dur) - 25, sy+20, Color.BLACK, Color.AQUA, dur);
    }
    @Override
    public boolean mouseMovedEvent(MouseEvent event) {
        for(var product : productDrawers) {
            if(product.mouseMovedEvent(event)) {
                drawTooltip = false;
                return true;
            }
        }
        return super.mouseMovedEvent(event);
    }

    @Override
    public void remove(EmployeeDrawer drawer) {
        super.remove(drawer);
        for(int i = 0; i < childrens.size(); i++) {
            var p = coords.get(i);
            childrens.get(i).move(p.getFirst(), p.getSecond());
        }
    }
    @Override
    protected void drawChildren(GraphicsContext g) {
        for(var product : productDrawers)
            product.draw(g);
        syncWithSource();
        super.drawChildren(g);
    }
    @Override
    public void drawTooltip(GraphicsContext g) {
        for(var product : productDrawers)
            product.drawTooltip(g);
        super.drawTooltip(g);
    }

    @Override
    public void add(EmployeeDrawer drawer) {
        super.add(drawer);
        var pair = coords.get(childrens.size()-1);
        drawer.move(pair.getFirst(), pair.getSecond());
    }

    @Override
    public void setSpeed(double value) {
        for(var product : productDrawers)
            product.setSpeed(value);
        super.setSpeed(value);
    }

    @Override
    public boolean isFixit() {
        boolean res = true;
        for(var pro : productDrawers)
            if(!pro.isFixit())
                return false;
        return super.isFixit();
    }
}
