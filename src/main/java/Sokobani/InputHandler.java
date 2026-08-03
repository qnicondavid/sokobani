package Sokobani;

/**
 * Handles user input actions and translates them into operations
 * on the {@link GameMap}. Each method corresponds to a specific
 * command such as movement, restarting the level, or quitting the game.
 */
public class InputHandler {

    /**
     * Triggers an attempt to move the player up.
     *
     * @param map the game map on which the movement is performed
     */
    public void fireMoveUp(GameMap map) {
        map.attemptToMovePlayer(-1, 0);
    }

    /**
     * Triggers an attempt to move the player down.
     *
     * @param map the game map on which the movement is performed
     */
    public void fireMoveDown(GameMap map) {
        map.attemptToMovePlayer(1, 0);
    }

    /**
     * Triggers an attempt to move the player left.
     *
     * @param map the game map on which the movement is performed
     */
    public void fireMoveLeft(GameMap map) {
        map.attemptToMovePlayer(0, -1);
    }

    /**
     * Triggers an attempt to move the player right.
     *
     * @param map the game map on which the movement is performed
     */
    public void fireMoveRight(GameMap map) {
        map.attemptToMovePlayer(0, 1);
    }

    /**
     * Restarts the current level by reloading the map file.
     *
     * @param map the game map to be reset
     */
    public void fireRestart(GameMap map) {
        map.restart("src/main/resources/level1.txt"); 
    }

    /**
     * Quits the game by delegating to the map's quit functionality.
     *
     * @param map the game map controlling the quit action
     */
    public void fireQuit(GameMap map) {
        map.quit();
    }
}
