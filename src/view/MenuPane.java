package view;

import controller.Controller;
import javafx.animation.ParallelTransition;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
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
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import javafx.util.Duration;
import javafx.util.StringConverter;
import model.*;

import java.util.*;


import javafx.scene.control.Button;

public class MenuPane extends AnchorPane {

    private List<Node> tabContents = new ArrayList();
    private List<String> tabNames = new ArrayList<>();
    private HBox hBox;
    private TabPane tabPane = new TabPane();
    private View view;
    private Canvas canvas;
    private Controller controller;
    private MouseEvent hoveredEvent;
    private int result;
    private HBox trafficPartTabContent;

    private boolean selectTrafficLineStationsMode = false;
    private TrafficLinePopup trafficLinePopup;

    // Wenn null, ist kein Bauwerk ausgewählt
    private Building selectedBuilding;

    ObjectToImageMapping mapping;

    private boolean run = true;
    private Button animationButton;
    private Slider slider;
    private Label dayLabel = new Label();
    private Locale locale;
    private ResourceBundle resourceBundle;

    public MenuPane(Controller controller, View view, Canvas canvas, ObjectToImageMapping mapping) {
        this.view = view;
        this.canvas = canvas;
        this.mapping = mapping;
        this.controller = controller;
        tabPane.setFocusTraversable(false);
        locale = controller.getLocale();
        resourceBundle = ResourceBundle.getBundle("Bundle", locale);

        setCanvasEvents();

        // HBox mit Reitern
        hBox = new HBox(tabPane);
        this.getChildren().add(hBox);
        generateTabContents();


        for (int i = 0; i < tabNames.size(); i++) {
            addTab(tabNames.get(i), tabContents.get(i), false);
        }
        createTrafficpartTab();

    }

    private void createTrafficpartTab(){

//        String name = "traffic part";
        String name = resourceBundle.getString("name");
        HBox box = boxWithLayout();
        tabNames.add(name);
        tabContents.add(box);
        addTab(name, box, false);
        trafficPartTabContent = box;
    }

    /**
     * Fügt einen Tab zu der tabPane hinzu
     * @param name
     * @param content
     */
    private Tab addTab(String name, Node content, boolean closeable) {
        Tab tab = new Tab();
        tab.setText(name);
        tab.setContent(content);
        tab.setClosable(closeable);
        tabPane.getTabs().add(tab);
        return tab;
    }

    /**
     * Erzeugt einen Button zum Starten/Pausieren von Simulation
     */
    private void createAnimationButton() {
        animationButton = new Button("PAUSE");
        animationButton.setDisable(view.getParallelTransition() == null);
        animationButton.setOnAction(e -> {

            ParallelTransition pt = view.getParallelTransition();

            if (pt != null) {
                if (run) {
                    animationButton.setText("START");
                    run = false;
                    pt.stop();
                    view.getTimer().stop();
                }
                else {
                    animationButton.setText("PAUSE");
                    run = true;
                    //pt.play();
                    view.getTimer().start();
                    pt.playFrom(Duration.seconds(1));
                }
            }
        });
    }

    /**
     * Erstellt einen Slider zum Steuern von Tick-Duration
     */
    private void createTickSlider() {
        slider = new Slider();
        slider.setLayoutX(view.getCanvas().getWidth()-15);
        slider.setMin(0.01);
        slider.setMax(5);
        slider.setValue(view.getTickDuration());
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setBlockIncrement(1);

        slider.valueProperty().addListener((observableValue, oldValue,newValue ) -> {
            view.setTickDuration(newValue.doubleValue());
        });
        slider.setLabelFormatter(new StringConverter<Double>() {
            String faster = resourceBundle.getString("faster");
            String slower = resourceBundle.getString("slower");
            @Override
            public String toString(Double n) {
                if (n == 0.01) return faster;
                return slower;
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
    }


    public void setDayLabel(int day){

        dayLabel.setText(resourceBundle.getString("dayLabel") + day);
//        dayLabel.setText("Current day: " + day);
        dayLabel.setFont(new Font("Arial", 15));
    }

    /**
     * Erstellt die Inhalte der Tabs in der tabPane nach den buildmenus in der JSONDatei und zusätzlich height und
     * vehicles
     */
    private void generateTabContents() {

        // Get Buildmenus from Controller
        Set<String> buildmenus = controller.getBuildmenus();

        String speed = resourceBundle.getString("speed");
        String height = resourceBundle.getString("height");
        String vehicles = resourceBundle.getString("vehicles");
        String remove = resourceBundle.getString("remove");

        tabNames.addAll(List.of(speed));
        tabNames.addAll(buildmenus);
        tabNames.addAll(List.of(height, vehicles, remove));

        // dummys:
        for (int i = 0; i < tabNames.size(); i++) {
            tabContents.add(new AnchorPane());
        }

        for (String name : tabNames) {
            ScrollPane scroll = new ScrollPane();
            scroll.setPrefViewportWidth(canvas.getWidth());
            scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
//            scroll.setFitToHeight(true);


//            scroll.setFitToWidth(true);
            scroll.setPannable(true);
            HBox container = boxWithLayout();
            List<Building> buildings = controller.getBuildingsByBuildmenu(name);
            for (Building building : buildings) {

                //TODO Wenn alle Grafiken fertig und eingebunden sind, sollten die zwei folgenden Zeilen gelöscht werden
                String imageName = mapping.getImageNameForObjectName(building.getBuildingName());
                if (imageName == null) continue;
                ImageView imageView = imageViewWithLayout(building);

                container.getChildren().add(imageView);
                //TODO
            }

            if (name.equals(height)) {
                Building height_up = new Building(1, 1, "height_up");
                height_up.setDz(2);
                ImageView imageViewUp = imageViewWithLayout(height_up);
                Building height_down = new Building(1, 1, "height_down");
                height_down.setDz(2);
                ImageView imageViewDown = imageViewWithLayout(height_down);
                container.getChildren().addAll(imageViewUp, imageViewDown);
            }

                if (name.equals(speed)) {
                    Button standardSpeedButton = new Button();
                    standardSpeedButton.setText(resourceBundle.getString("standardSpeed"));
                    standardSpeedButton.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            view.setTickDuration(1.0);
                            slider.setValue(view.getTickDuration());

                        }
                    });
                    container.getChildren().addAll(standardSpeedButton, dayLabel);

                    // erzeuge einen Button zum Starten/Pausieren von Simulation
                    createAnimationButton();
                    // erzeuge SLider
                    createTickSlider();
                    container.getChildren().add(0, animationButton);
                    container.getChildren().add(1, slider);
                    container.setSpacing(30);
                    container.setPadding(new Insets(20,20,20,20));
                }


                if (name.equals(remove)) {
                    Building removeBuilding = new Building(1, 1, "remove");
                    ImageView imageView = imageViewWithLayout(removeBuilding);
                    container.getChildren().add(imageView);
                }

                scroll.setContent(container);
                tabContents.set(tabNames.indexOf(name), scroll);
            }

    }

    /**
     * Erstellt eine HBox mit bestimmtem Layout
     * @return
     */
    private HBox boxWithLayout() {
        HBox box = new HBox(10);
        box.setPrefHeight(120);
        box.setPadding(new Insets(5, 20, 5, 20));
        return box;
    }

    /**
     * Gibt eine ImageView für das building zurück mit einem bestimmten Layout
     * @param building
     * @return
     */
    private ImageView imageViewWithLayout(Building building) {
        String imageName;
        imageName = mapping.getImageNameForObjectName(building.getBuildingName());
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

        if(isoCoord != null) {
            int xCoord = (int) isoCoord.getX();
            int yCoord = (int) isoCoord.getY();

            if (xCoord < 0 || yCoord < 0) {
                // Tu erstmal nichts
                return isoCoord;
            }
            if (controller.canPlaceBuildingAtPlaceInMapGrid(xCoord, yCoord, selectedBuilding)) {
                String imageName = mapping.getImageNameForObjectName(selectedBuilding.getBuildingName());
                if (selectedBuilding.getWidth() > 1 || selectedBuilding.getDepth() > 1) {
                    Tile tile = controller.getTileOfMapTileGrid(xCoord, yCoord);
                    tile.setBuildingOrigin(true);
                    view.drawBuildingOverMoreTiles(tile, selectedBuilding, xCoord, yCoord);
                    tile.setBuildingOrigin(false);
                } else {
                    double ratio = view.getImageNameToImageRatio().get(imageName);
                    double tileWidth = view.getTileImageWidth();
                    Image image = view.getResourceForImageName(imageName, tileWidth, tileWidth * ratio);
                    view.drawTileImage(xCoord, yCoord, image, transparent);
                }
                return isoCoord;
            } else return null;
        } return null;
    }

    public void showTrafficPart(ConnectedTrafficPart part){
        trafficPartTabContent.getChildren().clear();
        String trafficType = resourceBundle.getString("trafficType");
        Label type = new Label(trafficType + " \n"+part.getTrafficType().name());
        String stationsText = resourceBundle.getString("stationsText");
        Label stationList = new Label(stationsText);
        Pane box;
        if(part.getStations().size() > 4){
            HBox secondBox = boxWithLayout();
            VBox vbox1 = new VBox();
            VBox vbox2 = new VBox();
            vbox1.getChildren().add(stationList);
            vbox2.getChildren().add(new Label(""));
            secondBox.getChildren().addAll(vbox1, vbox2);
            for(int i=0; i<part.getStations().size(); i++){
                if(i<4){
                    vbox1.getChildren().add(new Label("ID: "+part.getStations().get(i).getId()));
                }
                else if(i<8) vbox2.getChildren().add(new Label("ID: "+part.getStations().get(i).getId()));
                else if(i==8){
                    vbox2.getChildren().remove(vbox2.getChildren().size()-1);
                    String andMore = resourceBundle.getString("andMore");
                    vbox2.getChildren().add(new Label(andMore));
                    break;
                }
            }
            box = secondBox;
        }
        else {
            List<Station> stations = part.getStations();
            VBox vbox = new VBox();
            vbox.getChildren().add(stationList);
            for (Station station : stations) {
                vbox.getChildren().add(new Label("ID: " + station.getId()));
            }
            box = vbox;
        }
        String buttonText = resourceBundle.getString("newTrafficLine");
        Button newTrafficLine = new Button(buttonText);
        // action event
        EventHandler<ActionEvent> event =
                new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent e)
                    {
                        showTrafficLineDialog();
                    }
                };
        newTrafficLine.setOnAction(event);
        trafficPartTabContent.getChildren().addAll(type, box, newTrafficLine);
        int index = tabContents.indexOf(trafficPartTabContent);
        tabPane.getSelectionModel().select(index);

//        trafficPartTabContent.getChildren().clear();
//        Label type = new Label("Traffic type: \n"+part.getTrafficType().name());
//        String stationList = "Stations \n";
//        for(Station station : part.getStations()){
//            stationList += "ID: "+station.getId()+"\n";
//        }
//        Label stations = new Label(stationList);
//        Button newTrafficLine = new Button("new Traffic Line");
//        trafficPartTabContent.getChildren().addAll(type, stations, newTrafficLine);
//        int index = tabContents.indexOf(trafficPartTabContent);
//        tabPane.getSelectionModel().select(index);
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

        // Bei einem Rechtsklick wird das ausgewählt Gebäude nicht mehr ausgewählt
        // Bei einem Linksklick wird das ausgewählte Gebäude platziert, sollte eines ausgewählt sein.
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton().compareTo(MouseButton.SECONDARY) == 0) {
                selectedBuilding = null;
                view.drawMap();
            } else if (
                    event.getButton().compareTo(MouseButton.PRIMARY) == 0 &&
                            selectedBuilding != null) {
                controller.managePlacement(event);
            }
            else if (
                    event.getButton().compareTo(MouseButton.PRIMARY) == 0 &&
                            selectedBuilding == null) {
                if(selectTrafficLineStationsMode){
                    System.out.println("selectTrafficLineStationsMode "+selectTrafficLineStationsMode);
                    controller.selectStationsForTrafficLine(event);
                }
                else controller.showTrafficPartInView(event);
            }
        });

        // Wenn die Maus mit einem Rechtsklick über mehrere Felder gezogen wird, werden mehrere Gebäude platziert
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, dragEvent -> {

            if (dragEvent.getButton().compareTo(MouseButton.PRIMARY) == 0 && selectedBuilding != null
                    && !(selectedBuilding.getBuildingName().equals("height_up"))
                    && !(selectedBuilding.getBuildingName().equals("height_down"))) {
                controller.managePlacement(dragEvent);
            }
        });
    }

    private void showTrafficLineDialog(){

        selectTrafficLineStationsMode = true;


        ChoiceDialog<TrafficType> trafficLineChoice = new ChoiceDialog<TrafficType>(TrafficType.ROAD, TrafficType.values());
        trafficLineChoice.setHeaderText("Traffic Type");
        trafficLineChoice.setContentText("Set the Traffic Type of the new Traffic Line");
        trafficLineChoice.showAndWait();
        if(!trafficLineChoice.getSelectedItem().equals(TrafficType.NONE)){
            // create a popup
            trafficLinePopup = new TrafficLinePopup(view, trafficLineChoice.getSelectedItem());
        }
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

    public TrafficLinePopup getTrafficLinePopup() {
        return trafficLinePopup;
    }

    public void setTrafficLinePopup(TrafficLinePopup trafficLinePopup) {
        this.trafficLinePopup = trafficLinePopup;
    }

    public boolean isSelectTrafficLineStationsMode() {
        return selectTrafficLineStationsMode;
    }

    public void setSelectTrafficLineStationsMode(boolean selectTrafficLineStationsMode) {
        this.selectTrafficLineStationsMode = selectTrafficLineStationsMode;
    }
}
