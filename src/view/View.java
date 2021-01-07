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

    private int tileWidth = 64;
    private final int tileWidthHalf = tileWidth / 2;
    private int tileHeight = 32;
    private final int tileHeightHalf = tileHeight / 2;


    public View(Stage primaryStage) {
        this.stage = primaryStage;
        Canvas canvas = new Canvas(800, 600);

        final int MAP_SIZE = 10;
        Image image = new Image(getClass().getResource("/greentile.png").toString());

        Label tileLabel = new Label();
        tileLabel.setFont(new Font("Arial", 20));

        BorderPane root = new BorderPane();
        root.setBottom(tileLabel);
        root.setCenter(canvas);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        double canvasCenterWidth = canvas.getWidth() / 2;
        double canvasCenterHeight = canvas.getHeight() / 2;

        for (int row = 0; row < MAP_SIZE; row++) {
            for (int col = 0; col < MAP_SIZE; col++) {

                ImageView tile = new ImageView(image);
                double tileX = (col + row) * tileWidthHalf;
                double tileY = (col - row) * tileHeightHalf;
                tile.setX(tileX);
                tile.setY(tileY);

                gc.drawImage(image, tileX + canvasCenterWidth - tileWidthHalf * MAP_SIZE, tileY + canvasCenterHeight);

                canvas.setOnMouseClicked(event -> {
                    double x = Math.floor(((event.getY()  / tileHeight) + ((event.getX()  / tileWidth))));
                    double y = Math.floor(((event.getY()) / tileHeight) - ((event.getX()) / tileWidth));

                    String text = "Tile coordinates: x: " + x + " y: " + y;
                    tileLabel.setText(text);
                });
            }
        }
        this.stage.setScene(new Scene(root));
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }
}

