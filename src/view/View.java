package view;

import controller.Controller;
import javafx.geometry.Point2D;
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
    private final int MAP_SIZE = 4;

    ScrollPane scrollPane = new ScrollPane();
    Canvas canvas = new Canvas(700, 500);

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

                System.out.println("TileX: " + tileX);
                System.out.println("TileY: " + tileY + "\n");

                System.out.println("StartX: " + (tileX + canvasCenterWidth - tileWidthHalf * MAP_SIZE));
                System.out.println("StartY: " + (tileY + canvasCenterHeight - tileHeightHalf * MAP_SIZE) + "\n");

                gc.drawImage(image, tileX + canvasCenterWidth - tileWidthHalf * MAP_SIZE,
                        tileY + canvasCenterHeight - tileHeightHalf * MAP_SIZE);

                canvas.setOnMouseClicked(event -> {

                    Point2D isoCoord = findTileCoord(event.getX(), event.getY());

                    String mousePos = "Mouse coordinates: x: " + event.getX() + " y: " + event.getY();
                    mousePosLabel.setText((mousePos));

                    String text = "Tile coordinates: x: " + Math.floor(isoCoord.getX() - (canvasCenterHeight/tileHeight))
                            + " y: " + Math.ceil(isoCoord.getY() + canvasCenterHeight/tileHeight);
                    isoCoordLabel.setText(text);
                });
            }
        }
        this.stage.setScene(new Scene(root));
    }

    public Point2D findTileCoord(double mouseX, double mouseY){
        double x = Math.floor(mouseY / tileHeight + mouseX  / tileWidth);
        double y = Math.floor(mouseX / tileWidth - mouseY / tileHeight);
        Point2D isoCoord = new Point2D(x, y);
        return isoCoord;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }
}

