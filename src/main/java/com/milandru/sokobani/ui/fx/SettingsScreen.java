package com.milandru.sokobani.ui.fx;

import com.milandru.sokobani.ui.Surface;
import com.milandru.sokobani.ui.TypeSetter;

import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.util.List;
import java.util.Objects;

public final class SettingsScreen extends BaseScreen {

    private final TypeSetter setter;
    private final Screens screens;
    private final SettingsState settings;
    private final SoundBank sound;
    private final Selection<SettingsView.Row> selection =
            new Selection<>(List.of(
                    SettingsView.Row.SOUND,
                    SettingsView.Row.ANIMATION,
                    SettingsView.Row.HINTS));

    public SettingsScreen(TypeSetter setter, ScreenController controller, Screens screens,
                          SettingsState settings, SoundBank sound) {
        super(controller);
        this.setter = Objects.requireNonNull(setter, "setter");
        this.screens = Objects.requireNonNull(screens, "screens");
        this.settings = Objects.requireNonNull(settings, "settings");
        this.sound = Objects.requireNonNull(sound, "sound");
        holder().setOnMouseMoved(this::onMouseMoved);
        holder().setOnMouseClicked(this::onMouseClicked);
    }

    @Override
    public void shown() {
        redraw();
    }

    @Override
    public void onKeyPressed(KeyEvent event) {
        switch (GameControls.commandFor(GameControls.Mode.SETTINGS, event.getCode(),
                event.isShortcutDown() || event.isControlDown())) {
            case MOVE_UP -> selection.move(-1);
            case MOVE_DOWN -> selection.move(1);
            case CONFIRM -> toggle();
            case BACK -> controller().show(screens.menu());
            case CYCLE_THEME -> cycleTheme();
            case MOVE_LEFT, MOVE_RIGHT, UNDO, RESTART, PAUSE, RESUME, NEXT_ROOM, IGNORED -> {
                return;
            }
        }
        redraw();
    }

    private void onMouseMoved(MouseEvent event) {
        int previous = selection.index();
        for (SettingsView.Row row : SettingsView.Row.values()) {
            if (SettingsView.overRow(baseX(event.getX()), baseY(event.getY()), row.ordinal())) {
                selection.select(row.ordinal());
                break;
            }
        }
        if (selection.index() != previous) {
            redraw();
        }
    }

    private void onMouseClicked(MouseEvent event) {
        for (SettingsView.Row row : SettingsView.Row.values()) {
            if (SettingsView.overRow(baseX(event.getX()), baseY(event.getY()), row.ordinal())) {
                selection.select(row.ordinal());
                toggle();
                return;
            }
        }
    }

    private void toggle() {
        switch (selection.selected()) {
            case SOUND -> {
                settings.toggleMuted();
                sound.setMuted(settings.muted());
            }
            case ANIMATION -> settings.toggleAnimation();
            case HINTS -> settings.toggleHints();
        }
    }

    @Override
    protected Surface renderBase() {
        Surface surface = new Surface(SettingsView.WIDTH, SettingsView.HEIGHT);
        SettingsView.render(surface, setter, settings.snapshot(), selection.index());
        return surface;
    }
}
