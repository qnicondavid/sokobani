package Sokobani;

import java.util.*;

/** In the root directory:
 *  Compile: mvn compile
 *  Run: mvn exec:java
 */

/**
 * Main class for running the Sokobani game.
 *
 * Handles user input and interacts with {@link GameMap} via {@link InputHandler}.
 */
public class Game {

    /**
     * Entry point of the Sokobani game.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        GameMap map = new GameMap();
        Scanner in = new Scanner(System.in);
        InputHandler inputHandler = new InputHandler();

        while (true) {
            System.out.print("Enter command (w/a/s/d - move, 'save', 'load', 'r' - reset, 'q' - quit): ");
            String line = in.nextLine().trim().toLowerCase();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+");
            String cmd = parts[0];
            String filename = (parts.length > 1 ? parts[1] : null);

            switch (cmd) {
                case "w" -> inputHandler.fireMoveUp(map);
                case "s" -> inputHandler.fireMoveDown(map);
                case "a" -> inputHandler.fireMoveLeft(map);
                case "d" -> inputHandler.fireMoveRight(map);
                case "r" -> inputHandler.fireRestart(map);
                case "q" -> inputHandler.fireQuit(map);

                case "save" -> {
                    if (filename == null) {
                        System.out.print("Enter filename (default: save.txt): ");
                        filename = in.nextLine().trim();
                        if (filename.isEmpty()) filename = "save.txt";
                    }
                    try {
                        map.writeToFile(filename);
                        System.out.println("Saved to " + filename);
                    } catch (Exception e) {
                        System.out.println("Save failed: " + e.getMessage());
                    }
                }

                case "load" -> {
                    if (filename == null) {
                        System.out.print("Enter filename of a saved game: ");
                        filename = in.nextLine().trim();
                    }
                    try {
                        map.reset("src/main/resources/" + filename);
                    } catch (Exception e) {
                        System.out.println("Load failed: " + e.getMessage());
                    }
                }

                default ->
                    System.out.println("Incorrect command. Use w/a/s/d - move, 'save', 'load', 'r' - reset, 'q' - quit");
            }
        }
    }
}
