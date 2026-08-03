package Sokobani;

/**
 * Represents a floor tile in the Sokobani game.
 */
public class Floor extends GameObject {

    /**
     * Creates a floor tile at the specified coordinates.
     *
     * @param x the x (row) coordinate
     * @param y the y (column) coordinate
     */
    public Floor(int x, int y) {
        super(x, y);
    }

    /**
     * Returns the symbol representing the floor.
     *
     * @return the character '.'
     */
    @Override
    public char getSymbol() {
        return '.';
    }

    /**
     * Returns the image filename for the floor tile.
     *
     * @return "floor.png"
     */
    @Override
    public String getImageName() {
        return "floor.png";
    }
}
