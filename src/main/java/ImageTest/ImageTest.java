package ImageTest;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ImageTest extends Application {

    @Override

    public void start(Stage stage) {

        ImageView view = new ImageView(ImageLoader.load("player.png"));

        StackPane root = new StackPane(view);
        Scene scene = new Scene(root, 64, 64);
        stage.setTitle("Image Test");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
