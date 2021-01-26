package view;

import controller.Controller;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MenuPane extends AnchorPane {

    private List<Node> tabContents = new ArrayList();
    private List<String> tabNames = new ArrayList<>();
    private HBox hBox;
    private TabPane tabPane = new TabPane();
    private BasicModel model;
    private View view;
    private Canvas canvas;
    private Controller controller;

    // Wenn null, ist kein Bauwerk ausgewählt
    private Building selectedBuilding;

    private Point2D hoveredTileBefore;
    ObjectToImageMapping mapping;

    public MenuPane(BasicModel model, View view, Canvas canvas, ObjectToImageMapping mapping) {
        this.model = model;
        this.view = view;
        this.canvas = canvas;
        this.mapping = mapping;
        tabPane.setFocusTraversable(false);

        setCanvasEvents();

        hBox = new HBox(tabPane);
        this.getChildren().add(hBox);

        generateTabContents();

        for (int i = 0; i < tabNames.size(); i++) {
            addTab(tabNames.get(i), tabContents.get(i));
        }
    }

    private void addTab(String name, Node content) {
        Tab tab = new Tab();
        tab.setText(name);
        tab.setContent(content);
        tabPane.getTabs().add(tab);
    }

    private void generateTabContents() {

        // Get Buildmenus from Model
        Set<String> buildmenus = model.getBuildmenus();

        tabNames.addAll(buildmenus);
        tabNames.addAll(List.of("height", "vehicles"));

        // dummys:
        for (int i = 0; i < tabNames.size(); i++) {
            tabContents.add(new AnchorPane());
        }

        for (String name : tabNames) {
            HBox container = boxWithLayout();
            List<Building> buildings = model.getBuildingsForBuildmenu(name);
            for (Building building : buildings) {

                //TODO Wenn alle Grafiken fertig und eingebunden sind, sollten die zwei folgenden Zeilen gelöscht werden
                String imageName = mapping.getImageNameForBuildingName(building.getBuildingName());
                if (imageName == null) continue;
                ImageView imageView = imageViewWithLayout(building);
                container.getChildren().add(imageView);
                //TODO
            }
            tabContents.set(tabNames.indexOf(name), container);
        }
    }

    private HBox boxWithLayout() {
        HBox box = new HBox(10);
        box.setPrefHeight(100);
        box.setPadding(new Insets(5, 20, 5, 20));
        return box;
    }

    private ImageView imageViewWithLayout(Building building) {
        String imageName = mapping.getImageNameForBuildingName(building.getBuildingName());
        Image image = view.getResourceForImageName(imageName);
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(90);
        imageView.setOnMouseClicked(event -> {
            // TODO
            selectedBuilding = building;
        });
        return imageView;
    }

    /**
     * @param mouseEvent
     * @param transparent
     * @return Gibt die Koordinaten des Tiles zurück, auf das gezeichnet wurde
     */
    private Point2D drawHoveredImage(MouseEvent mouseEvent, boolean transparent) {
        double mouseX = mouseEvent.getX();
        double mouseY = mouseEvent.getY();
        Point2D isoCoord = view.findTileCoord(mouseX, mouseY);
        int xCoord = (int) isoCoord.getX();
        int yCoord = (int) isoCoord.getY();

        if (xCoord < 0 || yCoord < 0) {
            // Tu erstmal nichts
            return isoCoord;
        }
        if (model.getMap().canPlaceBuilding(xCoord, yCoord, selectedBuilding)) {
            String imageName = mapping.getImageNameForBuildingName(selectedBuilding.getBuildingName());
            if(selectedBuilding.getWidth() > 1 || selectedBuilding.getDepth() > 1){
                Tile tile = model.getFieldGridOfMap()[xCoord][yCoord];
                tile.setBuildingOrigin(true);
                view.drawBuildingOverMoreTiles(tile, selectedBuilding, xCoord, yCoord);
                tile.setBuildingOrigin(false);
            }
            else {
                double ratio = view.getImageNameToImageRatio().get(imageName);
                double tileWidth = view.getTileImageWidth();
                Image image = view.getResourceForImageName(imageName, tileWidth, tileWidth * ratio);
                view.drawTileImage(yCoord, xCoord, image, transparent);
            }
            return isoCoord;
        } else return hoveredTileBefore;
    }

    private void setCanvasEvents() {
        canvas.setOnMouseMoved(event -> {
            if (selectedBuilding != null) {

                if (hoveredTileBefore != null) {
                    view.drawMap();
                }

                hoveredTileBefore = drawHoveredImage(event, true);
            }
        });


        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton().compareTo(MouseButton.SECONDARY) == 0) {
                selectedBuilding = null;
                view.drawMap();
            } else if (
                    event.getButton().compareTo(MouseButton.PRIMARY) == 0 &&
                            selectedBuilding != null) {
                managePlacement(event);
            }
        });


        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, dragEvent -> {

            if (dragEvent.getButton().compareTo(MouseButton.PRIMARY) == 0 &&
                    selectedBuilding != null) {
                managePlacement(dragEvent);
            }
        });
    }

    /**
     * Die Methode bekommt ein event übergeben und prüft, ob ein Gebäude platziert werden darf. Ist dies der Fall, so
     * wird außerdem geprüft, ob es sich beim zu platzierenden Gebäude um eine Straße handelt und ob diese mit dem
     * ausgewählten Feld kombiniert werden kann. Anschließend wird das Gebäude auf der Karte platziert und die
     * entsprechenden Points dem Verkehrsgraph hinzugefügt.
     * @param event MouseEvent, wodurch die Methode ausgelöst wurde
     */
    public void managePlacement(MouseEvent event) {
        // TODO gehört die Methode evtl. eher in den Controller?
        double mouseX = event.getX();
        double mouseY = event.getY();
        Point2D isoCoord = view.findTileCoord(mouseX, mouseY);
        int xCoord = (int) isoCoord.getX();
        int yCoord = (int) isoCoord.getY();

        String originalBuildingName = selectedBuilding.getBuildingName();

        if (model.getMap().canPlaceBuilding(xCoord, yCoord, selectedBuilding)) {

            if (selectedBuilding instanceof Road) {
                checkCombines(xCoord, yCoord);
            }
            model.getMap().placeBuilding(xCoord, yCoord, selectedBuilding);
            selectedBuilding.setBuildingName(originalBuildingName);

            if(selectedBuilding instanceof PartOfTrafficGraph){
                model.addPointsToGraph((PartOfTrafficGraph) selectedBuilding, xCoord, yCoord);
            }

            view.drawMap();
            view.translateCar();
        }
    }


    /**
     * Überprüft, ob das zu platzierende Straßenfeld mit dem ausgewählten Straßenfeld auf der Map Feld kombiniert
     * werden kann. Falls dies der Fall ist, wird das daraus entstehende Feld im Model gespeichert und in der View
     * angezeigt
     *
     * @param xCoord x-Koordinate des angeklickten Feldes, auf das die Straße gebaut werden soll
     * @param yCoord y-Koordinate des angeklickten Feldes, auf das die Straße gebaut werden soll
     */
    public void checkCombines(int xCoord, int yCoord) {

        Tile selectedField = model.getMap().getFieldGrid()[xCoord][yCoord];
        Building buildingOnSelectedTile = selectedField.getBuilding();
        if (buildingOnSelectedTile instanceof Road) {
            Map<String, String> combinations = ((Road) selectedBuilding).getCombines();
            for (Map.Entry<String, String> entry : combinations.entrySet()) {
                if (buildingOnSelectedTile.getBuildingName().equals(entry.getKey())) {
                    String newBuildingName = entry.getValue();

                    System.out.println(selectedBuilding.getBuildingName() + " and " +
                            buildingOnSelectedTile.getBuildingName() + " can be combined to " + newBuildingName);

                    selectedBuilding.setBuildingName(newBuildingName);
                    selectedField.setBuilding(buildingOnSelectedTile);
                    // Wenn eine Kombination einmal gefunden wurde, soll nicht weiter gesucht werden
                    break;

                } else {
                    selectedBuilding.setBuildingName(buildingOnSelectedTile.getBuildingName());
                    selectedField.setBuilding(buildingOnSelectedTile);

                }
            }
        }
    }

    public Controller getController() {
        return controller;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }
}
