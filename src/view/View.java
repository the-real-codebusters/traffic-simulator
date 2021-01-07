package view;

import controller.Controller;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class View {
    private Controller controller;
    private Stage stage;

    public View(Stage primaryStage) {
        this.stage = primaryStage;
        Canvas canvas = new Canvas(800, 600);
        BorderPane root = new BorderPane();
        root.setCenter(canvas);
        this.stage.setScene(new Scene(root));
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }
}

