package view;

import controller.Controller;
import javafx.animation.*;
import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import model.*;

import java.util.*;


public class View {

    private Stage stage;
    private Scene scene;
    private Controller controller;
    private MenuPane menuPane;

    private double tileImageWidth = 128;
    private double tileImageWidthHalf = tileImageWidth / 2;
    private double tileImageHeight = 64;
    private double tileImageHeightHalf = tileImageHeight / 2;
    private int mapWidth;
    private int mapDepth;

    // Gibt für den Namen eines Bildes das ursprüngliche Verhältnis von Höhe und Breite des Bildes an
    private Map<String, Double> imageNameToImageRatio = new HashMap<>();

    private final Canvas canvas = new Canvas(1200, 550);
    private final double canvasCenterWidth = canvas.getWidth() / 2;
    private final double canvasCenterHeight = canvas.getHeight() / 2;

    private BorderPane borderPane;

    // Höhenunterschied bei Grafiken nicht flacher Tiles
    private double heightOffset = 16;

    // Gibt die Verschiebung des sichtbaren Bereichs der Karte in x-Richtung an
    private double cameraOffsetX = 0.0;
    // Gibt die Verschiebung des sichtbaren Bereichs der Karte in y-Richtung an
    private double cameraOffsetY = 0.0;

    private double previousMouseX = -1.0;
    private double previousMouseY = -1.0;

    private Map<String, Image> imageCache = new HashMap<>();
    private ObjectToImageMapping objectToImageMapping;


    private Tile[][] fields;
    private double tickDuration = 1;
    private ParallelTransition parallelTransition;
    private AnimationTimer timer;

    // Map die Canvas-Koordinaten der Ecken eines Polygons auf isometrische Koordinaten abbildet
    private Map<List<Point2D>, Point2D> polygonEdgesToIsoCoords = new LinkedHashMap<>();

    public View(Stage primaryStage, BasicModel model) {
        this.stage = primaryStage;
        objectToImageMapping = new ObjectToImageMapping(model.getGamemode());

        Label isoCoordLabel = new Label();
        isoCoordLabel.setFont(new Font("Arial", 15));

        Label mousePosLabel = new Label();
        mousePosLabel.setFont(new Font("Arial", 15));

        borderPane = new BorderPane();
        VBox vBox = new VBox();
        borderPane.setBottom(vBox);
        vBox.getChildren().addAll(mousePosLabel, isoCoordLabel);
        borderPane.setCenter(canvas);
        borderPane.setPrefSize(canvas.getWidth(), canvas.getHeight() + 110 + 90);
        canvas.setFocusTraversable(true);

        showCoordinatesOnClick(mousePosLabel, isoCoordLabel);
        scrollOnKeyPressed();
        scrollOnMouseDragged();
        zoom();


        Screen screen = Screen.getPrimary();
        Rectangle2D screenBounds = screen.getVisualBounds();
        stage.setX((screenBounds.getWidth() - 1200) / 2);
        stage.setY((screenBounds.getHeight() - 800) / 2);
        scene = new Scene(borderPane);
    }

    /**
     * Erzeugt Menu-Leiste und setzt sie in den oberen Bereich der Border-Pane
     *
     * @param controller
     */
    public void generateMenuPane(Controller controller) {
        menuPane = new MenuPane(controller, this, canvas, objectToImageMapping);
        borderPane.setTop(menuPane);
    }

    /**
     * Ermöglicht das Zoomen auf die Mausposition durch scrollen mit dem Mausrad
     */
    public void zoom() {
        canvas.setOnScroll(scrollEvent -> {
            double scrollDelta = scrollEvent.getDeltaY();
            double zoomFactor = Math.exp(scrollDelta * 0.01);

            double plannedTileImageWidth = tileImageWidth * zoomFactor;
            if ((plannedTileImageWidth > 10 && scrollDelta < 0) || (scrollDelta >= 0 && plannedTileImageWidth < canvasCenterWidth * 2)) {

                Point2D currentIsoCoord = findTileCoord(scrollEvent.getX(), scrollEvent.getY());

                if (currentIsoCoord != null) {
                    double distanceX = (currentIsoCoord.getX() - mapWidth / 2) * (zoomFactor - 1);
                    double distanceY = (currentIsoCoord.getY() - mapDepth / 2) * (zoomFactor - 1);
                    double shiftX = distanceX * tileImageWidthHalf;
                    double shiftY = distanceX * tileImageHeightHalf;
                    shiftX += distanceY * tileImageWidthHalf;
                    shiftY -= distanceY * tileImageHeightHalf;


                    tileImageWidth = plannedTileImageWidth;
                    tileImageHeight = tileImageHeight * zoomFactor;

                    tileImageWidthHalf = tileImageWidthHalf * zoomFactor;
                    tileImageHeightHalf = tileImageHeightHalf * zoomFactor;

                    heightOffset = heightOffset * zoomFactor;

                    // verschiebt indirekt den Mittelpunkt und damit das ganze Spielfeld
                    cameraOffsetX += shiftX;
                    cameraOffsetY += shiftY;

                }
                drawMap();
            }

        });
    }

    /**
     * Verschiebt den zweidimensionalen Punkt point um die angegebenen Tiles in X- bzw. Y-Richtung
     *
     * @param point
     * @param changedTilesWitdh
     * @param changedTilesDepth
     * @return Einen dementsprechend verschobenen Punkt
     */
    public Point2D changePointByTiles(Point2D point, double changedTilesWitdh, double changedTilesDepth) {
        double changeX = tileImageWidthHalf * changedTilesWitdh + changedTilesDepth * tileImageWidthHalf;
        double changeY = tileImageHeightHalf * changedTilesWitdh - changedTilesDepth * tileImageHeightHalf;
        return point.add(changeX, changeY);
    }

    /**
     * Ermöglicht Verschieben der Karte mit den Pfeiltasten
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


    /**
     * Zeichnet Map auf Canvas anhand der Daten eines Arrays von Tiles
     */
    public void drawMap() {
        fields = controller.getFields();
        // Hintergrund wird schwarz gesetzt
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Grenzt Zeichenbereich ein
        int minimumX = (int) findTileCoordForDrawMap(0, 0).getX();
        int maximumX = (int) findTileCoordForDrawMap(canvas.getWidth(), canvas.getHeight()).getX();
        int minimumY = (int) findTileCoordForDrawMap(0, canvas.getHeight()).getY();
        int maximumY = (int) findTileCoordForDrawMap(canvas.getWidth(), 0).getY();

        polygonEdgesToIsoCoords.clear();

        // Es wird den sichtbaren Ausschnitt aus dem Array iteriert
        for (int col = maximumY; col >= minimumY; col--) {
            for (int row = minimumX; row <= maximumX; row++) {

                // Linke Ecke des Tiles
                Point2D drawOrigin = translateTileCoordsToCanvasCoords(row, col);
                if (drawOrigin.getX() > -tileImageWidth * 4 && drawOrigin.getX() < canvas.getWidth()
                        && drawOrigin.getY() > -tileImageHeightHalf * 4
                        && drawOrigin.getY() < canvas.getHeight() + tileImageHeightHalf * 4) {


                    // Row und Column müssen innerhalb des 2d-Arrays liegen
                    if (row >= 0 && col >= 0 && row < fields.length && col < fields[0].length) {
                        Tile field = fields[row][col];
                        Building building = field.getBuilding();
                        Map<String, Integer> cornerHeights = field.getCornerHeights();

                        // Wenn das Building mehr als ein Tile belegt
                        if (building != null && (building.getWidth() > 1 || building.getDepth() > 1)) {
                            if (field.isBuildingOrigin()) {
                                drawBuildingOverMoreTiles(field, building, row, col);
                            }
                        }
                        // Wenn das Building nur ein Tile belegt
                        else {
                            if (building != null) {
                                // Wenn es sich um ein Grasfeld handelt
                                if (building.getBuildingName().equals("ground")
                                        || building.getBuildingName().equals("grass")) {

                                    // Ein String der aus einer 4-Stelligen Zahl aus Ziffern zwischen 0 und 2 besteht
                                    String buildingName = field.absoluteHeigtToRelativeHeight(cornerHeights);

                                    // Gibt image für die angegebene Zeile und Spalte
                                    Image image = getSingleFieldImage(col, row, fields);
                                    String imageName = objectToImageMapping.getImageNameForObjectName(buildingName);
                                    Image r = getResourceForImageName(imageName);

                                    // Verhältnis Zwischen Höhe und Breite eines Bildes
                                    double ratio = r.getHeight() / r.getWidth();

                                    // Wenn das Verhältnis zwischen Höhe und Breite nicht 1:2 ist
                                    if (ratio != 0.5) {
                                        // Dann weicht Größe der Grafik von Standardformat ab
                                        drawGroundOverMoreTiles(drawOrigin, buildingName, image, cornerHeights);

                                    } else {
                                        drawGroundInOneTile(drawOrigin, image, cornerHeights);
                                    }

                                    // Wenn es sich nicht um ein Grasfeld handelt
                                } else {
                                    Image image = getSingleFieldImage(col, row, fields);
                                    drawTileImage(drawOrigin, image, false, cornerHeights);
                                }

                                // in diesem Fall handelt es sich um ein Wasserfeld
                            } else {
                                Image image = getSingleFieldImage(col, row, fields);
                                drawTileImage(drawOrigin, image, false, cornerHeights);
                            }
                        }
                        // Berechnet Koordinaten eines Polygons auf dem Canvas an angegebener Zeile und Spalte
                        calculatePolygonCoordsOnCanvas(row, col, drawOrigin);
                    }
                }
            }
        }

        // Zeichnet die Knoten des Graphen als gelbe Punkte ein
//        if (controller != null) {
//            controller.drawVertexesOfGraph();
//        }

        // Zeichnet eine Vorschau, falls nötig
        Building selectedBuilding = menuPane.getSelectedBuilding();
        MouseEvent hoveredEvent = menuPane.getHoveredEvent();
        if (selectedBuilding != null && hoveredEvent != null) {
            menuPane.drawHoveredImage(hoveredEvent, true);
        }
    }


    /**
     * Berechnet die Koordinaten auf dem Canvas eines Polygons an einer bestimmten Zeile und Spalte.
     * Wird zur Berechnung der Tile-Koordinaten benötigt
     *
     * @param row
     * @param col
     * @param drawOrigin linke Ecke des Tiles/Polygons
     */
    public void calculatePolygonCoordsOnCanvas(int row, int col, Point2D drawOrigin) {

        Tile tile = fields[row][col];
        // Höhen der vier Ecken eines Tiles
        int heightSouth = tile.getCornerHeights().get("cornerS");
        int heightWest = tile.getCornerHeights().get("cornerW");
        int heightNorth = tile.getCornerHeights().get("cornerN");
        int heightEast = tile.getCornerHeights().get("cornerE");

        double xCoordOnCanvas = drawOrigin.getX();
        double yCoordOnCanvas = drawOrigin.getY();
        double heightShift = tileImageHeight / 4;

        // Berechnet x- und y-Koordinaten der vier Ecken eines Polygons auf dem Canvas

        double xCoordWest = xCoordOnCanvas;
        double yCoordWest = yCoordOnCanvas - heightWest * heightShift;

        double xCoordNorth = xCoordOnCanvas + tileImageWidthHalf;
        double yCoordNorth = yCoordOnCanvas - tileImageHeightHalf - heightNorth * heightShift;

        double xCoordEast = xCoordOnCanvas + tileImageWidth;
        double yCoordEast = yCoordOnCanvas - heightEast * heightShift;

        double xCoordSouth = xCoordOnCanvas + tileImageWidthHalf;
        double yCoordSouth = yCoordOnCanvas + tileImageHeightHalf - heightSouth * heightShift;

        // Speichert gefundene Canvas-Koordinaten als Points
        Point2D west = new Point2D(xCoordWest, yCoordWest);
        Point2D north = new Point2D(xCoordNorth, yCoordNorth);
        Point2D east = new Point2D(xCoordEast, yCoordEast);
        Point2D south = new Point2D(xCoordSouth, yCoordSouth);

        List<Point2D> coordsOnCanvas = new ArrayList<>();
        coordsOnCanvas.add(west);
        coordsOnCanvas.add(north);
        coordsOnCanvas.add(east);
        coordsOnCanvas.add(south);

        // Fügt Points der Map hinzu, falls nicht bereits enthalten
        if (!polygonEdgesToIsoCoords.keySet().contains(coordsOnCanvas)) {
            polygonEdgesToIsoCoords.put(coordsOnCanvas, new Point2D(row, col));
        }
    }

    /**
     * Berechnet ob sich ein Punkt innerhalb der Fläche eines Polygons befindet.
     * Idee: Ein Punkt liegt innerhalb des Polygons, wenn eine Gerade in der Ebene eine ungerade Anzahl an
     * Schnittpunkten mit den Kanten des Polygons hat, ansonsten liegt der Punkt außerhalb.
     *
     * @param mouseX
     * @param mouseY
     * @param coordsOnCanvas die Koordinaten der vier Ecken eines Polygons
     * @return
     */
    public boolean isPointInsidePolygon(double mouseX, double mouseY, List<Point2D> coordsOnCanvas) {

        boolean inside = false;

        // start und end sind Start- und Endpunkte der vier Kanten des Polygons
        for (int start = 0, end = coordsOnCanvas.size() - 1; start < coordsOnCanvas.size(); end = start++) {

            double xStart = coordsOnCanvas.get(start).getX();
            double yStart = coordsOnCanvas.get(start).getY();

            double xEnd = coordsOnCanvas.get(end).getX();
            double yEnd = coordsOnCanvas.get(end).getY();

            // prüft Anzahl an Schnittpunkten zwischen einer virtuellen Gerade und den Kanten des Polygons
            boolean intersect = ((yStart > mouseY) != (yEnd > mouseY)) &&
                    (mouseX < (xEnd - xStart) * (mouseY - yStart) / (yEnd - yStart) + xStart);
            if (intersect) inside = !inside;
        }

        return inside;

    }

    /**
     * Gibt isometrische Koordinate der gegebenen Mausposition zurück
     *
     * @param mouseX x-Koordinate der Mausposition
     * @param mouseY y-Koordinate der Mausposition
     * @return ein Point2D mit isometrischen Koordinaten
     */
    public Point2D findTileCoord(double mouseX, double mouseY) {
        Point2D newIsoCoord = null;
        for (Map.Entry<List<Point2D>, Point2D> entry : polygonEdgesToIsoCoords.entrySet()) {
            if (isPointInsidePolygon(mouseX, mouseY, entry.getKey())) {
                newIsoCoord = entry.getValue();
            }
        }
        return newIsoCoord;
    }

    /**
     * Soll die Koordinaten der Mausposition von Pixel zu isometrischen Koordinaten umrechnen
     * Die Methode geht von einem Standardverhältnis zwischen Breite und Höhe von 2:1 aus
     *
     * @param mouseX x-Koordinate der Mausposition
     * @param mouseY y-Koordinate der Mausposition
     * @return ein Point2D mit isometrischen Koordinaten
     */
    public Point2D findTileCoordForDrawMap(double mouseX, double mouseY) {

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
     * Zeichnet das Bild in ein Feld an dem angegebenen Ursprungspunkt
     *
     * @param image
     * @param transparent
     */
    public void drawTileImage(Point2D drawOrigin, Image image, boolean transparent, Map<String, Integer> cornerHeights) {

        // Wie weit ein Gebäude über die "normale" Höhe eines Tiles hinausragt
        double heightAboveTile = image.getHeight() - tileImageHeight;

        double xCoordOnCanvas = drawOrigin.getX();

        // nehme cornerS als Referenzpunkt (bei allen Tiles wird immer cornerS als Höhenreferenz genommen)
        double yCoordOnCanvas = drawOrigin.getY() - tileImageHeightHalf - heightAboveTile - heightOffset * cornerHeights.get("cornerS");

        // Zeichne ggf. halbtransparentes Bild beim Hovern
        if (transparent) canvas.getGraphicsContext2D().setGlobalAlpha(0.7);
        canvas.getGraphicsContext2D().drawImage(image, xCoordOnCanvas, yCoordOnCanvas);
        canvas.getGraphicsContext2D().setGlobalAlpha(1);
    }

    /**
     * Zeichnet das Bild in ein Feld an der angegebenen Zeile und Spalte
     *
     * @param image
     * @param transparent
     */
    public void drawTileImage(int row, int column, Image image, boolean transparent) {
        Map<String, Integer> cornerHeights = controller.getTileOfMapTileGrid(row, column).getCornerHeights();
        double heightAboveTile = image.getHeight() - tileImageHeight;

        Point2D drawOrigin = translateTileCoordsToCanvasCoords(row, column);
        double xCoordOnCanvas = drawOrigin.getX();
        double yCoordOnCanvas = drawOrigin.getY() - tileImageHeightHalf - heightAboveTile - heightOffset * cornerHeights.get("cornerS");


        if (transparent) canvas.getGraphicsContext2D().setGlobalAlpha(0.7);
        canvas.getGraphicsContext2D().drawImage(image, xCoordOnCanvas, yCoordOnCanvas);
        canvas.getGraphicsContext2D().setGlobalAlpha(1);
    }

    /**
     * Speichert die Verhältnisse von Höhe und Breite für alle Bilder in einer Map
     */
    public void storeImageRatios() {
        for (String name : objectToImageMapping.getImageNames()) {
            Image r = getResourceForImageName(name);
            double ratio = r.getHeight() / r.getWidth();
            imageNameToImageRatio.put(name, ratio);
        }
    }


    /**
     * Zeichnet das Bild für ein Bodenfeld dessen Grafik ein Verhältnis von 2:1 zwischen Breite und Höhe hat und
     * Berücksichtigt zur Platzierung die Höhe an der unteren Ecke (cornerS)
     *
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
     *
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
        if (heightAboveTile > 0) {
            // Tile bei dem die obere Ecke 2 Einheiten höher ist als die untere Ecke. In diesem Fall, ist
            // die Grafik besonders hoch
            if (name.equals("2101")) {
                yCoordOnCanvas += -tileImageHeight - heightOffset * cornerHeights.get("cornerS");
            } else {
                yCoordOnCanvas += -tileImageHeight + heightOffset - heightOffset * cornerHeights.get("cornerS");
            }

            // Wenn die zu zeichnende Grafik niedriger ist, als die eine Grafik mit "normaler" Höhe
        } else {
            // Tile bei dem die untere Ecke 2 Einheiten höher ist als die obere Ecke. In diesem Fall, ist
            // die Grafik besonders flach
            if (name.equals("0121")) {
                yCoordOnCanvas += -heightOffset * cornerHeights.get("cornerS");

            } else {
                yCoordOnCanvas += -heightOffset - heightOffset * cornerHeights.get("cornerS");
            }
        }

        canvas.getGraphicsContext2D().drawImage(image, xCoordOnCanvas, yCoordOnCanvas);
    }


    /**
     * Zeichnet ein building, das größer ist als 1x1, über mehrere tiles.
     *
     * @param tile     Das entsprechende tile, von dem ausgehend das building gezeichnet werden soll
     * @param building Das zu zeichnende building
     * @param row      Die Reihe in dem zweidimensionalen Array der tiles
     * @param column   Die Spalte in dem zweidimensionalen Array der tiles
     */
    public void drawBuildingOverMoreTiles(Tile tile, Building building, int row, int column) {
        if (tile.isBuildingOrigin()) {
            String buildingName = building.getBuildingName();
            String name = objectToImageMapping.getImageNameForObjectName(buildingName);
            double ratio = imageNameToImageRatio.get(name);

            double imageWidth = (tileImageWidth * 0.5) * (building.getDepth() + building.getWidth());
            double imageHeight = imageWidth * ratio;
            double heightOfFloorTiles = tileImageHeightHalf * (building.getDepth() + building.getWidth());
            double heightAboveFloorTiles = imageHeight - heightOfFloorTiles;

            Image im = getResourceForImageName(name, imageWidth, imageHeight);

            Point2D drawOrigin = translateTileCoordsToCanvasCoords(row, column);
            double xCoordOnCanvas = drawOrigin.getX();
            double yCoordOnCanvas = drawOrigin.getY() - tileImageHeightHalf * building.getDepth() - heightAboveFloorTiles;
            canvas.getGraphicsContext2D().drawImage(im, xCoordOnCanvas, yCoordOnCanvas);
        }
    }


    /**
     * Gibt ein Image für die geforderte Stelle in der Tile-Map zurück in der Breite eines Tiles
     *
     * @param column
     * @param row
     * @param fields
     * @return
     */
    public Image getSingleFieldImage(int column, int row, Tile[][] fields) {
        String name;
        String buildingName;
        Tile field = fields[row][column];
        if (field.isWater()) {
            buildingName = "water";
        } else if (field.getBuilding() == null) {
            throw new RuntimeException("Das muss man sich nochmal anschauen: kann ein Field ohne Building existieren?");
        } else {

            // Wenn es sich um ein Grasfeld handelt
            if (field.getBuilding().getBuildingName().equals("ground")) {

                Map<String, Integer> cornerHeights = fields[row][column].getCornerHeights();
                // Buildingname besteht aus vierstelligen Zahl aus Ziffern zwischen 0 und 2, die die relativen
                // Höhen der vier Ecken innerhalb des Tiles darstellen
                buildingName = fields[row][column].absoluteHeigtToRelativeHeight(cornerHeights);

            } else if (field.getBuilding().getBuildingName().contains("road")) {
                // Wenn es sich um ein Straßen-Tile mit Höhenunterschied handelt
                if (!(field.absoluteHeigtToRelativeHeight(field.getCornerHeights()).equals("0000"))) {
                    Map<String, Integer> cornerHeights = fields[row][column].getCornerHeights();
                    String heights = fields[row][column].absoluteHeigtToRelativeHeight(cornerHeights);
                    buildingName = field.getBuilding().getBuildingName();

                    if (!(buildingName.contains("ne-se") || buildingName.contains("nw-sw")
                            || buildingName.contains("se-sw") || buildingName.contains("ne-nw"))) {
                        buildingName = buildingName + heights;
                    }
                } else {
                    buildingName = field.getBuilding().getBuildingName();
                }

            } else {
                buildingName = field.getBuilding().getBuildingName();
            }
        }
        name = objectToImageMapping.getImageNameForObjectName(buildingName);
        double ratio = 0;
        if (name != null && imageNameToImageRatio.containsKey(name)) {

            ratio = imageNameToImageRatio.get(name);
            return getResourceForImageName(name, tileImageWidth, tileImageWidth * ratio);
        } else {
            // Sollte nie aufgerufen werden
            throw new RuntimeException("invalid building name " + name);
        }
    }

    /**
     * Gibt den Punkt auf dem Canvas an der linken Ecke des Tiles zurück im Bezug auf den aktuellen Ausschnitt der Karte
     * @param row    Die Reihe des Tiles betrachtet für die gesamte Karte
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
     * @param mousePosLabel Label, das die Koordinaten der Mausposition in Pixel anzeigen soll
     * @param isoCoordLabel Label, das die Koordinaten des angeklickten Tile anzeigen soll
     */
    public void showCoordinatesOnClick(Label mousePosLabel, Label isoCoordLabel) {

        canvas.setOnMouseClicked(event -> {
            double mouseX = event.getX();
            double mouseY = event.getY();

            String mouseCoordinates = controller.getResourceBundle().getString("mouseCoordinates");
            String mouseCoords = "   " + mouseCoordinates + " x: " + mouseX + " y: " + mouseY;
            mousePosLabel.setText(mouseCoords);


            // Findet isometrische Koordinaten der Mouseposition
            Point2D newIsoCoord = findTileCoord(mouseX, mouseY);
            if (newIsoCoord != null) {

                String tileCoordinates = controller.getResourceBundle().getString("tileCoordinates");
                String tileCoords = "   " + tileCoordinates + " x: " + newIsoCoord.getX() + " y: " + newIsoCoord.getY();
                isoCoordLabel.setText(tileCoords);

            } else {
                String outsideOfMap = controller.getResourceBundle().getString("outsideOfMap");
                isoCoordLabel.setText("   " + outsideOfMap);
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
        Image image = null;
        image = new Image(
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
        String url = "/" + gamemode + "/" + imageName + ".png";

        Image image = null;
        image = new Image(url);
        imageCache.put(imageName + "raw", image);
        return image;
    }

    /**
     * Ermittelt das richtige Bild für fahrendes Fahrzeug, je nach Richtung in die das Fahrzeug fährt
     * @param start
     * @param end
     */
    private String getImageNameForCar(Point2D start, Point2D end, String vehicleName) {

        String vehichleName = vehicleName;
        if (start.getX() < end.getX()) {
            //nach rechts oben fahren
            if (start.getY() > end.getY()) {
                vehichleName += "-ne";
            } else {
                vehichleName += "-se";
            }
        } else if (start.getX() > end.getX()) {
            // nach link oben fahren
            if (start.getY() < end.getY()) {
                vehichleName += "-sw";
            } else {
                vehichleName += "-nw";
            }
        } else {
            vehichleName += "-nw";
        }
        return objectToImageMapping.getImageNameForObjectName(vehichleName);
    }

    /**
     * Zeigt Fahrzeug-Animationen für die Punkte in dem jeweiligen movement an.
     */
    public void translateVehicles(List<VehicleMovement> movements) {


        List<VehicleAnimation> animations = new ArrayList<>();
        boolean clearOnFinished = false;
        for (VehicleMovement movement : movements) {
            Point2D startPosition = translateTileCoordsToCanvasCoords(movement.getStartPosition().coordsRelativeToMapOrigin());
            Point2D endPosition;
            if (movement.hasMoreThanOnePoint()) {
                endPosition = translateTileCoordsToCanvasCoords(movement.getLastPair().getKey().coordsRelativeToMapOrigin());
            } else {
                endPosition = startPosition;
            }
            if (movement.isLastMovementBeforeRemove()) {
                clearOnFinished = true;
            }
            //Prüfe ob Bewegung auf Canvas
            if ((endPosition.getX() < 0 || endPosition.getX() > canvas.getWidth() || endPosition.getY() < 0
                    || endPosition.getY() > canvas.getHeight())
                    && (startPosition.getX() < 0 || startPosition.getX() > canvas.getWidth()
                    || startPosition.getY() < 0 || startPosition.getY() > canvas.getHeight())) {
                //Dann liegt die Bewegung nicht auf dem sichtbaren Bereich. Tue also nichts
            } else {
                //Dann liegt Bewegung auf Canvas
                // Erstellt für jedes VehicleMovement-Objekt ein passendes VehicleAnimation-Objekt
                animations.add(getAnimationForMovement(movement));
            }
        }

        if (animations.size() == 0) {
            controller.waitForOneDay();
            return;
        }

        // Der Punkt auf dem Canvas am Anfang der Animation beim Tile an der Stelle x=0, y=0. Wird später verwendet,
        // um die Animation beim Verschieben der Karte immer noch an der richtigen Stelle anzuzeigen
        Point2D zeroPointAtStart = translateTileCoordsToCanvasCoords(0, 0);

        timer = new AnimationTimer() {
            int handleCalls = 0;

            @Override
            //Diese Methode wird während der Animation ständig aufgerufen. Sie soll das vorherig gezeichnete Bild des
            //Fahrzeugs überzeichnen und das Bild mit den neuen Coordinaten zeichnen. Dies tut sie für jedes Fahrzeug
            //das sich auf der Karte bewegt, also für jede VehicleAnimation in der Liste animations
            public void handle(long now) {

                handleCalls++;

                // Der Punkt auf dem Canvas zum aktuellen Zeitpunkt beim Tile an der Stelle x=0, y=0. Dadurch kann die
                //Verschiebung der Karte im Vergleich zum Beginn festgestellt werden.
                Point2D actualZeroPoint = translateTileCoordsToCanvasCoords(0, 0);
                double xShift = actualZeroPoint.getX() - zeroPointAtStart.getX();
                double yShift = actualZeroPoint.getY() - zeroPointAtStart.getY();

                // Wenn diese bedingung falsch ist, befindet sich das Fahrzeug nicht mehr auf der Karte. Die Karte wurde
                //dann vom Benutzer während der Animation so bewegt, dass die Animation nicht mehr im sichtbaren Bereich
                //liegt
                if (xShift < canvas.getWidth() && yShift < canvas.getHeight()) {

                    // Hier wird drawMap() aufgerufen um die vorherigen Bilder zu entfernen. Sollte eventuell anders
                    //gemacht werden, da das in Zukunft Performance-Probleme verursachen könnte
                    drawMap();
                    GraphicsContext gc = canvas.getGraphicsContext2D();


                    for (VehicleAnimation animation : animations) {
                        // Der aktuelle Wert für das passende Bild steht in dem imageName Property der VehicleAnimation
                        String imageName = animation.getImageName().getValue();

                        if (imageName == null) throw new RuntimeException("imageName was null");

                        double ratio = imageNameToImageRatio.get(imageName);
                        double imageWidth = tileImageWidthHalf;
                        double imageHeight = imageWidth * ratio;

                        Image vehicleImage = getResourceForImageName(imageName, imageWidth, imageHeight);

                        double[] directionShift =
                                getShiftForTrafficType(animation.getTrafficType(), imageName, imageWidth, imageHeight);

                        gc.drawImage(vehicleImage, animation.getxCoordProperty().doubleValue() + xShift + directionShift[0],
                                animation.getyCoordProperty().doubleValue() + yShift + directionShift[1]);
                    }
                }
            }
        };
        // Die parallelTransition sorgt für eine gleichzeitige Ausführung der Animationen. Dazu werden ihr alle timlines
        //hinzugefügt
        parallelTransition = new ParallelTransition();
        for (VehicleAnimation animation : animations) {
            parallelTransition.getChildren().add(animation.getTimeline());
        }
        getMenuPane().getAnimationButton().setDisable(false);
        boolean finalClearOnFinished = clearOnFinished;
        parallelTransition.setOnFinished(event -> {
            parallelTransition.stop();
            timer.stop();

            if (finalClearOnFinished) {
                drawMap();
            }

            //Rufe simulateOneDay auf, wenn Animation vorbei. Dadurch entsteht ein Kreislauf, da simulateOneDay dann
            //wieder translateVehicles aufruft.
            controller.gameLoop();

        });

        timer.start();
        parallelTransition.play();

    }

    /**
     * Gibt Verschiebung für die korrekte Platzierung auf Schienen und Straßen auf Canvas zurück.
     * Ist im Fall von TrafficType AIR immer 0,0
     * @param type
     * @param imageName
     * @param imageWidth
     * @param imageHeight
     * @return
     */
    private double[] getShiftForTrafficType(TrafficType type, String imageName, double imageWidth, double imageHeight) {
        double[] directionShift = new double[]{0, 0};
        if (type.equals(TrafficType.ROAD)) {
            directionShift = getRoadShiftForImageName(imageName, imageWidth, imageHeight);
        } else if (type.equals(TrafficType.RAIL)) {
            directionShift = getRailShiftForImageName(imageWidth, imageHeight);
        }
        return directionShift;
    }

    /**
     * Gibt Verschiebung zur Platzierung auf Canvas von Fahrzeugen des Typs RAIL zurück.
     * Die Werte sind so gewählt, dass es auf der Anzeige gut aussieht und haben keine mathematische Begründung
     * @param imageWidth
     * @param imageHeight
     * @return
     */
    private double[] getRailShiftForImageName(double imageWidth, double imageHeight) {
        double xshift = -imageWidth / 2;
        double yshift = -imageHeight * 0.75;

        return new double[]{xshift, yshift};
    }

    /**
     * Gibt Verschiebung zur Platzierung auf Canvas von Fahrzeugen des Typs ROAS zurück.
     * Die Werte sind so gewählt, dass es auf der Anzeige gut aussieht und haben keine mathematische Begründung
     * @param imageName
     * @param imageWidth
     * @param imageHeight
     * @return
     */
    private double[] getRoadShiftForImageName(String imageName, double imageWidth, double imageHeight) {
        String ending = imageName.substring(imageName.length() - 2);
        double xshift = -imageWidth / 2;
        double yshift = -imageHeight * 0.75;
        if (ending.equals("ne")) {
            xshift += 0.225 * imageWidth;
            yshift += 0.225 * imageHeight;
        } else if (ending.equals("nw")) {
            xshift += 0.125 * imageWidth;
            yshift -= 0.125 * imageHeight;
        } else if (ending.equals("se")) {
            xshift -= 0.225 * imageWidth;
            yshift += 0.225 * imageHeight;
        } else if (ending.equals("sw")) {
            xshift -= 0.125 * imageWidth;
            yshift -= 0.125 * imageHeight;
        }

        return new double[]{xshift, yshift};
    }

    /**
     * Erstellt zu dem VehicleMovement das passende VehicleAnimation Objekt und gibt es zurück
     * @param movement
     * @return
     */
    private VehicleAnimation getAnimationForMovement(VehicleMovement movement) {
        DoubleProperty x = new SimpleDoubleProperty();
        DoubleProperty y = new SimpleDoubleProperty();
        StringProperty imageName = new SimpleStringProperty();

        Point2D startPoint = translateTileCoordsToCanvasCoords(movement.getStartPosition().coordsRelativeToMapOrigin());
        Point2D secondPoint;
        if (movement.hasMoreThanOnePoint()) {
            secondPoint =
                    translateTileCoordsToCanvasCoords(movement.getPairOfPositionAndDistance(0).getKey().coordsRelativeToMapOrigin());
        } else {
            secondPoint = startPoint;
        }
        String startImageName = getImageNameForCar(startPoint, secondPoint, movement.getVehicleName());
        imageName.setValue(startImageName);

        // Ein KeyFrame gibt den Zustand einer Animation zu einer bestimmten Zeit an. Hier wird der Zustand zu Beginn
        // angegeben. Ein KeyFrame hat momentan 3 Variablen: Die x-Coordinate des fahrzeugs, die y-Coordinate des
        // Fahrzeugs und das passende Bild
        KeyFrame start = new KeyFrame(
                Duration.seconds(0.0),
                new KeyValue(x, startPoint.getX()),
                new KeyValue(y, startPoint.getY()),
                new KeyValue(imageName, startImageName));

        Timeline timeline = new Timeline(start);

        if (movement.isWait()) {
            KeyFrame frame = new KeyFrame(
                    Duration.seconds(tickDuration),
                    new KeyValue(x, startPoint.getX()),
                    new KeyValue(y, startPoint.getY()),
                    new KeyValue(imageName, startImageName)
            );
            timeline.getKeyFrames().add(frame);
        } else {
            double wholeDistance = movement.getWholeDistance();

            double time = 0.0;
            for (int i = 0; i < movement.getNumberOfPoints(); i++) {
                Pair<PositionOnTilemap, Double> pair = movement.getPairOfPositionAndDistance(i);
                double distanceToPosition = pair.getValue();
                // time gibt den Zeitpunkt des neuen KeyFrames an. Dabei wird anteilig der aktuell animierten Distanz zu der
                // gesamten Distanz hinzugefügt.
                time += (distanceToPosition / wholeDistance) * tickDuration;
                Point2D point = translateTileCoordsToCanvasCoords(pair.getKey().coordsRelativeToMapOrigin());
                String actualImageName;
                if (i != movement.getNumberOfPoints() - 1) {
                    // Bestimme das Bild des Fahrzeugs aus der aktuellen Position und der nächsten Position (deswegen i+1)
                    Point2D nextPoint =
                            translateTileCoordsToCanvasCoords(movement.getPairOfPositionAndDistance(i + 1).getKey().coordsRelativeToMapOrigin());
                    actualImageName = getImageNameForCar(point, nextPoint, movement.getVehicleName());
                } else {
                    // Wenn der aktuelle Punkt der letzte Punkt der Liste ist, kann i+1 nicht verwendet werden. Also wird
                    // stattdessen der vorherige Punkt (i-1) benutzt, um das Bild zu bestimmen
                    Point2D lastPoint;
                    if (i == 0) lastPoint = startPoint;
                    else
                        lastPoint =
                                translateTileCoordsToCanvasCoords(movement.getPairOfPositionAndDistance(i - 1).getKey().coordsRelativeToMapOrigin());
                    actualImageName = getImageNameForCar(lastPoint, point, movement.getVehicleName());
                }
                KeyFrame frame = new KeyFrame(
                        Duration.seconds(time),
                        new KeyValue(x, point.getX()),
                        new KeyValue(y, point.getY()),
                        new KeyValue(imageName, actualImageName)
                );
                timeline.getKeyFrames().add(frame);
            }
        }

        VehicleAnimation animation = new VehicleAnimation(x, y, timeline, imageName, movement.getTrafficType());
        return animation;
    }


    public void setMapWidth(int mapWidth) {
        this.mapWidth = mapWidth;
    }

    public void setMapDepth(int mapDepth) {
        this.mapDepth = mapDepth;
    }

    public double getTileImageWidth() {
        return tileImageWidth;
    }

    public TrafficLinePopup getTrafficLinePopup() {
        return menuPane.getTrafficLinePopup();
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

    public ObjectToImageMapping getObjectToImageMapping() {
        return objectToImageMapping;
    }

    public ParallelTransition getParallelTransition() {
        return parallelTransition;
    }

    public double getDayDuration() {
        return tickDuration;
    }

    public void setTickDuration(double tickDuration) {
        this.tickDuration = tickDuration;
    }

    public AnimationTimer getTimer() {
        return timer;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public ResourceBundle getResourceBundleFromController() {
        return controller.getResourceBundle();
    }

    public Scene getScene() {
        return scene;
    }
}
