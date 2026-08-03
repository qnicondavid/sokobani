package Sokobani;

/**
 * Listener interface for receiving game events in Sokobani.
 */
public interface GameEventListener {

    /**
     * Called when a game event occurs.
     *
     * @param e the event that occurred
     */
    void onEvent(GameEvent e);
}
