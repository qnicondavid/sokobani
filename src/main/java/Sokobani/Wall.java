package Sokobani;

/**
 * Represents a wall in the Sokobani game.
 */
class Wall extends GameObject {

    /**
     * Creates a wall at the specified coordinates.
     *
     * @param x the x (row) coordinate
     * @param y the y (column) coordinate
     */
    public Wall(int x, int y) {
        super(x, y);
    }

    /**
     * Returns the symbol representing the wall.
     *
     * @return the character '#'
     */
    @Override
    public char getSymbol() {
        return '#';
    }

    /**
     * Returns the image filename for the wall.
     *
     * @return "wall.png"
     */
    @Override
    public String getImageName() {
        return "wall.png";
    }
}
