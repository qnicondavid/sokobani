package Sokobani;

/**
 * Thrown when a Sokobani level has an invalid format.
 */
public class InvalidLevelFormatException extends Exception {

    /**
     * Creates a new exception with the given message.
     *
     * @param message the detail message
     */
    public InvalidLevelFormatException(String message) {
        super(message);
    }
}
