package view;

import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import model.BasicModel;
import model.Field;

import java.util.HashMap;
import java.util.Map;


public class View {
    private Stage stage;

    private final double tileWidth = 64;
    private final double tileWidthHalf = tileWidth / 2;
    private final double tileHeight = 32;
    private final double tileHeightHalf = tileHeight / 2;
    private int mapWidth;
    private int mapDepth;

    private Canvas canvas = new Canvas(800, 500);
    private double canvasCenterWidth = canvas.getWidth() / 2;
    private double canvasCenterHeight = canvas.getHeight() / 2;

    private AnchorPane anchorPane = new AnchorPane();
    private ScrollPane scrollPane = new ZoomableScrollPane(anchorPane);

    private BasicModel model;

    private Map<String, Image> imageCache = new HashMap<>();


    public View(Stage primaryStage, BasicModel model) {
        this.stage = primaryStage;
        this.model = model;

        Label isoCoordLabel = new Label();
        isoCoordLabel.setFont(new Font("Arial", 15));

        Label mousePosLabel = new Label();
        mousePosLabel.setFont(new Font("Arial", 15));

        BorderPane root = new BorderPane();
        root.setPrefSize(1024, 768);
        VBox vBox = new VBox();
        root.setBottom(vBox);
        vBox.getChildren().addAll(mousePosLabel, isoCoordLabel);
        root.setCenter(scrollPane);
        root.setTop(new MenuPane(model, this, canvas));
        anchorPane.getChildren().add(canvas);
        anchorPane.setStyle("-fx-border-color: red");

        showCoordinatesOnClick(mousePosLabel, isoCoordLabel);

        this.stage.setScene(new Scene(root));
    }

    /**
     * Zeichnet Map auf Canvas anhand der Daten eines Arrays von Fields, das die Koordinaten sowie den FieldType
     * enthält
     */
    public void drawMap(Field[][] fields) {
        // Es wird über das Array mit Breite mapWidth und Tiefe mapDepth iteriert
        for (int col = 0; col < mapWidth; col++) {
            for (int row = mapDepth - 1; row >= 0; row--) {

                // Bilder werden auf Canvas anhand von fieldType gezeichnet
                Image image = getSingleFieldImage(col, row, fields);
                drawTileImage(col, row, image, false);
            }
        }
    }

    public Image getSingleFieldImage(int column, int row, Field[][] fields){
        //TODO: Methode zur Ermittlung des gewünschten Bildes anhand des FieldTypes

        String name;
        if(column < 0 || row < 0 || column >= mapWidth || row >= mapDepth) name = "black";
        else if(fields[column][row].getFieldType().equals("grey")) name = "Bodenplatte_Gras";
        else name = "greentile";
        return getResourceForImageName(name, true);
    }

    public void drawTileImage(int row, int column, Image image, boolean transparent){

        // TileX und TileY berechnet Abstand der Position von einem Bild zum nächsten in Pixel
        // Zeichenreihenfolge von oben rechts nach unten links
        double tileX = (row + column) * tileWidthHalf;
        double tileY = (row - column) * tileHeightHalf;

        // Differenz zwischen Breite und Tiefe der Map
        double differenceWidthHeigth = mapWidth - mapDepth;

        double tileOffset = differenceWidthHeigth * 0.25;

        // Zeichenreihenfolge von oben rechts nach unten links
        double startX = tileX + canvasCenterWidth - tileWidthHalf * mapWidth + (tileOffset * tileWidth);
        double startY = tileY + canvasCenterHeight - tileHeightHalf - (tileOffset * tileHeight);

        if(transparent) canvas.getGraphicsContext2D().setGlobalAlpha(0.7);
        canvas.getGraphicsContext2D().drawImage(image, startX, startY);
        canvas.getGraphicsContext2D().setGlobalAlpha(1);

    }

    /**
     * Soll die Koordinaten der Mausposition von Pixel zu isometrischen Koordinaten umrechnen
     *
     * @param mouseX x-Koordinate der Mausposition
     * @param mouseY y-Koordinate der Mausposition
     * @return ein Point2D mit isometrischen Koordinaten
     */
    public Point2D findTileCoord(double mouseX, double mouseY, double canvasCenterWidth, double canvasCenterHeight) {

        double offsetX = 0;
        double offsetY = 0;
        if(mapWidth%2 != 0){
            offsetX = tileWidthHalf/ tileWidth;
        }
        if(mapDepth%2 != 0){
            offsetY = tileHeightHalf/ tileHeight;
        }

        double x = Math.floor((mouseX/ tileWidth + mouseY/ tileHeight) - canvasCenterHeight/ tileHeight
                - (canvasCenterWidth/ tileWidth) + (mapWidth/2) + offsetX);
        double y = Math.floor((mouseX/ tileWidth - mouseY/ tileHeight) + canvasCenterHeight/ tileHeight
                - (canvasCenterWidth/ tileWidth) + (mapDepth/2) + offsetY );
        return new Point2D(x, y);
    }

    /**
     * Bei Mausklick, werden die Mauskoordinaten sowie die Koordinaten des angeklickten Tile ausgegeben
     *
     * @param mousePosLabel Label, das die Koordinaten der Mausposition in Pixel anzeigen soll
     * @param isoCoordLabel Label, das die Koordinaten des angeklickten Tile anzeigen soll
     */
    public void showCoordinatesOnClick(Label mousePosLabel, Label isoCoordLabel) {

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

    public Image getResourceForImageName(String imageName, boolean ínSizeOfOneTile){

        Image cachedImage = imageCache.get(imageName);
        if(cachedImage != null){
            return cachedImage;
        }

        String gamemode = model.getGamemode();
        Image image;

        if(ínSizeOfOneTile){
            image = new Image(
                    "/"+gamemode+"/"+imageName+".png",
                    tileWidth,
                    tileHeight,
                    false,
                    true);
            imageCache.put(imageName, image);
        }
        else {
            image = new Image("/"+gamemode+"/"+imageName+".png");
        }

        return image;
    }

    public void setMapWidth(int mapWidth) {
        this.mapWidth = mapWidth;
    }

    public void setMapDepth(int mapDepth) {
        this.mapDepth = mapDepth;
    }

    public double getCanvasCenterWidth() {
        return canvasCenterWidth;
    }

    public double getCanvasCenterHeight() {
        return canvasCenterHeight;
    }
}

