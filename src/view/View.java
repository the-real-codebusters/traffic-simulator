package view;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import model.BasicModel;
import model.Building;
import model.Field;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class View {
    private Stage stage;

    private final int tileWidth = 128;
    private final double tileWidthHalf = tileWidth / 2;
    private final int tileHeight = 64;
    private final double tileHeightHalf = tileHeight / 2;
    private int mapWidth;
    private int mapDepth;

    private Map<String, Double> imageNameToimageRatio = new HashMap<>();

    // Dies scheint die maximal einstellbare Größe eines Canvas zu sein. Bei größeren Angaben crasht das Programm
    private Canvas canvas = new Canvas(1000, 600);
    private double canvasCenterWidth = canvas.getWidth() / 2;
    private double canvasCenterHeight = canvas.getHeight() / 2;


    private int cameraOffsetX = 0;
    private int cameraOffsetY = 0;
    private Field[][] fields;

    private double previousMouseX = -1.0;
    private double previousMouseY = -1.0;

    // Gibt an, ab und bis welchen Index die Tiles des Arrays sichtbar sind
    int startIndex = (int) findTileCoord(0, 0).getX();
    int endIndex = (int) findTileCoord(canvasCenterWidth, canvasCenterHeight).getX();


    private BasicModel model;

    private Map<String, Image> imageCache = new HashMap<>();
    BuildingToImageMapping mapping;


    public View(Stage primaryStage, BasicModel model) {
        this.stage = primaryStage;
        this.model = model;
        mapping = new BuildingToImageMapping(model.getGamemode());
        fields = model.getFieldGridOfMap();

        Label isoCoordLabel = new Label();
        isoCoordLabel.setFont(new Font("Arial", 15));

        Label mousePosLabel = new Label();
        mousePosLabel.setFont(new Font("Arial", 15));

        BorderPane root = new BorderPane();
        VBox vBox = new VBox();
        root.setBottom(vBox);
        vBox.getChildren().addAll(mousePosLabel, isoCoordLabel);
        root.setCenter(canvas);
        root.setTop(new MenuPane(model, this, canvas, mapping));

        storeImageRatios();

        canvas.setFocusTraversable(true);
        scrollOnKeyPressed();
        showCoordinatesOnClick(mousePosLabel, isoCoordLabel);
        scrollOnMouseDragged();

        this.stage.setScene(new Scene(root));
    }


    public void scrollOnKeyPressed() {
        canvas.setOnKeyPressed(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ke) {
                int delta = tileWidth;
                if (ke.getCode() == KeyCode.DOWN) {
                    cameraOffsetY += delta / 2;
                } else if (ke.getCode() == KeyCode.UP) {
                    cameraOffsetY -= delta / 2;
                } else if (ke.getCode() == KeyCode.RIGHT) {
                    cameraOffsetX += delta;
                } else if (ke.getCode() == KeyCode.LEFT) {
                    cameraOffsetX -= delta;
                }
                System.out.println("OffsetX: " + cameraOffsetX);
                System.out.println("OffsetY: " + cameraOffsetY);
                drawMap();
            }
        });
    }

    public void scrollOnMouseDragged() {
        canvas.setOnMouseDragged(me -> {
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
        });
    }

    //TODO Checken: Sind die Zuordnungen von row -> Depth, column -> width so richtig? Ist es überall konsistent?

    /**
     * Zeichnet Map auf Canvas anhand der Daten eines Arrays von Fields
     */
    public void drawMap() {
        // Hintergrund wird schwarz gesetzt
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        int minimumX = (int) findTileCoord(0, 0).getX();
        int maximumX = (int) findTileCoord(canvas.getWidth(), canvas.getHeight()).getX();
        int minimumY = (int) findTileCoord(0, canvas.getHeight()).getY();
        int maximumY = (int) findTileCoord(canvas.getWidth(), 0).getY();

        // Es wird über das Array mit Breite mapWidth und Tiefe mapDepth iteriert
        for (int col = maximumY; col >= minimumY; col--) {
            for (int row = minimumX; row <= maximumX; row++) {

                if (row >= 0 && col >= 0 && row < fields.length && col < fields[0].length) {
                    Field field = fields[row][col];
                    Building building = field.getBuilding();
                    if (building != null && (building.getWidth() > 1 || building.getDepth() > 1)) {
                        if (field.isBuildingOrigin()) {
                            drawBuildingOverMoreTiles(field, building, row, col);
                        }
                    } else {
                        Image image = getSingleFieldImage(col, row, fields);
                        drawTileImage(col, row, image, false);
                    }
                }
            }
        }
    }

    private void storeImageRatios(){
        for(String name : mapping.getImageNames()){
            Image r = getResourceForImageName(name);
            double ratio = r.getHeight() / r.getWidth();
            imageNameToimageRatio.put(name, ratio);
        }
    }


    public void drawBuildingOverMoreTiles(Field field, Building building, int row, int column) {
        if (field.isBuildingOrigin()) {
            String buildingName = field.getBuilding().getBuildingName();
            String name = mapping.getImageNameForBuildingName(buildingName);
            double ratio = imageNameToimageRatio.get(name);

//            double imageWidth = tileWidth + (tileWidth * 0.5) * (building.getDepth() + building.getWidth() - 2);
        double imageWidth = (tileWidth * 0.5) * (building.getDepth() + building.getWidth());
            double imageHeight = imageWidth * ratio;
            double heightRatio = imageHeight / tileHeight;

            Image im = getResourceForImageName(name, imageWidth, imageHeight);

            double tileX = (row + column) * tileWidthHalf;
            double tileY = (row - column) * tileHeightHalf - tileHeightHalf * heightRatio + tileHeightHalf;
            Point2D drawOrigin = moveCoordinates(tileX, tileY);
            canvas.getGraphicsContext2D().drawImage(im, drawOrigin.getX(), drawOrigin.getY());
        }
    }

    public Image getSingleFieldImage(int column, int row, Field[][] fields) {
        String name;
        String buildingName;
        if (column < 0 || row < 0 || column >= mapWidth || row >= mapDepth) name = "black";
        else {
            Field field = fields[row][column];
            if (field.getHeight() < 0) {
                buildingName = "water";
            } else if (field.getBuilding() == null) {
                throw new RuntimeException("Das muss man sich nochmal anschauen: kann ein Field ohne Building existieren?");
            } else buildingName = field.getBuilding().getBuildingName();
            name = mapping.getImageNameForBuildingName(buildingName);
        }

        return getResourceForImageName(name, tileWidth, tileHeight);
    }

    public void drawTileImage(int column, int row, Image image, boolean transparent) {

        // TileX und TileY berechnet Abstand der Position von einem Bild zum nächsten in Pixel
        // Zeichenreihenfolge von oben rechts nach unten links
        double tileX = (row + column) * tileWidthHalf;
        double tileY = (row - column) * tileHeightHalf;

        Point2D drawOrigin = moveCoordinates(tileX, tileY);

        if (transparent) canvas.getGraphicsContext2D().setGlobalAlpha(0.7);
        canvas.getGraphicsContext2D().drawImage(image, drawOrigin.getX(), drawOrigin.getY());
        canvas.getGraphicsContext2D().setGlobalAlpha(1);
    }

    private Point2D moveCoordinates(double tileX, double tileY) {
        // Differenz zwischen Breite und Tiefe der Map
        double differenceWidthHeigth = mapWidth - mapDepth;

        double tileOffset = differenceWidthHeigth * 0.25;

        // Zeichenreihenfolge von oben rechts nach unten links
        double startX = tileX + canvasCenterWidth - tileWidthHalf * mapWidth + (tileOffset * tileWidth);
        double startY = tileY + canvasCenterHeight - tileHeightHalf - (tileOffset * tileHeight);
        startX -= cameraOffsetX;
        startY -= cameraOffsetY;
//        System.out.println(startX+"  "+startY);
        return new Point2D(startX, startY);
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
            offsetX = tileWidthHalf / tileWidth;
        }
        if (mapDepth % 2 != 0) {
            offsetY = tileHeightHalf / tileHeight;
        }

        double x = Math.floor((mouseX / tileWidth + mouseY / tileHeight) - canvasCenterHeight / tileHeight
                - (canvasCenterWidth / tileWidth) + (mapWidth / 2) + offsetX) + cameraOffsetX / tileWidth + cameraOffsetY / tileHeight;
        double y = Math.floor((mouseX / tileWidth - mouseY / tileHeight) + canvasCenterHeight / tileHeight
                - (canvasCenterWidth / tileWidth) + (mapDepth / 2) + offsetY) - cameraOffsetY / tileHeight + cameraOffsetX / tileWidth;
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
            Point2D isoCoord = findTileCoord(mouseX, mouseY);

            String tileCoords = "Tile coordinates: x: " + isoCoord.getX() + " y: " + isoCoord.getY();
            isoCoordLabel.setText(tileCoords);
        });
    }

    public Image getResourceForImageName(String imageName, double width, double height) {

        int widthAsInt = (int) Math.round(width);
        int heightAsInt = (int) Math.round(height);

        Image cachedImage = imageCache.get(imageName + widthAsInt + heightAsInt);
        if (cachedImage != null) {
            return cachedImage;
        }

        String gamemode = model.getGamemode();
        Image image = new Image(
                "/" + gamemode + "/" + imageName + ".png",
                widthAsInt,
                heightAsInt,
                false,
                true);
        imageCache.put(imageName + widthAsInt + heightAsInt, image);
        return image;
    }

    public Image getResourceForImageName(String imageName) {
        Image cachedImage = imageCache.get(imageName + "raw");
        if (cachedImage != null) {
            System.out.println("Used cached image " + imageName);
            return cachedImage;
        }

        String gamemode = model.getGamemode();
        Image image = new Image("/" + gamemode + "/" + imageName + ".png");
        imageCache.put(imageName + "raw", image);
        System.out.println("Image neugeladen " + imageName);
        return image;
    }

    private Dimension getDimensionForImageName(String imageName) {
        try {
            ImageInputStream in = ImageIO.createImageInputStream(imageName);
            final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    reader.setInput(in);
                    return new Dimension(reader.getWidth(0), reader.getHeight(0));
                } finally {
                    reader.dispose();
                }
            }
        } catch (IOException e) {
            System.out.println(e);
            // TODO handle exception
        }
        return null;
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

    public int getCameraOffsetX() {
        return cameraOffsetX;
    }

    public int getCameraOffsetY() {
        return cameraOffsetY;
    }
}

