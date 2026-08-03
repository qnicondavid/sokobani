package Sokobani;

class Position {

    /** The x (row) coordinate. */
    private int x;

    /** The y (column) coordinate. */
    private int y;

    /**
     * Constructs a new {@code Position} with the given coordinates.
     *
     * @param x the row coordinate
     * @param y the column coordinate
     */
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the x (row) coordinate.
     *
     * @return the x coordinate
     */
    public int getX() {
        return this.x;
    }

    /**
     * Returns the y (column) coordinate.
     *
     * @return the y coordinate
     */
    public int getY() {
        return this.y;
    }

    /**
     * Sets the x (row) coordinate.
     *
     * @param x the new x coordinate
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Sets the y (column) coordinate.
     *
     * @param y the new y coordinate
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * Returns a string representation of this position in the form (x,y).
     *
     * @return a string representing this position
     */
    @Override
    public String toString() {
        return "(" + Integer.toString(x) + "," + Integer.toString(y) + ")";
    }

    /**
     * Compares this position to another {@code Position} for equality.
     * Two positions are equal if they have the same x and y coordinates.
     *
     * @param position another {@code Position} to compare with
     * @return {@code true} if both positions have equal coordinates, {@code false} otherwise
     */
    public boolean equals(Position position) {
        if (position.getX() == this.x && position.getY() == this.y)
            return true;
        return false;
    }
}
