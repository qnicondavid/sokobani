package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.ui.Display;
import com.milandru.sokobani.ui.Scaling;
import com.milandru.sokobani.ui.Surface;
import com.milandru.sokobani.ui.Threshold;

import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;

import java.util.Objects;

abstract class BaseScreen implements Screen {

    private static final int VIEW_MARGIN = 16;

    private final StackPane holder = new StackPane();
    private final Canvas canvas = new Canvas();
    private final ScreenController controller;

    private int factor = 1;
    private int offsetX;
    private int offsetY;

    protected BaseScreen(ScreenController controller) {
        this.controller = Objects.requireNonNull(controller, "controller");
        holder.setMinSize(0, 0);
        holder.getChildren().add(canvas);
        canvas.widthProperty().bind(holder.widthProperty());
        canvas.heightProperty().bind(holder.heightProperty());
        canvas.widthProperty().addListener(ignored -> redraw());
        canvas.heightProperty().addListener(ignored -> redraw());
    }

    protected final ScreenController controller() {
        return controller;
    }

    protected final StackPane holder() {
        return holder;
    }

    @Override
    public final Parent node() {
        return holder;
    }

    protected void cycleTheme() {
        controller.cycleTheme();
        redraw();
    }

    protected void redraw() {
        if (canvas.getWidth() <= 0 || canvas.getHeight() <= 0) {
            return;
        }
        Surface base = renderBase();
        factor = Scaling.factor(
                (int) canvas.getWidth(), (int) canvas.getHeight(), base.width(), base.height(), VIEW_MARGIN);
        int scaledWidth = base.width() * factor;
        int scaledHeight = base.height() * factor;
        offsetX = (int) Math.floor((canvas.getWidth() - scaledWidth) / 2.0);
        offsetY = (int) Math.floor((canvas.getHeight() - scaledHeight) / 2.0);
        Display.present(canvas, controller.theme(), Threshold.paletteMap(base, controller.theme()),
                base.width(), base.height(), factor);
    }

    protected final int baseX(double viewX) {
        return (int) Math.floor((viewX - offsetX) / factor);
    }

    protected final int baseY(double viewY) {
        return (int) Math.floor((viewY - offsetY) / factor);
    }

    protected abstract Surface renderBase();
}
