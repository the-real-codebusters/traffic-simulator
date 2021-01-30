package view;

import controller.Controller;
import javafx.animation.ParallelTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.util.StringConverter;
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
    private View view;
    private Canvas canvas;
    private Controller controller;
    private MouseEvent hoveredEvent;

    // Wenn null, ist kein Bauwerk ausgewählt
    private Building selectedBuilding;

    ObjectToImageMapping mapping;

    private boolean run = true;
    private Button animationButton;

    public MenuPane(Controller controller, View view, Canvas canvas, ObjectToImageMapping mapping) {
        this.view = view;
        this.canvas = canvas;
        this.mapping = mapping;
        this.controller = controller;
        tabPane.setFocusTraversable(false);


        setCanvasEvents();

        animationButton = new Button("||");
        animationButton.setDisable(view.getParallelTransition() == null);
        animationButton.setOnAction(e -> {

            ParallelTransition pt = view.getParallelTransition();

            if (pt != null) {
                if (run) {
                    animationButton.setText(">");
                    run = false;
                    pt.stop();
                }
                else {
                    animationButton.setText("||");
                    run = true;
                    //pt.play();
                    pt.playFrom(Duration.seconds(1));
                }
            }
        });
        hBox = new HBox(tabPane);
        HBox buttonBox = new HBox(animationButton);
        buttonBox.setLayoutX(400);
        this.getChildren().add(hBox);
        generateTabContents();

        createTickSlider();

        for (int i = 0; i < tabNames.size(); i++) {
            addTab(tabNames.get(i), tabContents.get(i));
        }
        this.getChildren().add(buttonBox);
        //this.getChildren().add(slider);
    }

    /**
     * Fügt einen Tab zu der tabPane hinzu
     * @param name
     * @param content
     */
    private void addTab(String name, Node content) {
        Tab tab = new Tab();
        tab.setText(name);
        tab.setContent(content);
        tabPane.getTabs().add(tab);
    }

    /**
     * Erstellt einen Slider zum Steuern von Tick-Duration
     */
    private void createTickSlider() {
        Slider slider = new Slider();
        slider.setLayoutX(450);
        slider.setMin(0.01);
        slider.setMax(5);
        slider.setValue(view.getTickDuration());
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(50); //?
        slider.setMinorTickCount(5); //?
        slider.setBlockIncrement(1); //?
        slider.valueProperty().addListener((observableValue, oldValue,newValue ) -> {
            view.setTickDuration(newValue.doubleValue());
        });
        slider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double n) {
                if (n == 0.01) return "faster";
                return "slower";
            }

            @Override
            public Double fromString(String s) {
                switch (s) {
                    case "faster":
                        return 0.01;
                    default:
                        return 5.0;
                }
            }
        });
        this.getChildren().add(slider);
    }

    /**
     * Erstellt die Inhalte der Tabs in der tabPane nach den buildmenus in der JSONDatei und zusätzlich height und
     * vehicles
     */
    private void generateTabContents() {

        // Get Buildmenus from Controller
        Set<String> buildmenus = controller.getBuildmenus();

        tabNames.addAll(buildmenus);
        tabNames.addAll(List.of("height", "vehicles"));

        // dummys:
        for (int i = 0; i < tabNames.size(); i++) {
            tabContents.add(new AnchorPane());
        }

        for (String name : tabNames) {
            HBox container = boxWithLayout();
            List<Building> buildings = controller.getBuildingsByBuildmenu(name);
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

    /**
     * Erstellt eine HBox mit bestimmtem Layout
     * @return
     */
    private HBox boxWithLayout() {
        HBox box = new HBox(10);
        box.setPrefHeight(100);
        box.setPadding(new Insets(5, 20, 5, 20));
        return box;
    }

    /**
     * Gibt eine ImageView für das building zurück mit einem bestimmten Layout
     * @param building
     * @return
     */
    private ImageView imageViewWithLayout(Building building) {
        String imageName = mapping.getImageNameForBuildingName(building.getBuildingName());
        Image image = view.getResourceForImageName(imageName);
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(90);
        imageView.setOnMouseClicked(event -> {
            selectedBuilding = building;
            hoveredEvent = null;
        });
        return imageView;
    }

    /**
     * Zeichnet ein transparentes Bild als Vorschau für ein zu platzierendes Gebäude
     * @param mouseEvent
     * @param transparent
     * @return Gibt die Koordinaten des Tiles zurück, auf das gezeichnet wurde
     */
    public Point2D drawHoveredImage(MouseEvent mouseEvent, boolean transparent) {
        double mouseX = mouseEvent.getX();
        double mouseY = mouseEvent.getY();
        Point2D isoCoord = view.findTileCoord(mouseX, mouseY);
        int xCoord = (int) isoCoord.getX();
        int yCoord = (int) isoCoord.getY();

        if (xCoord < 0 || yCoord < 0) {
            // Tu erstmal nichts
            return isoCoord;
        }
        if (controller.canPlaceBuildingAtPlaceInMapGrid(xCoord, yCoord, selectedBuilding)) {
            String imageName = mapping.getImageNameForBuildingName(selectedBuilding.getBuildingName());
            if(selectedBuilding.getWidth() > 1 || selectedBuilding.getDepth() > 1){
                Tile tile = controller.getTileOfMapTileGrid(xCoord, yCoord);
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
        } else return null;
    }

    /**
     * Setzt einige Reaktionen auf Events auf dem canvas der View
     */
    private void setCanvasEvents() {

        // Zeichnet eine Vorschau eines Gebäudes wenn sich der Mauszeiger über einem Tile befindet
        canvas.setOnMouseMoved(event -> {
            if (selectedBuilding != null) {
                hoveredEvent = event;
                view.drawMap();
            }
        });

        // Bei einem Linksklick wird das ausgewählt Gebäude nicht mehr ausgewählt
        // Bei einem Rechtsklick wird das ausgewählte Gebäude platziert, sollte eines ausgewählt sein.
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton().compareTo(MouseButton.SECONDARY) == 0) {
                selectedBuilding = null;
                view.drawMap();
            } else if (
                    event.getButton().compareTo(MouseButton.PRIMARY) == 0 &&
                            selectedBuilding != null) {
                controller.managePlacement(event);
            }
        });

        // Wenn die Maus mit einem Rechtsklick über mehrere Felder gezogen wird, werden mehrere Gebäude platziert
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, dragEvent -> {

            if (dragEvent.getButton().compareTo(MouseButton.PRIMARY) == 0 &&
                    selectedBuilding != null) {
                controller.managePlacement(dragEvent);
            }
        });
    }


    public Controller getController() {
        return controller;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public Building getSelectedBuilding() {
        return selectedBuilding;
    }

    public MouseEvent getHoveredEvent() {
        return hoveredEvent;
    }

    public Button getAnimationButton() {
        return animationButton;
    }
}
