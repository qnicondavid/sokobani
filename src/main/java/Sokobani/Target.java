package Sokobani;

/**
 * Represents a target in the Sokobani game.
 */
class Target extends GameObject {

    /**
     * Creates a target at the specified coordinates.
     *
     * @param x the x (row) coordinate
     * @param y the y (column) coordinate
     */
    public Target(int x, int y) {
        super(x, y);
    }

    /**
     * Returns the symbol representing the target.
     *
     * @return the character 'T'
     */
    @Override
    public char getSymbol() {
        return 'T';
    }

    /**
     * Returns the image filename for the target.
     *
     * @return "target.png"
     */
    @Override
    public String getImageName() {
        return "target.png";
    }
}
