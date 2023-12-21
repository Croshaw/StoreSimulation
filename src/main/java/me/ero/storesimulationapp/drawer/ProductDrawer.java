package me.ero.storesimulationapp.drawer;

import javafx.scene.paint.Color;
import me.ero.storesimulationapp.simulation.store_api.Product;

public class ProductDrawer extends Drawer<Product, Drawer> {
    public ProductDrawer(Product source, double startX, double startY) {
        super(source, startX, startY, 15, 15, Color.BROWN);
    }
}
