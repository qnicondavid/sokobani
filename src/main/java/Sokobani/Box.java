package Sokobani;

/**
 * Represents a movable box in the Sokobani game.
 */
class Box extends GameObject implements Movable {

    /**
     * Creates a box at the specified coordinates.
     *
     * @param x the initial x (row) coordinate
     * @param y the initial y (column) coordinate
     */
    public Box(int x, int y) {
        super(x, y);
    }

    /**
     * Moves the box by the given amounts.
     *
     * @param dx change in x (row) direction
     * @param dy change in y (column) direction
     */
    @Override
    public void move(int dx, int dy) {
        super.position.setX(super.position.getX() + dx);
        super.position.setY(super.position.getY() + dy);
    }

    /**
     * Returns the symbol representing the box.
     *
     * @return the character 'B'
     */
    @Override
    public char getSymbol() {
        return 'B';
    }

    /**
     * Returns the image filename for the box.
     *
     * @return "box.png"
     */
    @Override
    public String getImageName() {
        return "box.png";
    }
}
