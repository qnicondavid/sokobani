package Sokobani;

/**
 * Represents an event in the Sokobani game.
 *
 * @param type the type of the event
 * @param from the starting position related to the event
 * @param to the ending position related to the event
 * @param note optional note or description for the event
 */
public record GameEvent(GameEventType type, Position from, Position to, String note) {}
