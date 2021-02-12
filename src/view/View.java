package view;

import controller.Controller;
import javafx.animation.*;
import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import model.*;


import java.util.*;


public class View {
    private Stage stage;

    private double tileImageWidth = 128;
    private double tileImageWidthHalf = tileImageWidth / 2;
    private double tileImageHeight = 64;
    private double tileImageHeightHalf = tileImageHeight / 2;
    double heightOffset = 16;
    private int mapWidth;
    private int mapDepth;

    private Controller controller;

    // Gint für den Namen eines Bildes das ursprüngliche Verhältnis von Höhe und Breite des Bildes an
    private Map<String, Double> imageNameToImageRatio = new HashMap<>();

    private Canvas canvas = new Canvas(1200, 600);
    private double canvasCenterWidth = canvas.getWidth() / 2;
    private double canvasCenterHeight = canvas.getHeight() / 2;


    // Gibt die Verschiebung des sichtbaren Bereichs der Karte in x-Richtung an
    private double cameraOffsetX = 0.0;
    // Gibt die Verschiebung des sichtbaren Bereichs der Karte in y-Richtung an
    private double cameraOffsetY = 0.0;
    private Tile[][] fields;

    private double previousMouseX = -1.0;
    private double previousMouseY = -1.0;

    private MenuPane menuPane;

    private Map<String, Image> imageCache = new HashMap<>();
    private ObjectToImageMapping objectToImageMapping;
    private Map<String, ImagePattern> imagePatternCache = new HashMap<>();

    private double zoomFactor = 1.0;
    private static final double MAX_SCALE = 10.0d;
    private static final double MIN_SCALE = .1d;

    private double tickDuration = 1;
    BorderPane borderPane;
    private ParallelTransition parallelTransition;
    private AnimationTimer timer;

    private Map<List<Point2D>, Point2D> rowColToCanvasCoordinates = new LinkedHashMap<>();

    public View(Stage primaryStage, BasicModel model) {
        this.stage = primaryStage;
        objectToImageMapping = new ObjectToImageMapping(model.getGamemode());
        fields = model.getFieldGridOfMap();

        Label isoCoordLabel = new Label();
        isoCoordLabel.setFont(new Font("Arial", 15));

        Label mousePosLabel = new Label();
        mousePosLabel.setFont(new Font("Arial", 15));

        Label cornerLabel = new Label();
        mousePosLabel.setFont(new Font("Arial", 15));

        borderPane = new BorderPane();
        VBox vBox = new VBox();
        borderPane.setBottom(vBox);
        vBox.getChildren().addAll(mousePosLabel, isoCoordLabel, cornerLabel);
        borderPane.setCenter(canvas);

        canvas.setFocusTraversable(true);
        showCoordinatesOnClick(mousePosLabel, isoCoordLabel, cornerLabel);
        scrollOnKeyPressed();
        scrollOnMouseDragged();

        zoom();

        this.stage.setScene(new Scene(borderPane));
    }

    public void generateMenuPane(Controller controller){
        menuPane = new MenuPane(controller, this, canvas, objectToImageMapping);
        borderPane.setTop(menuPane);
    }


    public void zoom (){
        canvas.setOnScroll(scrollEvent -> {
            double scrollDelta = scrollEvent.getDeltaY();
            double zoomFactor = Math.exp(scrollDelta * 0.01);
            tileImageWidth = tileImageWidth * zoomFactor;
            tileImageHeight = tileImageHeight * zoomFactor;

            tileImageWidthHalf = tileImageWidthHalf * zoomFactor;
            tileImageHeightHalf = tileImageHeightHalf * zoomFactor;

            heightOffset = heightOffset * zoomFactor;

            drawMap();

        });
    }
//
//    public void zoom2 (){
//        canvas.setOnScroll(event -> {
//            double delta = 1.2;
//            double scale = canvas.getScaleY(); // currently we only use Y, same value is used for X
//            double oldScale = scale;
//
//            if (event.getDeltaY() < 0) scale /= delta;
//            else scale *= delta;
//
//            scale = clamp(scale, MIN_SCALE, MAX_SCALE);
//            double f = (scale / oldScale) - 1;
//            double dx = (event.getSceneX() - (canvas.getBoundsInParent().getWidth() / 2 + canvas.getBoundsInParent().getMinX()));
//            double dy = (event.getSceneY() - (canvas.getBoundsInParent().getHeight() / 2 + canvas.getBoundsInParent().getMinY()));
//
//            canvas.setScaleY(scale);
//            // note: pivot value must be untransformed, i. e. without scaling
//            setPivot(f * dx, f * dy);
//            event.consume();
//        });
//    }


//    public void setPivot( double x, double y) {
//        canvas.setTranslateX(canvas.getTranslateX()-x);
//        canvas.setTranslateY(canvas.getTranslateY()-y);
//    }
//
//    public static double clamp( double value, double min, double max) {
//
//        if( Double.compare(value, min) < 0)
//            return min;
//
//        if( Double.compare(value, max) > 0)
//            return max;
//
//        return value;
//    }

    public double getQuadraticTileWidthOrDepth(){
        return Math.sqrt(Math.pow(tileImageWidth/2, 2) + Math.pow(tileImageHeight/2, 2));
    }

    /**
     * Verschiebt den zweidimensionalen Punkt point um die angegebenen Tiles in X- bze Y-Richtung
     * @param point
     * @param changedTilesWitdh
     * @param changedTilesDepth
     * @return Einen dementsprechend verschobenen Punkt
     */
    public Point2D changePointByTiles(Point2D point, double changedTilesWitdh, double changedTilesDepth){
        double changeX = tileImageWidthHalf * changedTilesWitdh + changedTilesDepth*tileImageWidthHalf;
        double changeY = tileImageHeightHalf * changedTilesWitdh - changedTilesDepth*tileImageHeightHalf;
        return point.add(changeX, changeY);
    }

    /**
     * Ermögliche Verschieben der Karte mit den Pfeiltasten
     */
    public void scrollOnKeyPressed() {
        canvas.setOnKeyPressed(ke -> {
            double delta = tileImageWidth;
            if (ke.getCode() == KeyCode.DOWN) {
                cameraOffsetY += delta / 2;
            } else if (ke.getCode() == KeyCode.UP) {
                cameraOffsetY -= delta / 2;
            } else if (ke.getCode() == KeyCode.RIGHT) {
                cameraOffsetX += delta;
            } else if (ke.getCode() == KeyCode.LEFT) {
                cameraOffsetX -= delta;
            }
            drawMap();
        });
    }

    /**
     * Fügt dem Canvas die Reaktion hinzu: Wenn mit der Maus rechtsgeklickt ist, kann die Karte verschoben werden
     */
    public void scrollOnMouseDragged() {
        canvas.setOnMouseDragged(me -> {
            if (me.getButton().compareTo(MouseButton.SECONDARY) == 0) {
                double mousePosX = me.getX();
                double mousePosY = me.getY();
                double deltaX = previousMouseX - mousePosX;
                double deltaY = previousMouseY - mousePosY;

                //TODO Mit scrollOnMouseDraggedReleased umsetzen bzw Maxi dazu fragen
                if (Math.abs(deltaX) < 30 && Math.abs(deltaY) < 30 && previousMouseX != -1.0 && previousMouseY != -1.0) {
                    cameraOffsetX += deltaX;
                    cameraOffsetY += deltaY;
                    drawMap();
                }
                previousMouseX = mousePosX;
                previousMouseY = mousePosY;
            }
        });
    }

    //TODO Checken: Sind die Zuordnungen von row -> Depth, column -> width so richtig? Ist es überall konsistent?

    /**
     * Zeichnet Map auf Canvas anhand der Daten eines Arrays von Tiles
     */
    public void drawMap() {
//        numberOfDrawPol = 0;
        final String grass1 = "file:grass.png";
        Image grass = new Image(grass1);
        // Hintergrund wird schwarz gesetzt
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        int minimumX = (int) findTileCoord(0, 0).getX();
        int maximumX = (int) findTileCoord(canvas.getWidth(), canvas.getHeight()).getX();
        int minimumY = (int) findTileCoord(0, canvas.getHeight()).getY();
        int maximumY = (int) findTileCoord(canvas.getWidth(), 0).getY();


        int startRow = 0;
        int startCol = 0;
        int endCol = 0;

//        rowColToCanvasCoordinates.clear();

        // Es wird den sichtbaren Ausschnitt aus dem Array iteriert
        for (int col = maximumY; col >= minimumY; col--) {
            for (int row = minimumX; row <= maximumX; row++) {

                // Linke Ecke des Tiles
                Point2D drawOrigin = translateTileCoordsToCanvasCoords(row, col);
                if(drawOrigin.getX() > -tileImageWidth*4 && drawOrigin.getX() < canvas.getWidth()
                && drawOrigin.getY() > -tileImageHeightHalf*4 && drawOrigin.getY() < canvas.getHeight() + tileImageHeightHalf*4){


                //TODO fields von Controller holen, da mvc-model

                // Row und Column müssen innerhalb des 2d-Arrays liegen
                if (row >= 0 && col >= 0 && row < fields.length && col < fields[0].length) {
                    Tile field = fields[row][col];
                    Building building = field.getBuilding();
                    Map<String, Integer> cornerHeights = controller.getTileOfMapTileGrid(row, col).getCornerHeights();

//                    System.out.println("View: " + row + " " + col + " " + cornerHeights);

                    // Wenn das building größer als ein Tile ist, zeichne über mehrere Tiles
                    if (building != null && (building.getWidth() > 1 || building.getDepth() > 1)) {
                        if (field.isBuildingOrigin()) {

                            for(int i = col; i <= col + building.getDepth()-1; i++) {
                                // Obere Kante vom Gebäude mit Grassfläche übermalen
                                Image image = getGrassImage(i, row);
                                drawTileImage(drawOrigin, image, false, cornerHeights);
                            }
                            drawBuildingOverMoreTiles(field, building, row, col);
                        }
                        // obere ecke ist ein gebäude
                        if (row == building.getStartRow() && col == building.getStartColumn()) {
                            // Startzeile und Start/Endespalte merken
                            startRow = row + building.getWidth();
                            endCol = col;
                            startCol = endCol - building.getDepth()+2;
                            for(int i = row; i <= startRow; i++) {
                                // Rechte Kante vom Gebäude mit Grassfläche übermalen
                                Image image = getGrassImage(col, i);
                                drawTileImage(drawOrigin, image, false, cornerHeights);
                            }

                        }

                    } else {
                        // diese Zelle wurde vorher als Zelle neben einem Gebäude identifiziert
                        // zeichnet neben Gebäude, um Problem der Überlappung zu lösen
                        if (row == startRow && col >= startCol && col <= endCol) {
                            // und muss daher als Grass gezeichnet werden
                            Image image = getGrassImage(col, row);
                            drawTileImage(drawOrigin, image, false, cornerHeights);
                        }
                        else {

                            if(building.getBuildingName().equals("ground")) {

                                String absoluteTileHeight = controller.getTileOfMapTileGrid(row, col).absoluteHeigtToRelativeHeight(cornerHeights);

                                String buildingName = absoluteTileHeight;

                                Image image = getSingleFieldImage(col, row, fields);
                                String imageName = objectToImageMapping.getImageNameForObjectName(buildingName);

                                Image r = getResourceForImageName(imageName);
                                double ratio = r.getHeight() / r.getWidth();
//                            System.out.println("Ratio: " + ratio);

                                // Wenn das Verhältnis zwischen Breite und Höhe nicht 2:1 ist
                                if (ratio != 0.5) {
                                    drawGroundOverMoreTiles(drawOrigin, buildingName, image, cornerHeights);

                                } else {
//                                drawTileImage(drawOrigin, image, false, cornerHeights);
                                    drawGroundInOneTile(drawOrigin, image, cornerHeights);
                                }
                            }
                            else {
                                Image image = getSingleFieldImage(col, row, fields);
                                drawTileImage(drawOrigin, image, false, cornerHeights);
                            }

                            //TODO Polygone mit Wasser oder Gras Tiles zu zeichnen, je nach Höhe
                            Tile tile = fields[row][col];
                            int cornerHeightSouth = tile.getCornerHeights().get("cornerS");
                            int cornerHeightWest = tile.getCornerHeights().get("cornerW");
                            int cornerHeightNorth = tile.getCornerHeights().get("cornerN");
                            int cornerHeightEast = tile.getCornerHeights().get("cornerE");


                            /*if(row-1 >= 0){
                                cornerHeightSouth = fields[row+1][col].getCornerHeights().get("cornerW");
                                cornerHeightEast = fields[row+1][col+1].getCornerHeights().get("cornerW");
                                cornerHeightNorth = fields[row][col+1].getCornerHeights().get("cornerW");
                            }*/
                            drawPolygon( row, col, drawOrigin, cornerHeightNorth,cornerHeightEast,cornerHeightSouth,cornerHeightWest);
                        }
                    }
                }
//                    System.out.println("calls of polygon "+numberOfDrawPol);
            }
            }
        }


        // Zeichnet die Knoten des Graphen als gelbe Punkte ein
        if(controller!=null){
            controller.drawVertexesOfGraph();
        }

        // Zeichnet eine Vorschau, falls nötig
        Building selectedBuilding = menuPane.getSelectedBuilding();
        MouseEvent hoveredEvent = menuPane.getHoveredEvent();
        if(selectedBuilding != null && hoveredEvent != null){
            menuPane.drawHoveredImage(hoveredEvent, true);
        }
    }


//    int numberOfDrawPol = 0;
    public void drawPolygon(int row, int col , Point2D drawOrigin, int heightNorth, int heightEast, int heightSouth, int heightWest) {

//        numberOfDrawPol++;

        double xCoordOnCanvas = drawOrigin.getX();
//        double yCoordOnCanvas = drawOrigin.getY() - tileImageHeightHalf;
        double yCoordOnCanvas = drawOrigin.getY();
//
//        GraphicsContext gc = canvas.getGraphicsContext2D();
//        int numberOfPoints = 4;
        double heightShift = tileImageHeight / 4;
        double xCoordWest = xCoordOnCanvas;
        double yCoordWest = yCoordOnCanvas - heightWest * heightShift;

        double xCoordNorth = xCoordOnCanvas + tileImageWidthHalf;
        double yCoordNorth = yCoordOnCanvas - tileImageHeightHalf - heightNorth * heightShift;

        double xCoordEast = xCoordOnCanvas + tileImageWidth;
        double yCoordEast = yCoordOnCanvas - heightEast * heightShift;

        double xCoordSouth = xCoordOnCanvas + tileImageWidthHalf;
        double yCoordSouth = yCoordOnCanvas + tileImageHeightHalf - heightSouth * heightShift;

        double[] xCoords = {xCoordWest, xCoordSouth, xCoordEast, xCoordNorth};
        double[] yCoords = {yCoordWest, yCoordSouth, yCoordEast, yCoordNorth};

        Point2D west = new Point2D(xCoordWest, yCoordWest);
        Point2D north = new Point2D(xCoordNorth, yCoordNorth);
        Point2D east = new Point2D(xCoordEast, yCoordEast);
        Point2D south = new Point2D(xCoordSouth, yCoordSouth);
//
        List<Point2D> coordsOnCanvas = new ArrayList<>();
        coordsOnCanvas.add(west);
        coordsOnCanvas.add(north);
        coordsOnCanvas.add(east);
        coordsOnCanvas.add(south);


//        boolean shouldBeDrawed = true;
//        for(Point2D coord : coordsOnCanvas){
//            // Zeichne nur Tiles, die tatsächlich auf dem Canvas sichtbar sind
//            if ((coord.getX() < 0 - tileImageWidth || coord.getX() > canvas.getWidth() + tileImageWidth ||
//                    coord.getY() < 0 -tileImageHeight || coord.getY() > canvas.getHeight() + tileImageHeight)){
//
////                if ((coord.getX() < 0 || coord.getX() > canvas.getWidth() ||
////                        coord.getY() < 0 || coord.getY() > canvas.getHeight())){
//
//
////              gc.setStroke(Color.WHITE);
//                shouldBeDrawed = false;


                if(!rowColToCanvasCoordinates.keySet().contains(coordsOnCanvas)){
                    rowColToCanvasCoordinates.put(coordsOnCanvas, new Point2D(row, col));
//                    System.out.println(rowColToCanvasCoordinates.size());
                }
//            }
//        }
//        if(shouldBeDrawed){
//                            ImagePattern imagePattern;
//                if (heightWest < 0) {
//                    imagePattern = getImagePatternForGroundName("water");
//                } else {
//                    imagePattern = getImagePatternForGroundName("grass");
//                }
//                gc.setFill(imagePattern);
////            gc.setFill(Color.BLUEVIOLET);
//            gc.fillPolygon(xCoords, yCoords, numberOfPoints);
//                gc.strokePolygon(xCoords, yCoords, 4);
////            gc.setStroke(Color.WHITE);
//
////              gc.strokeText("N: " + heightNorth + " E " + heightEast + " S " + heightSouth + " W " + heightWest, xCoordOnCanvas, yCoordOnCanvas);
//
//            gc.setFill(Color.BLACK);
//        }



        //TODO Das Tile 0,0 ganz links wird manchmal je nach Position komisch angezeigt

    }

    public boolean isPointInsidePolygon(double mouseX, double mouseY, List<Point2D> coordsOnCanvas) {

        double x = mouseX;
        double y = mouseY;

        boolean inside = false;
        for (int i = 0, j = coordsOnCanvas.size() - 1; i < coordsOnCanvas.size(); j = i++) {

            double xi = coordsOnCanvas.get(i).getX();
            double yi = coordsOnCanvas.get(i).getY();

            double xj = coordsOnCanvas.get(j).getX();
            double yj = coordsOnCanvas.get(j).getY();

            boolean intersect = ((yi > y) != (yj > y)) && (x < (xj - xi) * (y - yi) / (yj - yi) + xi);
            if (intersect) inside = !inside;
        }

        return inside;

    }


    public Point2D findTileCoordNew(double mouseX, double mouseY) {
        Point2D newIsoCoord = new Point2D(0,0);
        for(Map.Entry<List<Point2D>, Point2D> entry : rowColToCanvasCoordinates.entrySet()){
            if(isPointInsidePolygon(mouseX, mouseY, entry.getKey())){
                newIsoCoord = entry.getValue();
            }
        }
        return newIsoCoord;
    }

    /**
     * Soll die Koordinaten der Mausposition von Pixel zu isometrischen Koordinaten umrechnen
     *
     * @param mouseX x-Koordinate der Mausposition
     * @param mouseY y-Koordinate der Mausposition
     * @return ein Point2D mit isometrischen Koordinaten
     */
    public Point2D findTileCoord(double mouseX, double mouseY) {

        double offsetX = 0;
        double offsetY = 0;
        if (mapWidth % 2 != 0) {
            offsetX = tileImageWidthHalf / tileImageWidth;
        }
        if (mapDepth % 2 != 0) {
            offsetY = tileImageHeightHalf / tileImageHeight;
        }

        double x = Math.floor((mouseX / tileImageWidth + mouseY / tileImageHeight) - canvasCenterHeight / tileImageHeight
                - (canvasCenterWidth / tileImageWidth) + (mapWidth / 2) + offsetX + cameraOffsetX / tileImageWidth +
                cameraOffsetY / tileImageHeight);

        double y = Math.floor((mouseX / tileImageWidth - mouseY / tileImageHeight) + canvasCenterHeight / tileImageHeight
                - (canvasCenterWidth / tileImageWidth) + (mapDepth / 2) + offsetY - cameraOffsetY / tileImageHeight +
                cameraOffsetX / tileImageWidth);

        return new Point2D(x, y);
    }


    /**
     * Zeichnet das Bild in ein Feld an der angegebenen Stelle
//     * @param column
//     * @param row
     * @param image
     * @param transparent
     */
    public void drawTileImage(Point2D drawOrigin, Image image, boolean transparent, Map<String, Integer> cornerHeights) {

        double heightAboveTile = image.getHeight() - tileImageHeight;

        double xCoordOnCanvas = drawOrigin.getX();
        double yCoordOnCanvas = drawOrigin.getY() - tileImageHeightHalf - heightAboveTile ;

//        double xCoordOnCanvas = drawOrigin.getX();
        // nehme cornerS als Referenzpunkt (bei allen Tiles wird immer cornerS als Höhenreferenz genommen)
//        double yCoordOnCanvas = drawOrigin.getY() - tileImageHeightHalf - heightOffset * cornerHeights.get("cornerS");

//        double yCoordOnCanvas;
//        if(cornerHeights.get("cornerS") == 0) {
//            yCoordOnCanvas = drawOrigin.getY() - tileImageHeightHalf ;
//        } else {
//            yCoordOnCanvas = drawOrigin.getY() - tileImageHeightHalf - tilePlacingOffset * cornerHeights.get("cornerS");
//        }


        if (transparent) canvas.getGraphicsContext2D().setGlobalAlpha(0.7);
        canvas.getGraphicsContext2D().drawImage(image, xCoordOnCanvas, yCoordOnCanvas);
        canvas.getGraphicsContext2D().setGlobalAlpha(1);
    }

    public void drawTileImage(int row, int column, Image image, boolean transparent) {

        double heightAboveTile = image.getHeight() - tileImageHeight;

        Point2D drawOrigin = translateTileCoordsToCanvasCoords(row, column);
        double xCoordOnCanvas = drawOrigin.getX();
        double yCoordOnCanvas = drawOrigin.getY() - heightAboveTile - tileImageHeightHalf;

        if (transparent) canvas.getGraphicsContext2D().setGlobalAlpha(0.7);
        canvas.getGraphicsContext2D().drawImage(image, xCoordOnCanvas, yCoordOnCanvas);
        canvas.getGraphicsContext2D().setGlobalAlpha(1);
    }

    /**
     * Speichert die Verhältnisse von Höhe und Breite für alle Bilder in einer Map
     */
    public void storeImageRatios(){
        for(String name : objectToImageMapping.getImageNames()){
            Image r = getResourceForImageName(name);
            double ratio = r.getHeight() / r.getWidth();
            imageNameToImageRatio.put(name, ratio);
        }
    }

    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * Zeichnet das Bild für ein Bodenfeld dessen Grafik ein Verhältnis von 2:1 zwischen Breite und Höhe hat und
     * Berücksichtigt zur Platzierung die Höhe an der unteren Ecke (cornerS)
     * @param image die Graphik für das zu zeichnende Bodenfeld
     */
    public void drawGroundInOneTile(Point2D drawOrigin, Image image, Map<String, Integer> cornerHeights) {

        double xCoordOnCanvas = drawOrigin.getX();

        // nehme cornerS als Referenzpunkt (bei allen Tiles wird immer cornerS als Höhenreferenz genommen)
        double yCoordOnCanvas = drawOrigin.getY() - tileImageHeightHalf - heightOffset * cornerHeights.get("cornerS");


        canvas.getGraphicsContext2D().drawImage(image, xCoordOnCanvas, yCoordOnCanvas);
        canvas.getGraphicsContext2D().setGlobalAlpha(1);
    }


    /**
     * Zeichnet das Bild für ein Bodenfeld dessen Grafik NICHT das "normale" Verhältnis von 2:1 zwischen Breite
     * und Höhe hat und Berücksichtigt zur Platzierung die Höhe an der unteren Ecke (cornerS) sowie die Höhenabweichung
     * von der "Standardhöhe"
     * @param image die Graphik für das zu zeichnende Bodenfeld
     */
    public void drawGroundOverMoreTiles(Point2D drawOrigin, String name, Image image, Map<String, Integer> cornerHeights) {

        double heightAboveTile = image.getHeight() - tileImageHeight;

        double xCoordOnCanvas = drawOrigin.getX();
        double yCoordOnCanvas = drawOrigin.getY();

        // heightOffset gibt die Verschiebung in Pixel pro Höheneinheit an.
        // Z.B: Wenn ein Tile an der unteren Ecke (cornerS) eine Höhe von 2 hat, wird die Position
        // um 2*heightOffset verschoben

        // Wenn die zu zeichnende Grafik höher ist, als die eine Grafik mit "normaler" Höhe
        if(heightAboveTile > 0){
            // Tile bei dem die obere Ecke 2 Einheiten höher ist als die untere Ecke. In diesem Fall, ist
            // die Grafik besonders hoch
            if(name.equals("2101")) {
                yCoordOnCanvas += - tileImageHeight - heightOffset * cornerHeights.get("cornerS");
            } else {
                yCoordOnCanvas += - tileImageHeight + heightOffset - heightOffset * cornerHeights.get("cornerS");
            }

        // Wenn die zu zeichnende Grafik niedriger ist, als die eine Grafik mit "normaler" Höhe
        } else {
            // Tile bei dem die untere Ecke 2 Einheiten höher ist als die obere Ecke. In diesem Fall, ist
            // die Grafik besonders flach
            if(name.equals("0121")) {
                yCoordOnCanvas += - heightOffset * cornerHeights.get("cornerS");

            } else{
                yCoordOnCanvas += - heightOffset - heightOffset * cornerHeights.get("cornerS");
            }
        }

        canvas.getGraphicsContext2D().drawImage(image, xCoordOnCanvas, yCoordOnCanvas);
    }


    /**
     * Zeichnet ein building, das größer ist als 1x1, über mehrere tiles.
     * @param tile Das entsprechende tile, von dem ausgehend das building gezeichnet werden soll
     * @param building Das zu zeichnende building
     * @param row Die Reihe in dem zweidimensionalen Array der tiles
     * @param column Die Spalte in dem zweidimensionalen Array der tiles
     */
    public void drawBuildingOverMoreTiles(Tile tile, Building building, int row, int column) {
        if (tile.isBuildingOrigin()) {
            String buildingName = building.getBuildingName();
            String name = objectToImageMapping.getImageNameForObjectName(buildingName);
            double ratio = imageNameToImageRatio.get(name);

//            double imageWidth = tileWidth + (tileWidth * 0.5) * (building.getDepth() + building.getWidth() - 2);
            double imageWidth = (tileImageWidth * 0.5) * (building.getDepth() + building.getWidth());
            double imageHeight = imageWidth * ratio;
            double heightOfFloorTiles = tileImageHeightHalf * (building.getDepth() + building.getWidth());
            double heightAboveFloorTiles =  imageHeight - heightOfFloorTiles;

            Image im = getResourceForImageName(name, imageWidth, imageHeight);

            Point2D drawOrigin = translateTileCoordsToCanvasCoords(row, column);
            double xCoordOnCanvas = drawOrigin.getX();
            double yCoordOnCanvas = drawOrigin.getY() - tileImageHeightHalf * building.getDepth() - heightAboveFloorTiles;
            canvas.getGraphicsContext2D().drawImage(im, xCoordOnCanvas, yCoordOnCanvas);

            //TODO Da gebäude von ihrem Ursprungstile gezeichnet werden, überlappen sie Bäume aus reihen weiter oben,
            // die eigentlich das Gebäude überlappen sollten
        }
    }

    /**
     * Gibt ein ImagePattern für den Namen des Untergrunds zurück, z.B. grass
     * Um Performance-Probleme zu lösen, werden die imagePatterns in einem Cache gespeichert und wenn möglich
     * wiederverwendet. Der Cache ist mit einer Map umgesetzt.
     * @param groundName
     * @return
     */
    private ImagePattern getImagePatternForGroundName(String groundName){
        ImagePattern cachedPattern = imagePatternCache.get(groundName);
        if(cachedPattern != null){
            return cachedPattern;
        }
        else{
            String imageName = objectToImageMapping.getImageNameForObjectName(groundName);
            Image image = getResourceForImageName(imageName, tileImageWidth, tileImageHeight);
            ImagePattern newPattern = new ImagePattern(image);
            imagePatternCache.put(groundName, newPattern);
            return newPattern;
        }
    }

//
//    public Image getGroundImage(int column, int row, Tile[][] fields) {
//        String name;
//        String buildingName;
//
//                Map<String, Integer> cornerHeights = controller.getTileOfMapTileGrid(row, column).getCornerHeights();
//                System.out.println(row + " " + column + " " + cornerHeights);
//                String absoluteTileHeight = controller.getTileOfMapTileGrid(row, column).absoluteHeigtToRelativeHeight(cornerHeights);
//                System.out.println(row + " " + column + " " + absoluteTileHeight);
////
////                Map<String, Integer> heightMap = new HashMap<>();
////                heightMap.put("cornerN", 0);
////                heightMap.put("cornerE", 0);
////                heightMap.put("cornerS", 0);
////                heightMap.put("cornerW", 0);
////
////                Building ground = new Building();
////                controller.getTileOfMapTileGrid(row, column).setCornerHeights(cornerHeights);
//                ground.setBuildingName(absoluteTileHeight);
//                ground.setWidth(1);
//                ground.setDepth(1);
//                controller.getTileOfMapTileGrid(row, column).setBuilding(ground);
//                buildingName = field.getBuilding().getBuildingName();
//
//            name = objectToImageMapping.getImageNameForObjectName(buildingName);
//
//
//        System.out.println(imageNameToImageRatio);
//        double ratio = imageNameToImageRatio.get(name);
//
//        return getResourceForImageName(name, tileImageWidth, tileImageWidth * ratio);
//
//    }

    /**
     * Gibt ein Image für die geforderte Stelle in der Tile-Map zurück in der Breite eines Tiles
     * @param column
     * @param row
     * @param fields
     * @return
     */
    public Image getSingleFieldImage(int column, int row, Tile[][] fields) {
        String name;
        String buildingName;
        if (column < 0 || row < 0 || column >= mapWidth || row >= mapDepth) name = "black";
        else {
            Tile field = fields[row][column];
            if (field.isWater()) {
                buildingName = "water";
            } else
                if (field.getBuilding() == null) {
                throw new RuntimeException("Das muss man sich nochmal anschauen: kann ein Field ohne Building existieren?");
                } else {

                    if (field.getBuilding().getBuildingName().equals("ground")) {

                        Map<String, Integer> cornerHeights = controller.getTileOfMapTileGrid(row, column).getCornerHeights();
                        String absoluteTileHeight = controller.getTileOfMapTileGrid(row, column).absoluteHeigtToRelativeHeight(cornerHeights);

                        buildingName = absoluteTileHeight;
                System.out.println("Buildingname: " + buildingName);


                    } else {
                        buildingName = field.getBuilding().getBuildingName();
                    }
                }
            name = objectToImageMapping.getImageNameForObjectName(buildingName);
        }
        double ratio = 0;
        if(name != null && imageNameToImageRatio.containsKey(name)){
//            System.out.println(name + " " + imageNameToImageRatio.get(name));
            ratio = imageNameToImageRatio.get(name);
            return getResourceForImageName(name, tileImageWidth, tileImageWidth * ratio);
        }
        else {
            System.out.println("invalid name: + " + name);
            throw new RuntimeException("invalid building name");
        }

    }


    /**
     * Malt eine Grasszelle
     * @param column
     * @param row
     * @return
     */
    public Image getGrassImage(int column, int row) {
        String name;
        String buildingName = "grass";;
        if (column < 0 || row < 0 || column >= mapWidth || row >= mapDepth) name = "black";
        else {
            name = objectToImageMapping.getImageNameForObjectName(buildingName);
        }

        double ratio = imageNameToImageRatio.get(name);

        return getResourceForImageName(name, tileImageWidth, tileImageWidth * ratio);
    }

    /**
     * Gibt den Punkt auf dem Canvas an der linken Ecke des Tiles zurück im Bezug auf den aktuellen Ausschnitt der Karte
     * @param row Die Reihe des Tiles betrachtet für die gesamte Karte
     * @param column Die Spalte des Tiles betrachtet für die gesamte Karte
     * @return
     */
    public Point2D translateTileCoordsToCanvasCoords(double row, double column) {

        double pixelXCoordAtTile = (row + column) * tileImageWidthHalf;
        double pixelYCoordAtTile = (row - column) * tileImageHeightHalf;

        // Differenz zwischen Breite und Tiefe der Map
        double differenceWidthHeigth = mapWidth - mapDepth;

        double tileOffset = differenceWidthHeigth * 0.25;

        // Zeichenreihenfolge von oben rechts nach unten links
        double startX = pixelXCoordAtTile + canvasCenterWidth - tileImageWidthHalf * mapWidth + (tileOffset * tileImageWidth);
        double startY = pixelYCoordAtTile + canvasCenterHeight - (tileOffset * tileImageHeight);
        startX -= cameraOffsetX;
        startY -= cameraOffsetY;
//        System.out.println("moveCoordinates: row " + startX + " column " + startY);
        return new Point2D(startX, startY);
    }

    /**
     * Gibt den Punkt auf dem Canvas an der linken Ecke des Tiles zurück im Bezug auf den aktuellen Ausschnitt der Karte
     * @param pointOnTileMap Der Punkt in den Koordinaten der TileMap
     * @return
     */
    public Point2D translateTileCoordsToCanvasCoords(Point2D pointOnTileMap) {
        return translateTileCoordsToCanvasCoords(pointOnTileMap.getX(), pointOnTileMap.getY());
    }

    /**
     * Bei Mausklick, werden die Mauskoordinaten sowie die Koordinaten des angeklickten Tile ausgegeben
     *
     * @param mousePosLabel Label, das die Koordinaten der Mausposition in Pixel anzeigen soll
     * @param isoCoordLabel Label, das die Koordinaten des angeklickten Tile anzeigen soll
     */
    public void showCoordinatesOnClick(Label mousePosLabel, Label isoCoordLabel, Label cornerLabel) {

        canvas.setOnMouseClicked(event -> {
            double mouseX = event.getX();
            double mouseY = event.getY();

            Point2D mouse = new Point2D(mouseX, mouseY);

            String mouseCoords = "Mouse coordinates: x: " + mouseX + " y: " + mouseY;
            mousePosLabel.setText(mouseCoords);

            // Findet isometrische Koordinaten der Mouseposition
            Point2D isoCoord = findTileCoord(mouseX, mouseY);

            Point2D newIsoCoord = findTileCoordNew(mouseX, mouseY);

//            System.out.println(rowColToCanvasCoordinates);
//            String tileCoords = "Tile coordinates: x: " + isoCoord.getX() + " y: " + isoCoord.getY();
            String tileCoords = "Tile coordinates: x: " + newIsoCoord.getX() + " y: " + newIsoCoord.getY();
            isoCoordLabel.setText(tileCoords);


            Map<String, Integer> cornerHeights;
            if(isoCoord.getX() >= 0 && isoCoord.getX() < mapWidth && isoCoord.getY() >= 0 && isoCoord.getY() < mapDepth) {
                Tile tile = controller.getTileOfMapTileGrid((int) isoCoord.getX(), (int) isoCoord.getY());
                cornerHeights = tile.getCornerHeights();
                cornerLabel.setText(cornerHeights.toString());
            } else {
                cornerLabel.setText("undefined corner heights for this coordinates");
            }
        });
    }

    /**
     * Gibt das Bild für den entsprechenden Namen eines Bildes in der gewünschten Höhe und Breite zurück.
     * Dabei wird Caching verwendet.
     * @param imageName
     * @param width
     * @param height
     * @return
     */
    public Image getResourceForImageName(String imageName, double width, double height) {

        // Breite und Höhe wird auf Integer gerundet
        int widthAsInt = (int) Math.round(width);
        int heightAsInt = (int) Math.round(height);

        // Map cachedImage: Das ist eine Zuordnung von Namen zu Image-Objekten
        Image cachedImage = imageCache.get(imageName + widthAsInt + heightAsInt);
        // Es wird mit dem Namen namebreitehöhe nachgeschaut, ob es schon ein Image-Objekt des Bildes in der
        // passenden Breite und Höhe gibt. Das wär dann zum Beispiel "road-sw6432". Wenn das geladene Objekt
        // nicht null ist, ist das gesucht Image Objekt gefunden und wird zurückgegeben
        if (cachedImage != null) {
            return cachedImage;
        }

        String gamemode = controller.getGamemode();
        Image image = new Image(
                "/" + gamemode + "/" + imageName + ".png",
                widthAsInt,
                heightAsInt,
                false,
                true);
        imageCache.put(imageName + widthAsInt + heightAsInt, image);
        return image;
    }

    /**
     * Gibt das Bild zu dem angegebenen Namen in ursprünglicher Größe wie in resources zurück.
     * Dabei wird caching verwendet.
     * @param imageName
     * @return
     */
    public Image getResourceForImageName(String imageName) {
        Image cachedImage = imageCache.get(imageName + "raw");
        if (cachedImage != null) {
            return cachedImage;
        }

        String gamemode = controller.getGamemode();
        Image image = new Image("/" + gamemode + "/" + imageName + ".png");
        imageCache.put(imageName + "raw", image);
        return image;
    }

    /**
     * Ermittelt das richtige Bild für fahrendes Auto
     * @param start
     * @param end
     */
    private String getImageNameForCar(Point2D start, Point2D end) {

        String carName = null;
        if (start.getX() < end.getX()) {
            //nach rechts oben fahren
            //System.out.print("nach rechts");
            if (start.getY() > end.getY()) {
                //System.out.println(" oben");
                carName = "car_ne";
            }
            else {
                //System.out.println(" unten");
                carName = "car_se";
            }
        }
        else  if (start.getX() > end.getX()){
            // nach link oben fahren
            //System.out.print("nach links");
            if (start.getY() < end.getY()) {
                //System.out.println(" unten");
                carName = "car_sw";
            }
            else {
                //System.out.println(" oben");
                carName = "car_nw";
            }
        }
        return objectToImageMapping.getImageNameForObjectName(carName);
    }

    /**
     * Zeigt Auto-Animationen für die Punkte in dem jeweiligen movement an.
     * Soll in Zukunft auch movements von Flugzeugen und Zügen darstellen
     */
    public void translateVehicles(List<VehicleMovement> movements){

        List<VehicleAnimation> animations = new ArrayList<>();
        // Erstellt für jedes VehicleMovement-Objekt ein passendes VehicleAnimation-Objekt
        for(VehicleMovement movement : movements){
            Point2D endPosition = translateTileCoordsToCanvasCoords(movement.getLastPair().getKey().coordsRelativeToMapOrigin());
            Point2D startPosition = translateTileCoordsToCanvasCoords(movement.getStartPosition().coordsRelativeToMapOrigin());
            if((endPosition.getX() < 0 || endPosition.getX() > canvas.getWidth() || endPosition.getY() < 0 || endPosition.getY() > canvas.getHeight())
                    &&
                    (startPosition.getX() < 0 || startPosition.getX() > canvas.getWidth() || startPosition.getY() < 0 || startPosition.getY() > canvas.getHeight())
            ){
                //Dann liegt die Bewegung nicht auf dem sichtbaren Bereich. Tue also nichts
            }
            else {
                animations.add(getAnimationForMovement(movement));
            }
        }

        if(animations.size() == 0){
            controller.waitForOneDay();
            return;
        }

        // Der Punkt auf dem Canvas am Anfang der Animation beim Tile an der Stelle x=0, y=0. Wird später verwendet, um die Animation beim
        // Verschieben der Karte immer noch an der richtigen Stelle anzuzeigen
        Point2D zeroPointAtStart = translateTileCoordsToCanvasCoords(0,0);

        timer = new AnimationTimer() {
            @Override
            //Diese Methode wird während der Animation ständig aufgerufen. Sie soll das vorherig gezeichnete Bild des
            //Fahrzeugs überzeichnen und das Bild mit den neuen Coordinaten zeichnen. Dies tut sie für jedes Fahrzeug
            //das sich auf der Karte bewegt, also für jede VehicleAnimation in der Liste animations
            public void handle(long now) {
                // Der Punkt auf dem Canvas zum aktuellen Zeitpunkt beim Tile an der Stelle x=0, y=0. Dadurch kann die
                //Verschiebung der Karte im Vergleich zum Beginn festgestellt werden.
                Point2D actualZeroPoint = translateTileCoordsToCanvasCoords(0,0);
                double xShift = actualZeroPoint.getX() - zeroPointAtStart.getX();
                double yShift = actualZeroPoint.getY() - zeroPointAtStart.getY();

                // Wenn diese bedingung falsch ist, befindet sich das Fahrzeug nicht mehr auf der Karte. Die Karte wurde
                //dann vom Benutzer während der Animation so bewegt, dass die Animation nicht mehr im sichtbaren Bereich
                //liegt
                if(xShift < canvas.getWidth() && yShift < canvas.getHeight()){

                    // Hier wird drawMap() aufgerufen um die vorherigen Bilder zu entfernen. Sollte eventuell anders
                    //gemacht werden, da das in Zukunft Performance-Probleme verursachen könnte
                    drawMap();
                    GraphicsContext gc = canvas.getGraphicsContext2D();
                    for(VehicleAnimation animation: animations){
                        // Der aktuelle Wert für das passende Bild steht in dem imageName Property der VehicleAnimation
                        String imageName = animation.getImageName().getValue();

                        if(imageName==null) throw new RuntimeException("imageName was null");

                        Image carImage = getResourceForImageName(imageName, tileImageHeightHalf,
                                imageNameToImageRatio.get(imageName)*tileImageHeightHalf);

                        //Zeichnet das Bild auf das Canvas. xShift und yShift sind nur bei einer Verschiebung der Karte
                        //während der Animation nicht 0. Die Verschiebung von -15 in y-Richtung sind willkürlich
                        //und müssen nochmal angeschaut und verbessert werden. Es wurde dadurch versucht die Autos auf
                        //verschiedenen Straßenseiten anzuzeigen, geht aber noch nicht ganz.
                        gc.drawImage(carImage, animation.getxCoordProperty().doubleValue()+xShift,
                                animation.getyCoordProperty().doubleValue()-15+yShift);
                    }
                }
            }
        };
        // Die parallelTransition sorgt für eine gleichzeitige Ausführung der Animationen. Dazu werden ihr alle timlines
        //hinzugefügt
        parallelTransition = new ParallelTransition();
        for(VehicleAnimation animation: animations){
            parallelTransition.getChildren().add(animation.getTimeline());
        }
        getMenuPane().getAnimationButton().setDisable(false);
        parallelTransition.setOnFinished(event -> {
            parallelTransition.stop();
            timer.stop();

            //Rufe simulateOneDay auf, wenn Animation vorbei. Dadurch entsteht ein Kreislauf, da simulateOneDay dann
            //wieder translateVehicles aufruft.
            controller.simulateOneDay();
        });

        timer.start();
        parallelTransition.play();
    }

    /**
     * Erstellt zu dem VehicleMovement das passende VehicleAnimation Objekt und gibt es zurück
     * @param movement
     * @return
     */
    private VehicleAnimation getAnimationForMovement(VehicleMovement movement){
        DoubleProperty x  = new SimpleDoubleProperty();
        DoubleProperty y  = new SimpleDoubleProperty();
        StringProperty imageName  = new SimpleStringProperty();

        Point2D startPoint = translateTileCoordsToCanvasCoords(movement.getStartPosition().coordsRelativeToMapOrigin());
        Point2D secondPoint = translateTileCoordsToCanvasCoords(movement.getPairOfPositionAndDistance(0).getKey().coordsRelativeToMapOrigin());
        String startImageName = getImageNameForCar(startPoint, secondPoint);

        // Ein KeyFrame gibt den Zustand einer Animation zu einer bestimmten Zeit an. Hier wird der Zustand zu Beginn
        // angegeben. Ein KeyFrame hat momentan 3 Variablen: Die x-Coordinate des fahrzeugs, die y-Coordinate des
        // Fahrzeugs und das passende Bild
        KeyFrame start = new KeyFrame(
                Duration.seconds(0.0), new KeyValue(x, startPoint.getX()),
                new KeyValue(y, startPoint.getY()),
                new KeyValue(imageName, startImageName));

        Timeline timeline = new Timeline(start);
        double wholeDistance = movement.getWholeDistance();

        double time = 0.0;
        for(int i=0; i<movement.getNumberOfPoints(); i++){
            Pair<PositionOnTilemap, Double> pair = movement.getPairOfPositionAndDistance(i);
            double distanceToPosition = pair.getValue();
            // time gibt den Zeitpunkt des neuen KeyFrames an. Dabei wird anteilig der aktuell animierten Distanz zu der
            // gesamten Distanz hinzugefügt.
            time += ( distanceToPosition / wholeDistance) * tickDuration;
            Point2D point = translateTileCoordsToCanvasCoords(pair.getKey().coordsRelativeToMapOrigin());
            String actualImageName;
            if(i!=movement.getNumberOfPoints()-1){
                // Bestimme das Bild des Fahrzeugs aus der aktuellen Position und der nächsten Position (deswegen i+1)
                Point2D nextPoint = translateTileCoordsToCanvasCoords(movement.getPairOfPositionAndDistance(i+1).getKey().coordsRelativeToMapOrigin());
                actualImageName = getImageNameForCar(point, nextPoint);
            }
            else {
                // Wenn der aktuelle Punkt der letzte Punkt der Liste ist, kann i+1 nicht verwendet werden. Also wird
                // stattdessen der vorherige Punkt (i-1) benutzt, um das Bild zu bestimmen
                Point2D lastPoint;
                if(i==0) lastPoint = startPoint;
                else lastPoint = translateTileCoordsToCanvasCoords(movement.getPairOfPositionAndDistance(i-1).getKey().coordsRelativeToMapOrigin());
                actualImageName = getImageNameForCar(lastPoint, point);
            }
            KeyFrame frame = new KeyFrame(
                    Duration.seconds(time),
                    new KeyValue(x, point.getX()),
                    new KeyValue(y, point.getY()),
                    new KeyValue(imageName, actualImageName)
                    );
            timeline.getKeyFrames().add(frame);
        }

        VehicleAnimation animation = new VehicleAnimation(x,y, timeline, imageName);
        return animation;
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

    public double getTileImageWidth() {
        return tileImageWidth;
    }

    public double getTileImageHeight() {
        return tileImageHeight;
    }

    public double getPreviousMouseX() {
        return previousMouseX;
    }

    public double getPreviousMouseY() {
        return previousMouseY;
    }

    public Stage getStage() {
        return stage;
    }

    public Map<String, Double> getImageNameToImageRatio() {
        return imageNameToImageRatio;
    }

    public Controller getController() {
        return controller;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public MenuPane getMenuPane() {
        return menuPane;
    }


//    public Map<List<Point2D>, Point2D> getRowColToCanvasCoordinates() {
//        return rowColToCanvasCoordinates;
//    }

    public ParallelTransition getParallelTransition() {
        return parallelTransition;
    }

    public double getTickDuration() {
        return tickDuration;
    }

    public void setTickDuration(double tickDuration) {
        this.tickDuration = tickDuration;
    }

    public AnimationTimer getTimer() {
        return timer;
    }

}

