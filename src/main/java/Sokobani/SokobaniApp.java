package Sokobani;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * Main JavaFX application for Sokobani.
 * Listens to GameMap events to update UI reactively.
 */
public class SokobaniApp extends Application {

    /** The game map */
    private GameMap map;

    /** GridPane used for rendering the game grid */
    private GridPane grid;

    /** Size of each tile in pixels */
    private static final int TILE_SIZE = 64;

    /** Path to the initial level file */
    private static final String LEVEL_FILE = "src/main/resources/level1.txt";

    /**
     * JavaFX entry point. Sets up the scene, initializes the map and grid,
     * subscribes to game events, and handles keyboard input.
     *
     * @param stage primary stage
     */
    @Override
    public void start(Stage stage) {
        map = new GameMap();
        map.loadLevel(LEVEL_FILE);

        grid = new GridPane();
        grid.setHgap(0);
        grid.setVgap(0);

        BorderPane root = new BorderPane(grid);
        Scene scene = new Scene(root, map.getWidth() * TILE_SIZE, map.getHeight() * TILE_SIZE);

        map.addListener(e -> {
            switch (e.type()) {
                case MOVE, PUSH, RESTART -> render();
                case WIN -> {
                    render();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Victory!");
                    alert.setHeaderText(null);
                    alert.setContentText("You win!");
                    alert.showAndWait();
                    grid.getScene().getRoot().requestFocus();
                }
                case INVALID_MOVE -> {
                }
            }
        });

        InputHandler input = new InputHandler();
        scene.setOnKeyPressed(e -> {
            KeyCode code = e.getCode();
            switch (code) {
                case W, UP -> input.fireMoveUp(map);
                case S, DOWN -> input.fireMoveDown(map);
                case A, LEFT -> input.fireMoveLeft(map);
                case D, RIGHT -> input.fireMoveRight(map);
                case R -> input.fireRestart(map);
                case Q -> input.fireQuit(map);
            }
        });

        stage.setTitle("Sokobani");
        stage.setScene(scene);
        stage.show();
        scene.getRoot().requestFocus();

        render();
    }

    /**
     * Renders the game grid. Draws floor tiles first, then any game objects
     * on top of them.
     */
    private void render() {
        grid.getChildren().clear();

        for (int x = 0; x < map.getHeight(); x++) {
            for (int y = 0; y < map.getWidth(); y++) {
                Floor floor = new Floor(x, y);
                grid.add(createImageView(floor), y, x);

                GameObject top = map.getObjectAt(x, y);
                if (top != null) {
                    grid.add(createImageView(top), y, x);
                }
            }
        }
    }

    /**
     * Creates a properly scaled ImageView for a GameObject.
     * Clips the image to the tile size.
     *
     * @param obj the game object to render
     * @return a scaled and clipped ImageView
     */
    private ImageView createImageView(GameObject obj) {
        ImageView view = obj.getImageView();
        view.setFitWidth(TILE_SIZE);
        view.setFitHeight(TILE_SIZE);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        view.setCache(true);

        Rectangle clip = new Rectangle(TILE_SIZE, TILE_SIZE);
        view.setClip(clip);

        return view;
    }

    /**
     * Launches the Sokobani JavaFX application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch();
    }
}
