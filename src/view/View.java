package view;

import controller.Controller;
import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.BasicModel;
import model.Building;
import model.Tile;
import model.Vertex;


import java.util.*;
import javax.swing.border.Border;



public class View {
    private Stage stage;

    private double tileImageWidth = 128;
    private double tileImageWidthHalf = tileImageWidth / 2;
    private double tileImageHeight = 64;
    private double tileImageHeightHalf = tileImageHeight / 2;
    private int mapWidth;
    private int mapDepth;

    private Controller controller;

    private Map<String, Double> imageNameToImageRatio = new HashMap<>();

    private Canvas canvas = new Canvas(900, 450);
    private double canvasCenterWidth = canvas.getWidth() / 2;
    private double canvasCenterHeight = canvas.getHeight() / 2;


    private double cameraOffsetX = 0.0;
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

    private String carName = null;

//    private Map <List<Point2D>, Point2D>  rowColToCanvasCoordinates = new LinkedHashMap<>();


    public View(Stage primaryStage, BasicModel model) {
        this.stage = primaryStage;
        objectToImageMapping = new ObjectToImageMapping(model.getGamemode());
        fields = model.getFieldGridOfMap();

        Label isoCoordLabel = new Label();
        isoCoordLabel.setFont(new Font("Arial", 15));

        Label mousePosLabel = new Label();
        mousePosLabel.setFont(new Font("Arial", 15));

        borderPane = new BorderPane();
        VBox vBox = new VBox();
        borderPane.setBottom(vBox);
        vBox.getChildren().addAll(mousePosLabel, isoCoordLabel);
        borderPane.setCenter(canvas);

        canvas.setFocusTraversable(true);
        showCoordinatesOnClick(mousePosLabel, isoCoordLabel);
        scrollOnKeyPressed();
        scrollOnMouseDragged();

        zoom();
//        zoom2();

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

            drawMap();

        });
    }

    public void zoom2 (){
        canvas.setOnScroll(event -> {
            double delta = 1.2;
            double scale = canvas.getScaleY(); // currently we only use Y, same value is used for X
            double oldScale = scale;

            if (event.getDeltaY() < 0) scale /= delta;
            else scale *= delta;

            scale = clamp(scale, MIN_SCALE, MAX_SCALE);
            double f = (scale / oldScale) - 1;
            double dx = (event.getSceneX() - (canvas.getBoundsInParent().getWidth() / 2 + canvas.getBoundsInParent().getMinX()));
            double dy = (event.getSceneY() - (canvas.getBoundsInParent().getHeight() / 2 + canvas.getBoundsInParent().getMinY()));

            canvas.setScaleY(scale);
            // note: pivot value must be untransformed, i. e. without scaling
            setPivot(f * dx, f * dy);
            event.consume();
        });
    }


    public void setPivot( double x, double y) {
        canvas.setTranslateX(canvas.getTranslateX()-x);
        canvas.setTranslateY(canvas.getTranslateY()-y);
    }

    public static double clamp( double value, double min, double max) {

        if( Double.compare(value, min) < 0)
            return min;

        if( Double.compare(value, max) > 0)
            return max;

        return value;
    }

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
     * Zeichnet Map auf Canvas anhand der Daten eines Arrays von Fields
     */
    public void drawMap() {
        System.out.println("draw Map Called");
        numberOfDrawPol = 0;
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
                Point2D drawOrigin = moveCoordinates(row, col);
                if(drawOrigin.getX() > -tileImageWidth && drawOrigin.getX() < canvas.getWidth()
                && drawOrigin.getY() > -tileImageHeightHalf && drawOrigin.getY() < canvas.getHeight() + tileImageHeightHalf){


                //TODO fields von Controller holen, da mvc-model

                // Row und Column müssen innerhalb des 2d-Arrays liegen
                if (row >= 0 && col >= 0 && row < fields.length && col < fields[0].length) {
                    Tile field = fields[row][col];
                    Building building = field.getBuilding();

                    // Wenn das building größer als ein Tile ist, zeichne über mehrere Tiles
                    if (building != null && (building.getWidth() > 1 || building.getDepth() > 1)) {
                        if (field.isBuildingOrigin()) {

                            for(int i = col; i <= col + building.getDepth()-1; i++) {
                                // Obere Kante vom Gebäude mit Grassfläche übermalen
                                Image image = getGrassImage(i, row);
                                drawTileImage(drawOrigin, image, false);
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
                                drawTileImage(drawOrigin, image, false);
                            }

                        }

                    } else {
                        // diese Zelle wurde vorher als Zelle neben einem Gebäude identifiziert
                        // zeichnet neben Gebäude, um Problem der Überlappung zu lösen
                        if (row == startRow && col >= startCol && col <= endCol) {
                            // und muss daher als Grass gezeichnet werden
                            Image image = getGrassImage(col, row);
                            drawTileImage(drawOrigin, image, false);
                        }
                        else {

                            Image image = getSingleFieldImage(col, row, fields);
                            drawTileImage(drawOrigin, image, false);
                            //TODO Polygone mit Wasser oder Gras Tiles zu zeichnen, je nach Höhe
                            Tile tile = fields[row][col];
                            int cornerHeightSouth = tile.getCornerHeights().get("cornerS");
                            int cornerHeightWest = tile.getCornerHeights().get("cornerW");
                            int cornerHeightNorth = tile.getCornerHeights().get("cornerN");
                            int cornerHeightEast = tile.getCornerHeights().get("cornerE");

//                            int cornerHeightSouth = 0;
//                            int cornerHeightWest = 0;
//                            int cornerHeightNorth = 0;
//                            int cornerHeightEast = 0;


                            /*if(row-1 >= 0){
                                cornerHeightSouth = fields[row+1][col].getCornerHeights().get("cornerW");
                                cornerHeightEast = fields[row+1][col+1].getCornerHeights().get("cornerW");
                                cornerHeightNorth = fields[row][col+1].getCornerHeights().get("cornerW");
                            }*/
//                            drawPolygon(drawOrigin, cornerHeightNorth,cornerHeightEast,cornerHeightSouth,cornerHeightWest);
                        }
                    }
                }
                    System.out.println("calls of polygon "+numberOfDrawPol);
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


    int numberOfDrawPol = 0;
    public void drawPolygon(Point2D drawOrigin, int heightNorth, int heightEast, int heightSouth, int heightWest) {

        numberOfDrawPol++;
        // X und Y Koordinaten der linken Ecke des Tiles

        double xCoordOnCanvas = drawOrigin.getX();
//        double yCoordOnCanvas = drawOrigin.getY() - tileImageHeightHalf;
        double yCoordOnCanvas = drawOrigin.getY();
//
        GraphicsContext gc = canvas.getGraphicsContext2D();
        int numberOfPoints = 4;
        double heightShift = tileImageHeight / 5;
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
//
//        double[] xCoords = {100, 200, 100, 200};
//        double[] yCoords = {100, 200, 200, 100};

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


//                if(!rowColToCanvasCoordinates.keySet().contains(coordsOnCanvas)){
//                    rowColToCanvasCoordinates.put(coordsOnCanvas, new Point2D(row, col));
//                    System.out.println(rowColToCanvasCoordinates.size());
//                }
//            }
//        }
//        if(shouldBeDrawed){
                            ImagePattern imagePattern;
                if (heightWest < 0) {
                    imagePattern = getImagePatternForGroundName("water");
                } else {
                    imagePattern = getImagePatternForGroundName("grass");
                }
                gc.setFill(imagePattern);
//            gc.setFill(Color.BLUEVIOLET);
            gc.fillPolygon(xCoords, yCoords, numberOfPoints);
                gc.strokePolygon(xCoords, yCoords, 4);
//            gc.setStroke(Color.WHITE);

//              gc.strokeText("N: " + heightNorth + " E " + heightEast + " S " + heightSouth + " W " + heightWest, xCoordOnCanvas, yCoordOnCanvas);

            gc.setFill(Color.BLACK);
//        }



        //TODO Das Tile 0,0 ganz links wird manchmal je nach Position komisch angezeigt

    }

//    public boolean isPointInsidePolygon(double mouseX, double mouseY, List<Point2D> coordsOnCanvas) {
//
//        double x = mouseX;
//        double y = mouseY;
//
//        boolean inside = false;
//        for (int i = 0, j = coordsOnCanvas.size() - 1; i < coordsOnCanvas.size(); j = i++) {
//
//            double xi = coordsOnCanvas.get(i).getX();
//            double yi = coordsOnCanvas.get(i).getY();
//
//            double xj = coordsOnCanvas.get(j).getX();
//            double yj = coordsOnCanvas.get(j).getY();
//
//            boolean intersect = ((yi > y) != (yj > y)) && (x < (xj - xi) * (y - yi) / (yj - yi) + xi);
//            if (intersect) inside = !inside;
//        }
//
//        if(inside){
//            System.out.println(rowColToCanvasCoordinates.get(coordsOnCanvas));
//        }
//
//        return inside;
//
//    }


//    public Point2D findTileCoordNew(double mouseX, double mouseY) {
//        Point2D newIsoCoord = new Point2D(0,0);
//        for(Map.Entry<List<Point2D>, Point2D> entry : rowColToCanvasCoordinates.entrySet()){
//            if(isPointInsidePolygon(mouseX, mouseY, entry.getKey())){
//                System.out.println("Clicked on coordinates : " + entry.getValue());
//                newIsoCoord = entry.getValue();
//            }
//        }
//        return newIsoCoord;
//    }

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

//        double changedHeight = tileImageHeight + tileImageHeight/10;

        double x = Math.floor((mouseX / tileImageWidth + mouseY / tileImageHeight) - canvasCenterHeight / tileImageHeight
                - (canvasCenterWidth / tileImageWidth) + (mapWidth / 2) + offsetX + cameraOffsetX / tileImageWidth +
                cameraOffsetY / tileImageHeight);

//        double heightShift = tileImageHeight/10;
//        double x = Math.floor((mouseX / tileImageWidth + mouseY / changedHeight) - canvasCenterHeight / changedHeight
//                - (canvasCenterWidth / tileImageWidth) + (mapWidth / 2) + offsetX + cameraOffsetX / tileImageWidth +
//                cameraOffsetY / changedHeight);


        double y = Math.floor((mouseX / tileImageWidth - mouseY / tileImageHeight) + canvasCenterHeight / tileImageHeight
                - (canvasCenterWidth / tileImageWidth) + (mapDepth / 2) + offsetY - cameraOffsetY / tileImageHeight +
                cameraOffsetX / tileImageWidth);

//        double y = Math.floor((mouseX / tileImageWidth - mouseY / changedHeight) + canvasCenterHeight / changedHeight
//                - (canvasCenterWidth / tileImageWidth) + (mapDepth / 2) + offsetY - cameraOffsetY / changedHeight +
//                cameraOffsetX / tileImageWidth);
        return new Point2D(x, y);
    }

    /**
     * Zeichnet das Bild in ein Feld an der angegebenen Stelle
//     * @param column
//     * @param row
     * @param image
     * @param transparent
     */
    public void drawTileImage(Point2D drawOrigin, Image image, boolean transparent) {

        // TileX und TileY berechnet Abstand der Position von einem Bild zum nächsten in Pixel
        // Zeichenreihenfolge von oben rechts nach unten links

        double heightAboveTile = image.getHeight() - tileImageHeight;

//        Point2D drawOrigin = moveCoordinates(row, column);
        double xCoordOnCanvas = drawOrigin.getX();
        double yCoordOnCanvas = drawOrigin.getY() - heightAboveTile - tileImageHeightHalf;

        if (transparent) canvas.getGraphicsContext2D().setGlobalAlpha(0.7);
        canvas.getGraphicsContext2D().drawImage(image, xCoordOnCanvas, yCoordOnCanvas);
        canvas.getGraphicsContext2D().setGlobalAlpha(1);
    }

    public void drawTileImage(int row, int column, Image image, boolean transparent) {

        double heightAboveTile = image.getHeight() - tileImageHeight;

        Point2D drawOrigin = moveCoordinates(row, column);
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

            Point2D drawOrigin = moveCoordinates(row, column);
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
            } else if (field.getBuilding() == null) {
                throw new RuntimeException("Das muss man sich nochmal anschauen: kann ein Field ohne Building existieren?");
            } else {
                buildingName = field.getBuilding().getBuildingName();
            }
            name = objectToImageMapping.getImageNameForObjectName(buildingName);
        }

        double ratio = imageNameToImageRatio.get(name);

        return getResourceForImageName(name, tileImageWidth, tileImageWidth * ratio);

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
    public Point2D moveCoordinates(int row, int column) {

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
//        System.out.println("moveCoordinates: " + startX + " " + startY);
        return new Point2D(startX, startY);
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

            Point2D mouse = new Point2D(mouseX, mouseY);

            String mouseCoords = "Mouse coordinates: x: " + mouseX + " y: " + mouseY;
            mousePosLabel.setText(mouseCoords);

            // Findet isometrische Koordinaten der Mouseposition
            Point2D isoCoord = findTileCoord(mouseX, mouseY);

//            Point2D newIsoCoord = findTileCoordNew(mouseX, mouseY);

//            System.out.println(rowColToCanvasCoordinates);
            String tileCoords = "Tile coordinates: x: " + isoCoord.getX() + " y: " + isoCoord.getY();
//            String tileCoords = "Tile coordinates: x: " + newIsoCoord.getX() + " y: " + newIsoCoord.getY();
            isoCoordLabel.setText(tileCoords);
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
    private void setImageForCar(Point2D start, Point2D end) {

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
    }

    /**
     * Experimentelle Methode, die ein Auto vom Punkt start zum Punkt end fahren lässt
     * @param start
     * @param end
     */
    public void translateCar(Point2D start, Point2D end){

        setImageForCar(start, end);

        DoubleProperty x  = new SimpleDoubleProperty();
        DoubleProperty y  = new SimpleDoubleProperty();

        carName = mapping.getImageNameForBuildingName(carName);


        Point2D zeroPointAtStart = moveCoordinates(0,0);


        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0),
                        new KeyValue(x, start.getX()),
                        new KeyValue(y, start.getY())
                ),
                new KeyFrame(Duration.seconds(0.5),
                        new KeyValue(x, end.getX()),
                        new KeyValue(y, end.getY())
                )
        );

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (carName != null) {
                    Image carImage = getResourceForImageName(carName, tileImageHeightHalf,
                            imageNameToImageRatio.get(carName)*tileImageHeightHalf);

                    Point2D actualZeroPoint = moveCoordinates(0,0);
                    double xShift = actualZeroPoint.getX() - zeroPointAtStart.getX();
                    double yShift = actualZeroPoint.getY() - zeroPointAtStart.getY();

                    if(xShift < canvas.getWidth() && yShift < canvas.getHeight()){
                        GraphicsContext gc = canvas.getGraphicsContext2D();
                        drawMap();
                        gc.drawImage(carImage, x.doubleValue()+xShift,
                                y.doubleValue()-15+yShift);
                    }
                }
            }
        };
        parallelTransition = new ParallelTransition(timeline);
        getMenuPane().getAnimationButton().setDisable(false);
        parallelTransition.setOnFinished(event -> {
            parallelTransition.stop();
            timer.stop();

            // Die folgenden Zeilen dienen der experimentellen Darstellung der Animation, sind also nicht endgültig
            Vertex v1;
            Vertex v2;
            if (controller.indexOfNext < controller.path.size()-1) {
                v1 = controller.path.get(++controller.indexOfStart);
                v2 = controller.path.get(++controller.indexOfNext);
            } else {
                // Wenn letzter point aus path erreicht ist, dann kehre Reihenfolge in path um und fahre zurück
                Collections.reverse(controller.path);
                controller.indexOfStart = 0;
                controller.indexOfNext = controller.indexOfStart + 1;

                v1 = controller.path.get(++controller.indexOfStart);
                v2 = controller.path.get(++controller.indexOfNext);
            }
            controller.moveCarFromPointToPoint(v1,v2);
        });

        timer.start();
        parallelTransition.play();
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

