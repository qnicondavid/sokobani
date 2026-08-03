package Sokobani;

import java.util.*;
import java.io.*;

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

    /**
     * Constructs the game map and loads the default level.
     */
    public GameMap() {
        loadFromFile("src/main/resources/level1.txt");
        draw();
    }

    /**
     * Loads a level from a file.
     *
     * @param filename path to the level file
     */
    public void loadFromFile(String filename) {
        File file = new File(filename);

        if (!file.exists()) {
            System.err.println("Error: Level file \"" + filename + "\" not found. Falling back to level1.txt.");
            if (!filename.equals("level1.txt") && !filename.equals("src/main/resources/level1.txt")) {
                loadFromFile("src/main/resources/level1.txt");
            } else if (!filename.equals("level1.txt")) {
                loadFromFile("level1.txt");
            } else {
                System.err.println("Fallback file level1.txt also missing. Exiting.");
                System.exit(1);
            }
            return;
        }

        try (Scanner reader = new Scanner(file)) {
            List<String> lines = new ArrayList<>();
            while (reader.hasNextLine()) {
                String line = reader.nextLine().trim();
                if (!line.isEmpty()) lines.add(line);
            }

            if (lines.isEmpty()) {
                throw new RuntimeException("Level file is empty!");
            }

            gridHeight = 0;
            staticObjects.clear();
            dynamicObjects.clear();
            player = null;

            gridWidth = (lines.get(0).length() + 1) / 2;
            int expectedWidth = gridWidth;
            int playerCount = 0;
            int lineNumber = 0;

            for (String line : lines) {
                lineNumber++;
                int currentWidth = (line.length() + 1) / 2;

                if (currentWidth != expectedWidth) {
                    throw new InvalidLevelFormatException(
                            "Line " + lineNumber + " has inconsistent length. Expected width: " + expectedWidth
                    );
                }

                for (int j = 0; j < line.length(); j += 2) {
                    char c = line.charAt(j);

                    if ("#TBP.*+".indexOf(c) == -1) {
                        throw new InvalidLevelFormatException(
                                "Invalid character '" + c + "' at line " + lineNumber + ", column " + (j / 2 + 1)
                        );
                    }

                    boolean isBorder = lineNumber == 1 || lineNumber == lines.size() || j == 0 || j == (line.length() - 1);
                    if (isBorder && c != '#') {
                        throw new InvalidLevelFormatException(
                                "Border at line " + lineNumber + ", column " + (j / 2 + 1) + " must be '#'"
                        );
                    }

                    int cellX = gridHeight;
                    int cellY = j / 2;

                    if (c == '*') {
                        staticObjects.add(new Target(cellX, cellY));
                        dynamicObjects.add(new Box(cellX, cellY));
                    } else if (c == '+') {
                        staticObjects.add(new Target(cellX, cellY));
                        player = new Player(cellX, cellY);
                        dynamicObjects.add(player);
                        playerCount++;
                    } else {
                        switch (c) {
                            case 'P':
                                player = new Player(cellX, cellY);
                                dynamicObjects.add(player);
                                playerCount++;
                                break;
                            case 'B':
                                dynamicObjects.add(new Box(cellX, cellY));
                                break;
                            case 'T':
                                staticObjects.add(new Target(cellX, cellY));
                                break;
                            case '#':
                                staticObjects.add(new Wall(cellX, cellY));
                                break;
                        }
                    }

                    if (j + 1 < line.length()) {
                        char separator = line.charAt(j + 1);
                        if (separator != ' ') {
                            throw new InvalidLevelFormatException(
                                    "Expected space after cell at line " + lineNumber + ", column " + (j / 2 + 1)
                            );
                        }
                    }
                }

                gridHeight++;
            }

            if (playerCount != 1) {
                throw new InvalidLevelFormatException(
                        "Level must contain exactly one player (P), found: " + playerCount
                );
            }

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        } catch (InvalidLevelFormatException e) {
            System.err.println("Invalid level format: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Adds a game object to the map based on a character symbol.
     *
     * @param c Character representing the object ('P', 'B', 'T', '#').
     * @param x Row coordinate.
     * @param y Column coordinate.
     */
    private void addObject(char c, int x, int y) {
        switch(c) {
            case 'P':
                player = new Player(x, y);
                dynamicObjects.add(player);
                break;
            case 'B':
                dynamicObjects.add(new Box(x, y));
                break;
            case 'T':
                staticObjects.add(new Target(x, y));
                break;
            case '#':
                staticObjects.add(new Wall(x, y));
                break;
        }
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

    /**
     * Resets the game map with a new level file.
     *
     * @param filename path to the level file
     */
    public void reset(String filename) {
        staticObjects.clear();
        dynamicObjects.clear();
        gridHeight = 0;
        gridWidth = 0;
        player = null;

        loadFromFile(filename);
        draw();
        System.out.println("New game!");
    }

    /**
     * Restarts the game and emits a restart event.
     *
     * @param path level file path
     */
    public void restart(String path) {
        reset(path);
        emit(new GameEvent(GameEventType.RESTART, null, null, path));
    }

    /**
     * Writes the current game state to a file.
     *
     * @param filename file path
     */
    public void writeToFile(String filename) {
        String path = "src/main/resources/" + filename;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            char[][] grid = new char[gridHeight][gridWidth];

            for (int i = 0; i < gridHeight; i++) {
                Arrays.fill(grid[i], '.');
            }

            for (GameObject obj : staticObjects) {
                int x = obj.getPosition().getX();
                int y = obj.getPosition().getY();
                grid[x][y] = obj.getSymbol();
            }

            for (Movable obj : dynamicObjects) {
                if (obj instanceof Box box) {
                    int x = box.getPosition().getX();
                    int y = box.getPosition().getY();

                    boolean onTarget = false;
                    for (GameObject sObj : staticObjects) {
                        if (sObj instanceof Target target &&
                                target.getPosition().equals(box.getPosition())) {
                            onTarget = true;
                            break;
                        }
                    }

                    grid[x][y] = onTarget ? '*' : 'B';
                }
            }

            if (player != null) {
                int x = player.getPosition().getX();
                int y = player.getPosition().getY();

                boolean onTarget = false;
                for (GameObject sObj : staticObjects) {
                    if (sObj instanceof Target target &&
                            target.getPosition().equals(player.getPosition())) {
                        onTarget = true;
                        break;
                    }
                }

                grid[x][y] = onTarget ? '+' : 'P';
            }

            for (int i = 0; i < gridHeight; i++) {
                StringBuilder line = new StringBuilder();
                for (int j = 0; j < gridWidth; j++) {
                    line.append(grid[i][j]);
                    if (j < gridWidth - 1) line.append(' ');
                }
                writer.write(line.toString());
                writer.newLine();
            }

            System.out.println("Autosave successful: " + path);

        } catch (IOException e) {
            System.err.println("Autosave failed: " + e.getMessage());
        }
    }

    /**
     * Loads a level and redraws the map.
     *
     * @param filename level file path
     * @return true if successful
     */
    public boolean loadLevel(String filename) {
        try {
            staticObjects.clear();
            dynamicObjects.clear();
            gridHeight = 0;
            gridWidth = 0;
            player = null;

            loadFromFile(filename);
            draw();
            return true;

        } catch (Exception e) {
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
