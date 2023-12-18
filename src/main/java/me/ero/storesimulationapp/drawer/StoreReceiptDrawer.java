package me.ero.storesimulationapp.drawer;

import javafx.scene.paint.Color;
import me.ero.storesimulationapp.simulation.store_api.StoreReceipt;

public class StoreReceiptDrawer extends Drawer<StoreReceipt, Drawer> {
    public StoreReceiptDrawer(StoreReceipt source, double sx, double sy) {
        super(source, sx, sy, 10, 10, Color.BLACK);
    }
}
