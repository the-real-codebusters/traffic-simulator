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
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;

public class View {
    private Controller controller;
    private Stage stage;

    private int tileWidth = 64;
    private final int tileWidthHalf = tileWidth / 2;
    private int tileHeight = 32;
    private final int tileHeightHalf = tileHeight / 2;
    private final int MAP_SIZE = 9;

    ScrollPane scrollPane = new ScrollPane();
    Canvas canvas = new Canvas(1000, 800);

    public View(Stage primaryStage) {
        this.stage = primaryStage;

        scrollPane.setContent(canvas);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);

        Image image = new Image(getClass().getResource("/greentile.png").toString());

        Label isoCoordLabel = new Label();
        isoCoordLabel.setFont(new Font("Arial", 15));

        Label mousePosLabel = new Label();
        mousePosLabel.setFont(new Font("Arial", 15));

        BorderPane root = new BorderPane();
        root.setPrefSize(700, 500);
        VBox vBox = new VBox();
        root.setBottom(vBox);
        vBox.getChildren().addAll(mousePosLabel, isoCoordLabel);
        root.setCenter(scrollPane);
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

                    double x = findTileCoordX(event.getX(), event.getY());
                    double y = findTileCoordY(event.getX(), event.getY());

                    String mousePos = "Mouse coordinates: x: " + event.getX() + " y: " + event.getY();
                    mousePosLabel.setText((mousePos));

                    String text = "Tile coordinates: x: " + x + " y: " + y;
                    isoCoordLabel.setText(text);
                });
            }
        }
        this.stage.setScene(new Scene(root));
    }

    public double findTileCoordX(double mouseX, double mouseY){
        double x = Math.floor(mouseY / tileHeight + mouseX  / tileWidth) - MAP_SIZE -1;
        return x;
    }

    public double findTileCoordY(double mouseX, double mouseY){
        double y = Math.floor(mouseX / tileWidth - mouseY / tileHeight) + MAP_SIZE -1;
        return y;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }
}

