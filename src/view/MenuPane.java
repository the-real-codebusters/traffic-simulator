package view;

import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MenuPane extends AnchorPane {

    private List<Node> tabContents = new ArrayList();
    private List<String> tabNames = new ArrayList<>();
    private HBox hBox;
    private TabPane tabPane = new TabPane();
    private BasicModel model;
    private View view;
    private Canvas canvas;

    // Wenn null, ist kein Bauwerk ausgewählt
    private Building selectedBuilding;

    private Point2D hoveredTileBefore;
    BuildingToImageMapping mapping;

    public MenuPane(BasicModel model, View view, Canvas canvas, BuildingToImageMapping mapping) {
        this.model = model;
        this.view = view;
        this.canvas = canvas;
        this.mapping = mapping;
        tabPane.setFocusTraversable(false);

        drawHoveredImageOnMouseMoved();

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
                double tileWidth = view.getTileWidth();
                Image image = view.getResourceForImageName(imageName, tileWidth, tileWidth * ratio);
                view.drawTileImage(yCoord, xCoord, image, transparent);
            }
            return isoCoord;
        } else return hoveredTileBefore;
    }

    private void drawHoveredImageOnMouseMoved() {
        canvas.setOnMouseMoved(event -> {
            if (selectedBuilding != null) {

                if (hoveredTileBefore != null) {
                    view.drawMap();
                }

                hoveredTileBefore = drawHoveredImage(event, true);
            }
        });
    }


    public Building getSelectedBuilding() {
        return selectedBuilding;
    }
}
