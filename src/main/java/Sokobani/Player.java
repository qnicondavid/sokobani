package Sokobani;

/**
 * Represents the player in the game.
 */
class Player extends GameObject implements Movable {

    /**
     * Creates a player at the given coordinates.
     *
     * @param x the initial x (row) coordinate
     * @param y the initial y (column) coordinate
     */
    public Player(int x, int y) {
        super(x, y);
    }

    /**
     * Moves the player by the given amounts.
     *
     * @param dx change in x direction
     * @param dy change in y direction
     */
    @Override
    public void move(int dx, int dy) {
        super.position.setX(super.position.getX() + dx);
        super.position.setY(super.position.getY() + dy);
    }

    /**
     * Returns the symbol representing the player.
     *
     * @return the character 'P'
     */
    @Override
    public char getSymbol() {
        return 'P';
    }

    /**
     * Returns the image filename for the player.
     *
     * @return "player.png"
     */
    @Override
    public String getImageName() {
        return "player.png";
    }
}
