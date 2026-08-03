package Sokobani;

/**
 * Represents the types of events that can occur during the Sokobani game.
 */
public enum GameEventType {
    /** Player moves without pushing a box. */
    MOVE,

    /** Player pushes a box. */
    PUSH,

    /** Player completes the level. */
    WIN,

    /** The game is restarted. */
    RESTART,

    /** An invalid move was attempted. */
    INVALID_MOVE
}
