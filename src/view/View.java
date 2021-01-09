package view;

import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import model.Field;


public class View {
    private Stage stage;

    private final int tileWidth = 64;
    private final int tileWidthHalf = tileWidth / 2;
    private final int tileHeight = 32;
    private final int tileHeightHalf = tileHeight / 2;
    private int mapSize;

    private Canvas canvas = new Canvas(700, 500);
    private double canvasCenterWidth = canvas.getWidth() / 2;
    private double canvasCenterHeight = canvas.getHeight() / 2;
    private AnchorPane anchorPane = new AnchorPane();
    private ScrollPane scrollPane = new ZoomableScrollPane(anchorPane);

    public View(Stage primaryStage) {
        this.stage = primaryStage;

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
        scrollPane.setStyle("-fx-background-color: black");
        anchorPane.getChildren().add(canvas);

        showCoordinatesOnClick(mousePosLabel, isoCoordLabel);

        this.stage.setScene(new Scene(root));
    }

    /**
     * Zeichnet Map auf Canvas
     * Überlegung: später könnte ein Array als Parameter übergeben werden, in dem die Tile-Typen mit Koordinaten
     * angegenen werden (Beispeil: Bodenfeld [0,0], Wasser [1,2] etc.)
     */
    public void drawMap(Field[][] fields) {
        Image greyGrassImage = new Image(getClass().getResource("/Bodenplatte_Gras.png").toString());
        Image greenGrassImage = new Image(getClass().getResource("/greentile.png").toString());

        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Es wird wie über ein 2D-Array mit größe MAP_SIZE iteriert
        for (int row = 0; row < fields.length; row++) {
            for (int col = 0; col < fields.length; col++) {

                // TileX und TileY berechnet Abstand der Position von einem Bild zum nächsten
                double tileX = (col + row) * tileWidthHalf;
                double tileY = (col - row) * tileHeightHalf;

                // Bilder sollen ausgehend vom Mittelpunkt des Canvas gezeichnet werden
                double startX = tileX + canvasCenterWidth - tileWidthHalf * mapSize;
                double startY = tileY + canvasCenterHeight;

                // Bilder werden auf Canvas gezeichnet
                if(fields[row][col].getFieldType().equals("grass")){
                    gc.drawImage(greyGrassImage, startX, startY);
                } else {
                    gc.drawImage(greenGrassImage, startX, startY);
                }
                //TODO: Methode zur Ermittlung des gewünschten Bildes anhand des FieldTypes
            }
        }
    }

    /**
     * Soll die Koordinaten der Mouseposition von Pixel zu isometrischen Koordinaten umrechnen
     *
     * @param mouseX x-Koordinate der Mouseposition
     * @param mouseY y-Koordinate der Mouseposition
     * @return ein Point2D mit isometrischen Koordinaten
     */
    public Point2D findTileCoord(double mouseX, double mouseY, double canvasCenterWidth, double canvasCenterHeight) {
        double x = Math.floor((mouseY / tileHeight + mouseX / tileWidth) - (canvasCenterHeight / tileHeight) -5 + mapSize /2 - 1);
        double y = Math.floor((mouseX / tileWidth - mouseY / tileHeight) + (canvasCenterHeight / tileHeight) -5 + mapSize /2);
        return new Point2D(x, y);
    }


    /**
     * Bei Mousklick, werden die Mousekoordinaten sowie die Koordinaten des angeklickten Tile ausgegeben
     * @param mousePosLabel Label, das die Koordinaten der Mausposition in Pixel anzeigen soll
     * @param isoCoordLabel Label, das die Koordinaten des angeklickten Tile anzeigen soll
     */
    public void showCoordinatesOnClick(Label mousePosLabel, Label isoCoordLabel){

        // Ereignisbehandlung
        canvas.setOnMouseClicked(event -> {
            double mouseX = event.getX();
            double mouseY = event.getY();

            String mouseCoords = "Mouse coordinates: x: " + mouseX + " y: " + mouseY;
            mousePosLabel.setText(mouseCoords);

            // Findet isometrische Koordinaten der Mouseposition
            Point2D isoCoord = findTileCoord(mouseX, mouseY, canvasCenterWidth, canvasCenterHeight);

            String tileCoords = "Tile coordinates: x: " + isoCoord.getX() + " y: " + isoCoord.getY();
            isoCoordLabel.setText(tileCoords);
        });
    }

    public void setMapSize(int mapSize) {
        this.mapSize = mapSize;
    }
}

