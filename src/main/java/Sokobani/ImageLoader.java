package Sokobani;

import javafx.scene.image.Image;
import java.util.HashMap;

/**
 * Loads and caches images for the Sokobani game.
 * Ensures each image is loaded only once and reused from memory.
 */
public class ImageLoader {

    private static final HashMap<String, Image> cache = new HashMap<>();

    /**
     * Loads an image by name. If already loaded, returns it from the cache.
     *
     * @param name the path or filename of the image
     * @return the loaded Image, or null if loading fails
     */
    public static Image load(String name) {
        if (!cache.containsKey(name)) {
            try {
                Image img = new Image(name);
                cache.put(name, img);
            } catch (Exception e) {
                System.err.println("Could not load image: " + name);
                return null;
            }
        }
        return cache.get(name);
    }
}
