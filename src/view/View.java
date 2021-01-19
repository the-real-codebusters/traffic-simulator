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
import model.Building;
import model.Field;

import java.util.HashMap;
import java.util.Map;


public class View {
    private Stage stage;

    private final double tileWidth = 128;
    private final double tileWidthHalf = tileWidth / 2;
    private final double tileHeight = 64;
    private final double tileHeightHalf = tileHeight / 2;
    private int mapWidth;
    private int mapDepth;

    // Dies scheint die maximal einstellbare Größe eines Canvas zu sein. Bei größeren Angaben crasht das Programm
    private Canvas canvas = new Canvas(4096, 4096);
    private double canvasCenterWidth = canvas.getWidth() / 2;
    private double canvasCenterHeight = canvas.getHeight() / 2;

    private AnchorPane anchorPane = new AnchorPane();
    private ScrollPane scrollPane = new ZoomableScrollPane(anchorPane);

    private BasicModel model;

    private Map<String, Image> imageCache = new HashMap<>();
    BuildingToImageMapping mapping;


    public View(Stage primaryStage, BasicModel model) {
        this.stage = primaryStage;
        this.model = model;
        mapping = new BuildingToImageMapping(model.getGamemode());
        scrollPane.setPrefSize(1000, 600);

        Label isoCoordLabel = new Label();
        isoCoordLabel.setFont(new Font("Arial", 15));

        Label mousePosLabel = new Label();
        mousePosLabel.setFont(new Font("Arial", 15));

        BorderPane root = new BorderPane();
        VBox vBox = new VBox();
        root.setBottom(vBox);
        vBox.getChildren().addAll(mousePosLabel, isoCoordLabel);
        root.setCenter(scrollPane);
        root.setTop(new MenuPane(model, this, canvas, mapping));

        anchorPane.getChildren().add(canvas);
        anchorPane.setStyle("-fx-border-color: red; -fx-border-width : 5 5 5 5");
        centerCanvasInScrollPane(scrollPane, canvas);

        showCoordinatesOnClick(mousePosLabel, isoCoordLabel);

        this.stage.setScene(new Scene(root));
    }


    /**
     * Zentriert das Canvas mittig auf dem Scrollpane, wenn z.B das Canvas größer ist, als der sichtbare Bereich
     * @param scrollPane auf dem ScrollPane wird das Canvas platziert
     * @param canvas soll mittig auf ScrollPane platziert werden
     */
    public void centerCanvasInScrollPane(ScrollPane scrollPane, Canvas canvas) {
        // Gibt die Höhe des Inhalts der ScrollPane an
        double h = scrollPane.getContent().getBoundsInLocal().getHeight();
        double y = (canvas.getBoundsInParent().getMaxY() + canvas.getBoundsInParent().getMinY()) / 2.0;
        // Gibt Höhe des sichtbaren Bereichs an
        double v = scrollPane.getViewportBounds().getHeight();
        double w = scrollPane.getViewportBounds().getWidth();
        scrollPane.setVvalue(scrollPane.getVmax() * ((y - 0.5 * v) / (h - v)));
        scrollPane.setHvalue(scrollPane.getVmax() * ((y - 0.5 * w) / (h - w)));
}

    //TODO Checken: Sind die Zuordnungen von row -> Depth, column -> width so richtig? Ist es überall konsistent?

    /**
     * Zeichnet Map auf Canvas anhand der Daten eines Arrays von Fields
     */
    public void drawMap(Field[][] fields) {
        // Es wird über das Array mit Breite mapWidth und Tiefe mapDepth iteriert
        for (int col = mapWidth-1; col >= 0; col--) {
            for (int row = 0; row < mapDepth; row++) {

                Field field = fields[row][col];
                Building building = field.getBuilding();
                if(building!=null && (building.getWidth() > 1 || building.getDepth() > 1)){
                    if(field.isBuildingOrigin()){
                        drawBuildingOverMoreTiles(field, building, row, col);
                    }
                }
                else {
                    Image image = getSingleFieldImage(col, row, fields);
                    drawTileImage(col, row, image, false);
                }
            }
        }
    }

    public void drawBuildingOverMoreTiles(Field field, Building building, int row, int column){
        String buildingName = field.getBuilding().getBuildingName();
        String name = mapping.getImageNameForBuildingName(buildingName);

        Image r = getResourceForImageName(name);
        double ratio = r.getHeight() / r.getWidth();
        double imageWidth = tileWidth + (tileWidth*0.5)*(building.getDepth()+building.getWidth()-2);
        double imageHeight = imageWidth*ratio;
        double heightRatio = imageHeight / tileHeight;

        Image im = getResourceForImageName(name, imageWidth, imageHeight);

        double tileX = (row + column) * tileWidthHalf;
        double tileY = (row - column)  * tileHeightHalf - tileHeightHalf*heightRatio + tileHeightHalf;
        Point2D drawOrigin = moveCoordinates(tileX, tileY);
        canvas.getGraphicsContext2D().drawImage(im, drawOrigin.getX(), drawOrigin.getY());
    }

    public Image getSingleFieldImage(int column, int row, Field[][] fields){
        String name;
        String buildingName;
        if(column < 0 || row < 0 || column >= mapWidth || row >= mapDepth) name = "black";
        else {
            Field field = fields[row][column];
            if(field.getHeight() < 0){
                buildingName = "water";
            }
            else if(field.getBuilding() == null) {
                throw new RuntimeException("Das muss man sich nochmal anschauen: kann ein Field ohne Building existieren?");
            }
            else  buildingName = field.getBuilding().getBuildingName();
            name = mapping.getImageNameForBuildingName(buildingName);
        }

        return getResourceForImageName(name, tileWidth, tileHeight);
    }

    public void drawTileImage(int column, int row, Image image, boolean transparent){

        // TileX und TileY berechnet Abstand der Position von einem Bild zum nächsten in Pixel
        // Zeichenreihenfolge von oben rechts nach unten links
        double tileX = (row + column) * tileWidthHalf;
        double tileY = (row - column) * tileHeightHalf;

        Point2D drawOrigin = moveCoordinates(tileX, tileY);

        if(transparent) canvas.getGraphicsContext2D().setGlobalAlpha(0.7);
        canvas.getGraphicsContext2D().drawImage(image, drawOrigin.getX(), drawOrigin.getY());
        canvas.getGraphicsContext2D().setGlobalAlpha(1);
    }

    private Point2D moveCoordinates(double tileX, double tileY){
        // Differenz zwischen Breite und Tiefe der Map
        double differenceWidthHeigth = mapWidth - mapDepth;

        double tileOffset = differenceWidthHeigth * 0.25;

        // Zeichenreihenfolge von oben rechts nach unten links
        double startX = tileX + canvasCenterWidth - tileWidthHalf * mapWidth + (tileOffset * tileWidth);
        double startY = tileY + canvasCenterHeight - tileHeightHalf - (tileOffset * tileHeight);
        System.out.println(startX+"  "+startY);
        return new Point2D(startX, startY);
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
        System.out.println(mouseX + "  "+mouseY+"  "+x+"  "+y);
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

    public Image getResourceForImageName(String imageName, double width, double height) {

        Image cachedImage = imageCache.get(imageName+(int)width+(int)height);
        if (cachedImage != null) {
            return cachedImage;
        }


        String gamemode = model.getGamemode();
        Image image = new Image(
                    "/" + gamemode + "/" + imageName + ".png",
                    width,
                    height,
                    false,
                    true);
        imageCache.put(imageName+(int)image.getWidth()+(int)image.getHeight(), image);
        return image;
    }

    public Image getResourceForImageName(String imageName){
        Image cachedImage = imageCache.get(imageName+"raw");
        if (cachedImage != null) {
            return cachedImage;
        }

        String gamemode = model.getGamemode();
        Image image = new Image("/"+gamemode+"/"+imageName+".png");
        imageCache.put(imageName+"raw", image);
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

    public double getTileWidth() {
        return tileWidth;
    }

    public double getTileHeight() {
        return tileHeight;
    }
}

