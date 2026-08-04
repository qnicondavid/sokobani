package Sokobani;

import com.milandru.sokobani.core.Level;
import com.milandru.sokobani.core.Tile;
import com.milandru.sokobani.level.InvalidLevelFormatException;
import com.milandru.sokobani.level.LevelParser;
import com.milandru.sokobani.level.LevelRepository;

import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Interface for movable game objects.
 */
interface Movable {

    /**
     * Moves the object by the specified delta.
     *
     * @param dx change in x direction
     * @param dy change in y direction
     */
    void move(int dx, int dy);
}

/**
 * Represents the game map in Sokobani, including static and dynamic objects.
 * Handles level loading, rendering, player and box movement, win condition checks,
 * and game state saving.
 */
class GameMap {

    /** Listeners for game events */
    private final List<GameEventListener> listeners = new ArrayList<>();

    /** List of static objects (walls, targets, etc.) */
    private ArrayList<GameObject> staticObjects = new ArrayList<>();

    /** List of dynamic objects (player, boxes) */
    private ArrayList<Movable> dynamicObjects = new ArrayList<>();

    /** Number of rows in the map */
    private int gridHeight = 0;

    /** Number of columns in the map */
    private int gridWidth = 0;

    /** Reference to the player */
    private Player player;

    /**
     * Adds a listener for game events.
     *
     * @param l the listener to add
     */
    public void addListener(GameEventListener l) { listeners.add(l); }

    /**
     * Removes a listener for game events.
     *
     * @param l the listener to remove
     */
    public void removeListener(GameEventListener l) { listeners.remove(l); }

    /**
     * Emits a game event to all registered listeners.
     *
     * @param e the event to emit
     */
    private void emit(GameEvent e) {
        for (var l : listeners) l.onEvent(e);
    }

    public GameMap() {
        loadFirstLevel();
        draw();
    }

    private void loadFirstLevel() {
        try {
            adopt(LevelRepository.load(LevelRepository.CLASSIC_PACK).get(0));
        } catch (IOException | InvalidLevelFormatException e) {
            throw new IllegalStateException("bundled level pack could not be loaded: " + e.getMessage(), e);
        }
    }

    private void adopt(Level level) {
        staticObjects.clear();
        dynamicObjects.clear();
        gridHeight = level.rowCount();
        gridWidth = level.columnCount();
        player = null;

        for (int row = 0; row < gridHeight; row++) {
            for (int col = 0; col < gridWidth; col++) {
                Tile tile = level.tileAt(new com.milandru.sokobani.core.Position(row, col));
                if (tile == Tile.WALL) {
                    staticObjects.add(new Wall(row, col));
                } else if (tile == Tile.GOAL) {
                    staticObjects.add(new Target(row, col));
                }
            }
        }
        for (var box : level.initialBoxes()) {
            dynamicObjects.add(new Box(box.row(), box.col()));
        }
        player = new Player(level.initialPlayer().row(), level.initialPlayer().col());
        dynamicObjects.add(player);
    }

    /**
     * Attempts to move the player by a given delta.
     *
     * @param dx delta x
     * @param dy delta y
     * @return true if move successful
     */
    public boolean attemptToMovePlayer(int dx, int dy) {
        Position from = player.getPosition();

        boolean pushed = false;
        boolean moved = false;

        int newX = from.getX() + dx;
        int newY = from.getY() + dy;
        GameObject obj = getObjectAt(newX, newY);

        if (obj == null || obj.getSymbol() == 'T') {
            player.move(dx, dy);
            moved = true;
        } else if (obj.getSymbol() == 'B') {
            if (attemptToMoveBox((Box)obj, dx, dy)) {
                player.move(dx, dy);
                moved = true;
                pushed = true;
            }
        }

        if (!moved) {
            emit(new GameEvent(GameEventType.INVALID_MOVE, from, from, null));
            return false;
        }

        Position to = player.getPosition();
        emit(new GameEvent(pushed ? GameEventType.PUSH : GameEventType.MOVE, from, to, null));

        if (checkWinCondition()) {
            emit(new GameEvent(GameEventType.WIN, null, null, "All boxes on targets"));
        }

        return true;
    }

    /**
     * Attempts to move a box by a given delta.
     *
     * @param box the box to move
     * @param dx delta x
     * @param dy delta y
     * @return true if move successful
     */
    private boolean attemptToMoveBox(Box box, int dx, int dy) {
        Position from = box.getPosition();
        int newX = from.getX() + dx;
        int newY = from.getY() + dy;

        GameObject obj = getObjectAt(newX, newY);

        if (obj == null || obj.getSymbol() == 'T') {
            box.move(dx, dy);
            emit(new GameEvent(GameEventType.PUSH, from, box.getPosition(), null));
            return true;
        }

        if (obj.getSymbol() == 'B') {
            if (attemptToMoveBox((Box) obj, dx, dy)) {
                box.move(dx, dy);
                emit(new GameEvent(GameEventType.PUSH, from, box.getPosition(), null));
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if all boxes are on targets.
     *
     * @return true if player has won
     */
    private boolean checkWinCondition() {
        for (GameObject a : staticObjects) {
            if (a instanceof Target target) {
                boolean found = false;
                for (Movable o : dynamicObjects) {
                    if (o instanceof Box box) {
                        if (box.getPosition().equals(target.getPosition())) {
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) return false;
            }
        }
        return true;
    }

    /**
     * Returns the object at a specific position.
     *
     * @param x row coordinate
     * @param y column coordinate
     * @return the object at the position, or null if empty
     */
    public GameObject getObjectAt(int x, int y) {
        for (Movable obj : dynamicObjects)
            if (((GameObject) obj).getPosition().equals(new Position(x, y)))
                return (GameObject) obj;

        for (GameObject obj : staticObjects)
            if (obj.getPosition().equals(new Position(x, y)))
                return obj;

        return null;
    }

    /**
     * Draws the current map state to the console.
     */
    public void draw() {
        System.out.print("\033[H\033[2J");
        System.out.flush();

        for (int i = 0; i < gridHeight; i++) {
            for (int j = 0; j < gridWidth; j++) {
                GameObject obj = getObjectAt(i, j);
                if (obj != null) {
                    System.out.print(obj.getSymbol() + " ");
                } else {
                    System.out.print(". ");
                }
            }
            System.out.println();
        }
    }

    public void reset(String filename) {
        if (loadLevel(filename)) {
            System.out.println("New game!");
        }
    }

    public void restart() {
        loadFirstLevel();
        draw();
        emit(new GameEvent(GameEventType.RESTART, null, null, null));
    }

    public void writeToFile(String filename) {
        String path = "src/main/resources/" + filename;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            char[][] grid = new char[gridHeight][gridWidth];

            for (int row = 0; row < gridHeight; row++) {
                Arrays.fill(grid[row], LevelParser.FLOOR);
            }

            for (GameObject object : staticObjects) {
                int row = object.getPosition().getX();
                int col = object.getPosition().getY();
                grid[row][col] = object instanceof Target ? LevelParser.GOAL : LevelParser.WALL;
            }

            for (Movable object : dynamicObjects) {
                if (object instanceof Box box) {
                    write(grid, box, LevelParser.BOX, LevelParser.BOX_ON_GOAL);
                }
            }

            if (player != null) {
                write(grid, player, LevelParser.PLAYER, LevelParser.PLAYER_ON_GOAL);
            }

            for (int row = 0; row < gridHeight; row++) {
                writer.write(new String(grid[row]));
                writer.newLine();
            }

            System.out.println("Autosave successful: " + path);

        } catch (IOException e) {
            System.err.println("Autosave failed: " + e.getMessage());
        }
    }

    private void write(char[][] grid, GameObject object, char plain, char onTarget) {
        int row = object.getPosition().getX();
        int col = object.getPosition().getY();
        grid[row][col] = grid[row][col] == LevelParser.GOAL ? onTarget : plain;
    }

    public boolean loadLevel(String filename) {
        try {
            adopt(LevelParser.parse(Files.readString(Path.of(filename))));
            draw();
            return true;

        } catch (IOException | InvalidLevelFormatException | RuntimeException e) {
            System.err.println("Load failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Quits the game.
     */
    public void quit() {
        System.out.println("Goodbye!");
        System.exit(0);
    }

    /**
     * Returns the width of the map.
     *
     * @return grid width
     */
    public int getWidth() { return gridWidth; }

    /**
     * Returns the height of the map.
     *
     * @return grid height
     */
    public int getHeight() { return gridHeight; }
}
