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
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
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

    private Canvas canvas = new Canvas(1200, 600);
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

        // Es wird den sichtbaren Ausschnitt aus dem Array iteriert
        for (int col = maximumY; col >= minimumY; col--) {
            for (int row = minimumX; row <= maximumX; row++) {

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
                                drawTileImage(i, row, image, false);
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
                                drawTileImage(col, i, image, false);
                            }

                        }

                    } else {
                        // diese Zelle wurde vorher als Zelle neben einem Gebäude identifiziert
                        // zeichnet neben Gebäude, um Problem der Überlappung zu lösen
                        if (row == startRow && col >= startCol && col <= endCol) {
                            // und muss daher als Grass gezeichnet werden
                            Image image = getGrassImage(col, row);
                            drawTileImage(col, row, image, false);
                        }
                        else {

//                            Image image = getSingleFieldImage(col, row, fields);
//                            drawTileImage(col, row, image, false);
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

                            drawPolygon(null, col, row, cornerHeightNorth,cornerHeightEast,cornerHeightSouth,cornerHeightWest);
//                            drawPolygon(null, col, row, 1,0,0,0);
//                            drawPolygon(null, col, row, 0,1,0,0);
//                            drawPolygon(null, col, row, 0,0,1,0);
                        }
                    }
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

    List<Point2D>  firstTile = new ArrayList<>();
    Map <List<Point2D>, Point2D>  rowColToCanvasCoordinates = new LinkedHashMap<>();

    public void drawPolygon(Image image, int col, int row, int heightNorth, int heightEast, int heightSouth, int heightWest) {

        // X und Y Koordinaten der linken Ecke des Tiles
        Point2D drawOrigin = moveCoordinates(row, col);
        double xCoordOnCanvas = drawOrigin.getX();
//        double yCoordOnCanvas = drawOrigin.getY() - tileImageHeightHalf;
        double yCoordOnCanvas = drawOrigin.getY();

        GraphicsContext gc = canvas.getGraphicsContext2D();
        int numberOfPoints = 4;
        double heightShift = tileImageHeight / 10;
        double xCoordWest = xCoordOnCanvas;
        double yCoordWest = yCoordOnCanvas - heightWest * heightShift;

        double xCoordNorth = xCoordOnCanvas + tileImageWidthHalf;
        double yCoordNorth = yCoordOnCanvas - tileImageHeightHalf - heightNorth * heightShift;

        double xCoordEast = xCoordOnCanvas + tileImageWidth;
        double yCoordEast = yCoordOnCanvas - heightEast * heightShift;

        double xCoordSouth = xCoordOnCanvas + tileImageWidthHalf;
        double yCoordSouth = yCoordOnCanvas + tileImageHeightHalf - heightSouth * heightShift;

//        double[] xCoords = {xCoordWest, xCoordSouth, xCoordEast, xCoordNorth};
//        double[] yCoords = {yCoordWest, yCoordSouth, yCoordEast, yCoordNorth};

        double[] xCoords = {xCoordWest, xCoordNorth, xCoordEast, xCoordSouth};
        double[] yCoords = {yCoordWest, yCoordNorth, yCoordEast, yCoordSouth};

        Point2D west = new Point2D(xCoordWest, yCoordWest);
        Point2D north = new Point2D(xCoordNorth, yCoordNorth);
        Point2D east = new Point2D(xCoordEast, yCoordEast);
        Point2D south = new Point2D(xCoordSouth, yCoordSouth);

        List<Point2D> coordsOnCanvas = new ArrayList<>();
        coordsOnCanvas.add(west);
        coordsOnCanvas.add(north);
        coordsOnCanvas.add(east);
        coordsOnCanvas.add(south);

        List<Double> xCoordsList = Arrays.asList(xCoordWest, xCoordNorth, xCoordEast, xCoordSouth);
        List<Double> yCoordsList = Arrays.asList(yCoordWest, yCoordNorth, yCoordEast, yCoordSouth);

        ImagePattern imagePattern;
        if (heightWest < 0) {
            imagePattern = getImagePatternForGroundName("water");
        } else {
            imagePattern = getImagePatternForGroundName("grass");
        }
        gc.setFill(imagePattern);
        gc.fillPolygon(xCoords, yCoords, numberOfPoints);
        gc.strokePolygon(xCoords, yCoords, numberOfPoints);



//        gc.strokeText("N: " + heightNorth + " E " + heightEast + " S " + heightSouth + " W " + heightWest, xCoordOnCanvas, yCoordOnCanvas);

        gc.setFill(Color.BLACK);
//        gc.setStroke(Color.BLACK);

        firstTile = coordsOnCanvas;

//        for(Map.Entry<List<Point2D>, Point2D> entry : rowColToCanvasCoordinates.entrySet()){
//        if(!(entry.getValue().getX() == row && entry.getValue().getY() == col)) {
//            System.out.println("TEST if condition");
////            System.out.println(entry.getValue().getX() + " " + row);
////            System.out.println(entry.getValue().getY() + " " + col);
////        System.out.println(coordsOnCanvas);
//        rowColToCanvasCoordinates.put(coordsOnCanvas, new Point2D(row, col));
//        }
//    }

        if(!rowColToCanvasCoordinates.keySet().contains(coordsOnCanvas)){
            rowColToCanvasCoordinates.put(coordsOnCanvas, new Point2D(row, col));
        }
        //TODO Das Tile 0,0 ganz links wird manchmal je nach Position komisch angezeigt

    }

    public boolean isPointInsidePolygon(Point2D point, List<Point2D> coordsOnCanvas) {

        double x = point.getX();
        double y = point.getY();

        boolean inside = false;
        for (int i = 0, j = coordsOnCanvas.size() - 1; i < coordsOnCanvas.size(); j = i++) {

            double xi = coordsOnCanvas.get(i).getX();
            double yi = coordsOnCanvas.get(i).getY();

            double xj = coordsOnCanvas.get(j).getX();
            double yj = coordsOnCanvas.get(j).getY();

            boolean intersect = ((yi > y) != (yj > y))
                    && (x < (xj - xi) * (y - yi) / (yj - yi) + xi);
            if (intersect) inside = !inside;
        }

        if(inside){
            System.out.println(rowColToCanvasCoordinates.get(coordsOnCanvas));
        }

        return inside;

    };

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
     * @param column
     * @param row
     * @param image
     * @param transparent
     */
    public void drawTileImage(int column, int row, Image image, boolean transparent) {

        // TileX und TileY berechnet Abstand der Position von einem Bild zum nächsten in Pixel
        // Zeichenreihenfolge von oben rechts nach unten links

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
            Image image = getResourceForImageName(imageName);
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

            Point2D newIsoCoord = new Point2D(0,0);

            for(Map.Entry<List<Point2D>, Point2D> entry : rowColToCanvasCoordinates.entrySet()){
                if(isPointInsidePolygon(mouse, entry.getKey())){
                    System.out.println("Clicked on coordinates : " + entry.getValue());
                    newIsoCoord = entry.getValue();
                }
            }
//            System.out.println(rowColToCanvasCoordinates);
//            String tileCoords = "Tile coordinates: x: " + isoCoord.getX() + " y: " + isoCoord.getY();
            String tileCoords = "Tile coordinates: x: " + newIsoCoord.getX() + " y: " + newIsoCoord.getY();
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
     * Experimentelle Methode, die ein Auto vom Punkt start zum Punkt end fahren lässt
     * @param start
     * @param end
     */
    public void translateCar(Point2D start, Point2D end){
        DoubleProperty x  = new SimpleDoubleProperty();
        DoubleProperty y  = new SimpleDoubleProperty();

        String name = objectToImageMapping.getImageNameForObjectName("car-sw");

        Point2D zeroPointAtStart = moveCoordinates(0,0);


        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0),
                        new KeyValue(x, start.getX()),
                        new KeyValue(y, start.getY())
                ),
                new KeyFrame(Duration.seconds(tickDuration),
                        new KeyValue(x, end.getX()),
                        new KeyValue(y, end.getY())
                )
        );

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                Image carImage = getResourceForImageName(name, tileImageHeightHalf,
                        imageNameToImageRatio.get(name)*tileImageHeightHalf);

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
        };
        ParallelTransition parallelTransition = new ParallelTransition(timeline);

        parallelTransition.setOnFinished(event -> {
            parallelTransition.stop();
            timer.stop();

            // Die folgenden Zeilen dienen der experimentellen Darstellung der Animation, sind also nicht endgültig
            Vertex v1 = controller.path.get(++controller.indexOfStart);
            Vertex v2 = controller.path.get(++controller.indexOfNext);
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
}

