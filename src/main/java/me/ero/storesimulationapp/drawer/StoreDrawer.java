package me.ero.storesimulationapp.drawer;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import me.ero.storesimulationapp.simulation.store_api.store.Store;
import me.ero.storesimulationapp.simulation.store_api.util.DurationUtils;
import me.ero.storesimulationapp.simulation.store_api.util.Pair;

import java.util.ArrayList;

public class StoreDrawer extends Drawer<Store, EmployeeDrawer> {
    private final ArrayList<Pair<Double,Double>> coords;
    private final double sx;
    private final double sy;
    public StoreDrawer(Store source, double sx, double sy, double width, double height) {
        super(source, sx, sy, width, height, Color.AQUA);
        this.sx = sx;
        this.sy = sy;
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
    }

    @Override
    protected void drawChildren(GraphicsContext g) {
        syncWithSource();
        super.drawChildren(g);
    }

    @Override
    public void add(EmployeeDrawer drawer) {
        super.add(drawer);
        var pair = coords.get(childrens.size()-1);
        drawer.move(pair.getFirst(), pair.getSecond());
    }
}
