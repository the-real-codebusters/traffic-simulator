package view;

import controller.Controller;
import javafx.animation.ParallelTransition;
import javafx.event.ActionEvent;
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

    private final List<Node> tabContents = new ArrayList();
    private final List<String> tabNames = new ArrayList<>();
    private HBox hBox;
    private final TabPane tabPane = new TabPane();
    private final View view;
    private final Canvas canvas;
    private Controller controller;
    private MouseEvent hoveredEvent;
    private HBox trafficPartTabContent;
    private HBox factoryTabContent;

    // Factory labels
    private Label factoryNameLabel;
    private Label productionLabel;
    private Label consumptionLabel;
    private Label factoryMaximaStorageLabel;
    private Label factoryRealStorageLabel;
    private Label factoryNearStationsLabel;
    private ImageView factoryImageView;

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
        createTrafficPartTab();
        createFactoryTab();
    }

    /**
     * Erzeugt Tab für TrafficPart
     */
    private void createTrafficPartTab(){

        String trafficPart = resourceBundle.getString("trafficPart");
        HBox box = boxWithLayout();
        tabNames.add(trafficPart);
        tabContents.add(box);
        addTab(trafficPart, box, false);
        trafficPartTabContent = box;
    }


    /**
     * Erzeugt Factory-Tab
     */
    private void createFactoryTab() {
        String name = resourceBundle.getString("factory");
        VBox box = new VBox(10);
        box.setPrefHeight(120);
        box.setPadding(new Insets(5, 20, 5, 20));

        Label factoryNameLabel = new Label();
        Label productionLabel = new Label();
        Label consumptionLabel = new Label();
        factoryRealStorageLabel = new Label();
        factoryMaximaStorageLabel = new Label();
        factoryNearStationsLabel = new Label();
        factoryImageView = new ImageView();
        factoryImageView.setPreserveRatio(true);
        factoryImageView.setFitHeight(90);

        VBox box2 = new VBox(factoryRealStorageLabel, factoryMaximaStorageLabel, factoryNearStationsLabel);
        box2.setSpacing(10);
        box2.setPrefHeight(120);
        box2.setPadding(new Insets(5, 20, 5, 20));
        HBox hbox = boxWithLayout();
        hbox.getChildren().addAll(factoryImageView, box, box2);

        factoryRealStorageLabel.setFont(new Font("Arial", 15));
        factoryMaximaStorageLabel.setFont(new Font("Arial", 15));
        factoryNearStationsLabel.setFont(new Font("Arial", 15));

        factoryNameLabel.setFont(new Font("Arial", 15));
        box.getChildren().add(factoryNameLabel);
        productionLabel.setFont(new Font("Arial", 15));
        box.getChildren().add(productionLabel);
        consumptionLabel.setFont(new Font("Arial", 15));
        box.getChildren().add(consumptionLabel);
        factoryNameLabel.setText(resourceBundle.getString("factory name"));
        productionLabel.setText(resourceBundle.getString("production"));
        consumptionLabel.setText(resourceBundle.getString("consumption"));

        setFactoryLabels(factoryNameLabel, productionLabel, consumptionLabel);
        factoryTabContent = hbox;
        addTab(name, hbox, false);

        tabNames.add(name);
        tabContents.add(hbox);
    }


    public void setFactoryLabels(Label factoryNameLabel, Label productionLabel, Label consumptionLabel){
        this.factoryNameLabel = factoryNameLabel;
        this.productionLabel = productionLabel;
        this.consumptionLabel = consumptionLabel;
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

    /**
     * Setzt den Text des Labels auf den aktuellen Tag
     * @param day aktueller Tag
     */
    public void setDayLabel(int day){

        dayLabel.setText(resourceBundle.getString("dayLabel") + day);
        dayLabel.setFont(new Font("Arial", 15));
    }

    /**
     * Erstellt die Inhalte der Tabs in der tabPane nach den buildmenus in der JSONDatei und zusätzlich height und
     * remove und speed
     */
    private void generateTabContents() {

        // Holt buildmenus aus Controller
        Set<String> buildmenus = controller.getBuildmenus();
        Set<String> buildmenusLocalized = new HashSet<>();

        // Übersetze Namen der Einträge aus Buildmenus (Airport, Road, Rail usw)
        for (String buildmenu : buildmenus){
            buildmenusLocalized.add(resourceBundle.getString(buildmenu));
        }


        String speed = resourceBundle.getString("speed");
        String height = resourceBundle.getString("height");
        String remove = resourceBundle.getString("remove");

        tabNames.addAll(List.of(speed));
        tabNames.addAll(buildmenusLocalized);
        tabNames.addAll(List.of(height, remove));

        // dummys:
        for (int i = 0; i < tabNames.size(); i++) {
            tabContents.add(new AnchorPane());
        }

        for (String tabName : tabNames) {

            ScrollPane scroll = new ScrollPane();
            scroll.setPrefViewportWidth(canvas.getWidth());
            scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scroll.setPannable(true);

            HBox container = boxWithLayout();

            // Nehme Englische Einträge in ResourceBundle für die buildmenus
            Locale locale = new Locale("en_US");
            ResourceBundle bundleEN = ResourceBundle.getBundle("Bundle", locale);

            if(bundleEN.getString(tabName).equals("road") || bundleEN.getString(tabName).equals("rail") ||
                    bundleEN.getString(tabName).equals("airport") || bundleEN.getString(tabName).equals("nature")) {

                List<Building> buildings = controller.getBuildingsByBuildmenu(bundleEN.getString(tabName));
                for (Building building : buildings) {
                    ImageView imageView = imageViewWithLayout(building);

                    container.getChildren().add(imageView);
                }
            }

            if (tabName.equals(height)) {
                Building height_up = new Building(1, 1, "height_up");
                height_up.setDz(2);
                ImageView imageViewUp = imageViewWithLayout(height_up);
                Building height_down = new Building(1, 1, "height_down");
                height_down.setDz(2);
                ImageView imageViewDown = imageViewWithLayout(height_down);
                container.getChildren().addAll(imageViewUp, imageViewDown);
            }

            if (tabName.equals(speed)) {
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
                // erzeuge Slider
                createTickSlider();
                container.getChildren().add(0, animationButton);
                container.getChildren().add(1, slider);
                container.setSpacing(30);
                container.setPadding(new Insets(20, 20, 20, 20));
            }


            if (tabName.equals(remove)) {
                Building removeBuilding = new Building(1, 1, "remove");
                ImageView imageView = imageViewWithLayout(removeBuilding);
                container.getChildren().add(imageView);
            }
            scroll.setContent(container);
            tabContents.set(tabNames.indexOf(tabName), scroll);
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

        // Wenn sich die Koordinaten innerhalb der Map befinden
        if(isoCoord != null) {
            int xCoord = (int) isoCoord.getX();
            int yCoord = (int) isoCoord.getY();

            if (xCoord < 0 || yCoord < 0) {
                // Tu erstmal nichts
                return isoCoord;
            }
            // Prüfe ob Building platziert werden darf und zeichne Vorschau falls ja
            if (controller.canPlaceBuildingAtPlaceInMapGrid(xCoord, yCoord, selectedBuilding)) {
                String imageName = mapping.getImageNameForObjectName(selectedBuilding.getBuildingName());
                // Wenn das Building mehr als ein Tile einnimt
                if (selectedBuilding.getWidth() > 1 || selectedBuilding.getDepth() > 1) {
                    Tile tile = controller.getTileOfMapTileGrid(xCoord, yCoord);
                    tile.setBuildingOrigin(true);
                    view.drawBuildingOverMoreTiles(tile, selectedBuilding, xCoord, yCoord);
                    tile.setBuildingOrigin(false);
                    // Wenn das Building auf ein Tile passt
                } else {
                    double ratio = view.getImageNameToImageRatio().get(imageName);
                    double tileWidth = view.getTileImageWidth();
                    Image image = view.getResourceForImageName(imageName, tileWidth, tileWidth * ratio);
                    view.drawTileImage(xCoord, yCoord, image, transparent);
                }
                return isoCoord;
                // Wenn das Building nicht platziert werden darf, dann gib null zurück
            } else return null;
        } return null;
    }

    /**
     * Zeige Inhalte des Tabs für TrafficPart
     * @param part
     */
    public void showTrafficPart(ConnectedTrafficPart part){
        trafficPartTabContent.getChildren().clear();
        String trafficType = resourceBundle.getString("trafficType");
        String type = resourceBundle.getString(part.getTrafficType().name());
        Label typeLabel = new Label(trafficType + " \n" + type);
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
        } else {
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
                        showTrafficLineDialog(part.getTrafficType());
                    }
                };
        newTrafficLine.setOnAction(event);
        trafficPartTabContent.getChildren().addAll(typeLabel, box, newTrafficLine);
        int index = tabContents.indexOf(trafficPartTabContent);
        tabPane.getSelectionModel().select(index);
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
                else {
                    Building building = controller.getBuildingForMouseEvent(event);
                    if(building instanceof Factory){
                        showFactoryInformation(building);
                    }
                    else controller.showTrafficPartInView(building);
                }
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

    /**
     * Zeigt Inhalte des Factory-Tabs mit Informationen über die angeklickte Fabrik
     * @param building
     */
    private void showFactoryInformation(Building building){
        Factory factory = (Factory) building;

        String imageName = mapping.getImageNameForObjectName(building.getBuildingName());
        Image image = view.getResourceForImageName(imageName);
        factoryImageView.setImage(image);

        List<Label> labels = new ArrayList<>();

        labels.addAll(List.of(factoryNameLabel, productionLabel, factoryNearStationsLabel, factoryMaximaStorageLabel,
                factoryRealStorageLabel, consumptionLabel));

        // Füge Name der Fabrik zum Label hinzu
        String factoryName = resourceBundle.getString(factory.getBuildingName());
        factoryNameLabel.setText(resourceBundle.getString("factory name") + factoryName);

        // Füge Produzierte Waren zum Label hinzu
        StringBuilder production = new StringBuilder();
        for(Map.Entry<String, Integer> entry : factory.getProductionSteps().get(0).getProduce().entrySet()){
            String producedGood = resourceBundle.getString(entry.getKey());
            production.append(producedGood).append(" (").append(entry.getValue()).append("); ");
        }
        if(production.toString().equals("")) {
            production = new StringBuilder(resourceBundle.getString("nothing"));
        }
        productionLabel.setText(resourceBundle.getString("production") + production);

        // Füge komsumierte Waren zum Label hinzu
        StringBuilder consumption = new StringBuilder();
        for(Map.Entry<String, Integer> entry : factory.getProductionSteps().get(0).getConsume().entrySet()){
            String consumedGood = resourceBundle.getString(entry.getKey());
            consumption.append(consumedGood).append(" (").append(entry.getValue()).append("); ").append("  ");
        }
        if(consumption.toString().equals("")) {
            consumption = new StringBuilder(resourceBundle.getString("nothing"));
        }
        consumptionLabel.setText(resourceBundle.getString("consumption") + consumption);


        if(factory.getStorage() != null){
            // Füge tatsächlichen Warenbestand zum Label hinzu
            StringBuilder realStored = new StringBuilder();
            for(Map.Entry<String, Integer> entry : factory.getStorage().getCargo().entrySet()){
                String storedGood = resourceBundle.getString(entry.getKey());
                realStored.append(storedGood).append(" (").append(entry.getValue()).append("); ");
            }
            factoryRealStorageLabel.setText(resourceBundle.getString("stored cargo") + realStored);

            // Füge max. Lagerkapazität zu Label hinzu
            StringBuilder maxStored = new StringBuilder();
            for(Map.Entry<String, Integer> entry : factory.getStorage().getMaxima().entrySet()){
                String maxStorageForGood = resourceBundle.getString(entry.getKey());
                maxStored.append(maxStorageForGood).append(" (").append(entry.getValue()).append("); ");
            }
            factoryMaximaStorageLabel.setText(resourceBundle.getString("maximum cargo capacity") + maxStored);

            // Prüfe ob Labelinhalt zu lang und verkleinere ggf. die Schrift
            boolean labelTooLong = false;
            for(Label label: labels){
                if(label.getText().length() > 100){
                    labelTooLong = true;
                }
            }
                for(Label label: labels){
                    if(labelTooLong){
                        label.setFont(new Font("Arial", 10));
                    }
                    else {
                        label.setFont(new Font("Arial", 15));

                    }
                }

        } else {
            // Wenn eine Fabrik kein Storage hat, sind die entsprechenden Labels leer
            factoryRealStorageLabel.setText("");
            factoryMaximaStorageLabel.setText("");
        }

        // Füge IDs der nahegelegenen Stationen zu Label hinzu
        StringBuilder nearStations = new StringBuilder();
        for(Station station : factory.getNearStations()){
            nearStations.append(station.getId()).append("; ");
        }
        factoryNearStationsLabel.setText(resourceBundle.getString("near station-IDs") + nearStations);

        int index = tabContents.indexOf(factoryTabContent);
        tabPane.getSelectionModel().select(index);

    }

    /**
     * Zeigt TrafficLinePopup an
     * @param trafficType
     */
    private void showTrafficLineDialog(TrafficType trafficType){
        selectTrafficLineStationsMode = true;
        trafficLinePopup = new TrafficLinePopup(view, trafficType);
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

    public void setSelectTrafficLineStationsMode(boolean selectTrafficLineStationsMode) {
        this.selectTrafficLineStationsMode = selectTrafficLineStationsMode;
    }
}
