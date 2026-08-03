package Sokobani;

import javafx.scene.image.ImageView;

/**
 * Abstract base class for all objects in the Sokobani game.
 * Each object has a position on the grid and can provide
 * a symbol and image representation.
 */
abstract class GameObject {

    /**
     * The position of this object on the grid.
     * Accessible to subclasses for direct updates.
     */
    protected Position position;

    /**
     * Creates a new game object at the specified coordinates.
     *
     * @param x initial x (row) coordinate
     * @param y initial y (column) coordinate
     */
    public GameObject(int x, int y) {
        position = new Position(x, y);
    }

    /**
     * Returns the current position of this object.
     *
     * @return the object's position
     */
    public Position getPosition() {
        return this.position;
    }

    /**
     * Returns the character symbol representing this object on the grid.
     * Subclasses must override this to provide their unique symbol.
     *
     * @return the character symbol for this object
     */
    public abstract char getSymbol();

    /**
     * Returns an ImageView for this object using its image file.
     * The image is loaded via {@link ImageLoader} and sized to 64x64.
     *
     * @return an ImageView representing this object
     */
    public ImageView getImageView() {
        ImageView view = new ImageView(ImageLoader.load(getImageName()));
        view.setFitWidth(64);
        view.setFitHeight(64);
        return view;
    }

    /**
     * Returns the filename of the image representing this object.
     *
     * @return the image filename
     */
    public abstract String getImageName();
}
