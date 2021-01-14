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
import model.BasicModel;
import model.Field;


public class View {
    private Stage stage;

    private final int tileWidth = 64;
    private final int tileWidthHalf = tileWidth / 2;
    private final int tileHeight = 32;
    private final int tileHeightHalf = tileHeight / 2;
    private int mapWidth;
    private int mapDepth;

    private Canvas canvas = new Canvas(700, 500);
    private double canvasCenterWidth = canvas.getWidth() / 2;
    private double canvasCenterHeight = canvas.getHeight() / 2;
    private AnchorPane anchorPane = new AnchorPane();
    private ScrollPane scrollPane = new ZoomableScrollPane(anchorPane);

    private BasicModel model;


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
        scrollPane.setStyle("-fx-background-color: black");
        anchorPane.getChildren().add(canvas);

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
            for (int row = 0; row < mapDepth; row++) {

                // Bilder werden auf Canvas anhand von fieldType gezeichnet
                Image image = getSingleFieldImage(col, row, fields);
                drawTileImage(col, row, image);
            }
        }
    }

    public Image getSingleFieldImage(int column, int row, Field[][] fields){
        //TODO: Methode zur Ermittlung des gewünschten Bildes anhand des FieldTypes

        String name;
        if(column < 0 || row < 0 || column >= mapWidth || row >= mapWidth) name = "black3";
        else if(fields[column][row].getFieldType().equals("grey")) name = "Bodenplatte_Gras";
        else name = "greentile";
        return getResourceForImageName(name, true);
    }

    public void drawTileImage(int tileXCoord, int tileYCoord, Image image){

        // TileX und TileY berechnet Abstand der Position von einem Bild zum nächsten
        double tileX = (tileXCoord + tileYCoord) * tileWidthHalf;
        double tileY = (tileXCoord - tileYCoord) * tileHeightHalf;

        // Bilder sollen ausgehend vom Mittelpunkt des Canvas gezeichnet werden
        double startX = tileX + canvasCenterWidth - tileWidthHalf * mapWidth;
        double startY = tileY + canvasCenterHeight;

        canvas.getGraphicsContext2D().drawImage(image, startX, startY);
    }

    /**
     * Soll die Koordinaten der Mausposition von Pixel zu isometrischen Koordinaten umrechnen
     *
     * @param mouseX x-Koordinate der Mausposition
     * @param mouseY y-Koordinate der Mausposition
     * @return ein Point2D mit isometrischen Koordinaten
     */
    public Point2D findTileCoord(double mouseX, double mouseY, double canvasCenterWidth, double canvasCenterHeight) {
        double x = Math.floor((mouseY/tileHeight + mouseX/tileWidth) - (canvasCenterHeight/tileHeight) - 5 + mapDepth/2 - 1);
        double y = Math.floor((mouseX/tileWidth - mouseY/tileHeight) + (canvasCenterHeight/tileHeight) - 5 + mapWidth/2);
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
        String gamemode = model.getGamemode();
        Image image;
        if(ínSizeOfOneTile){
            image = new Image(
                    getClass().getResource("/"+gamemode+"/"+imageName+".png").toString(),
                    tileWidth,
                    tileHeight,
                    false,
                    true);
        }
        else {
            image = new Image(getClass().getResource("/"+gamemode+"/"+imageName+".png").toString());
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

