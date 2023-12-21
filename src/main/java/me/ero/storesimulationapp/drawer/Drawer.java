package me.ero.storesimulationapp.drawer;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import me.ero.storesimulationapp.simulation.store_api.util.Pair;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;

public class Drawer<T, F extends Drawer> {
    protected T source;
    protected LinkedList<F> childrens;
    protected Rectangle rectangle;
    private Color color;
    protected Deque<Pair<Double, Double>> moves;
    private boolean isPause;
    private double speed;
    protected boolean drawTooltip;
    private double mouseX;
    private double mouseY;
    public Drawer(T source, double startX, double startY, double width, double height, Color color) {
        this.source = source;
        rectangle = new Rectangle(startX,startY, width, height);
        this.color = color;
        moves = new ArrayDeque<>();
        childrens = new LinkedList<>();
        drawTooltip = false;
    }
    public Rectangle getRectangle() {
        return rectangle;
    }
    public boolean mouseMovedEvent(MouseEvent event) {
        for(var child : childrens) {
            if(child.mouseMovedEvent(event)) {
                drawTooltip = false;
                return true;
            }
        }
        drawTooltip = isHover(event.getX(), event.getY());
        if(drawTooltip) {
            mouseX = event.getX();
            mouseY = event.getY();
        }
        return drawTooltip;
    }
    public boolean isPause() {
        return isPause;
    }
    public void pause() {
        isPause = true;
        childrens.forEach(Drawer::pause);
    }
    public void resume() {
        isPause = false;
        childrens.forEach(Drawer::resume);
    }
    public void setSpeed(double value) {
        this.speed = value;
        childrens.forEach(x -> x.setSpeed(value));
    }
    public void draw(GraphicsContext g) {
        if(source == null)
            return;
        drawRectangle(g);
        drawChildren(g);
    }
    protected void drawRectangle(GraphicsContext g) {
        g.setFill(color);
        g.fillRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
        if(moves.isEmpty() || isPause)
            return;
        var newCords = moves.getFirst();
        boolean isDelete = true;
        if(Math.abs(rectangle.getX() - newCords.getFirst()) > 1) {
            rectangle.setX(rectangle.getX() + (speed * (newCords.getFirst() - rectangle.getX())));
            isDelete = false;
        }
        else
            rectangle.setX(newCords.getFirst());

        if(Math.abs(rectangle.getY() - newCords.getSecond()) > 1) {
            rectangle.setY(rectangle.getY() + (speed * (newCords.getSecond() - rectangle.getY())));
            isDelete = false;
        }
        else
            rectangle.setY(newCords.getSecond());
        if(isDelete)
            moves.removeFirst();
    }
    protected void drawChildren(GraphicsContext g) {
        childrens.forEach(x-> x.draw(g));
    }
    public void drawTooltip(GraphicsContext g) {
        childrens.forEach(x-> x.drawTooltip(g));
        if(source != null && drawTooltip) {
            drawTextOnBackground(g, mouseX, mouseY, Color.WHITE, Color.BLACK, source.toString());
        }
    }
    public void move(double toX, double toY) {
        if(source != null && !isPause)
            moves.addLast(new Pair<>(toX, toY));
    }
    public boolean isHover(double x, double y) {
        return x >= rectangle.getX() && x <= rectangle.getX()+rectangle.getWidth() &&
                y >= rectangle.getY() && y <= rectangle.getY()+rectangle.getHeight();
    }
    public boolean isFixit() {
        boolean tempRes = true;
        for(var child : childrens)
            tempRes = tempRes && child.isFixit();
        return moves.isEmpty() && tempRes;
    }
    public double getSpeed() {
        return speed;
    }
    public void clearMoves() {
        moves.clear();
    }
    public void add(F drawer) {
        childrens.add(drawer);
        drawer.setSpeed(getSpeed());
        if(isPause)
            drawer.pause();
    }
    public void remove(F drawer) {
        childrens.remove(drawer);
    }
    public static void drawTextOnBackground(GraphicsContext g, double x, double y,Color foreColor, Color background, String text) {
        double xx = x + 10;
        double yy = y + 10;

        double width = computeTextWidth(g.getFont(), text)+15;
        double height = computeTextHeight(g.getFont(), text) + 10;

        Canvas c = g.getCanvas();
        if(xx + width > c.getWidth()) {
            xx = x - width - 10;
        }
        if(yy + height > c.getHeight()) {
            yy = y - height;
        }
        g.setFill(background);
        g.fillRoundRect(xx, yy, width, height, 20, 20);

        g.setFill(foreColor);
        g.fillText(text, xx + 15/2, yy+15);
    }
    public static double computeTextWidth(Font font, String text) {
        javafx.scene.text.Text textNode = new javafx.scene.text.Text(text);
        textNode.setFont(font);
        return textNode.getLayoutBounds().getWidth();
    }
    public static double computeTextHeight(Font font, String text) {
        javafx.scene.text.Text textNode = new javafx.scene.text.Text(text);
        textNode.setFont(font);
        return textNode.getLayoutBounds().getHeight();
    }
}
