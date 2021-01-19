package view;

import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
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
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import model.BasicModel;
import model.Building;
import model.Field;

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
    private String selectedBuilding;

    private Point2D hoveredTileBefore;
    BuildingToImageMapping mapping;

    public MenuPane(BasicModel model, View view, Canvas canvas, BuildingToImageMapping mapping) {
        this.model = model;
        this.view = view;
        this.canvas = canvas;
        this.mapping = mapping;

        setCanvasEvents();

        hBox = new HBox(tabPane);
        this.getChildren().add(hBox);

        generateTabContents();

        for (int i=0; i<tabNames.size(); i++){
            addTab(tabNames.get(i), tabContents.get(i));
        }
    }

    private void addTab(String name, Node content){
        Tab tab = new Tab();
        tab.setText(name);
        tab.setContent(content);
        tabPane.getTabs().add(tab);
    }

    private void generateTabContents(){

        // Get Buildmenus from Model
        Set<String> buildmenus = model.getBuildmenus();

        tabNames.addAll(buildmenus);
        tabNames.addAll(List.of("height", "vehicles"));

        // dummys:
        for (int i=0; i<tabNames.size(); i++){
            tabContents.add(new AnchorPane());
        }

        for(String name: tabNames){
            HBox container = boxWithLayout();
            List<Building> buildings = model.getBuildingsForBuildmenu(name);
            if(name.equals("road")) System.out.println("test "+buildings.size());
            for(Building building: buildings){
                String imageName = mapping.getImageNameForBuildingName(building.getBuildingName());
                if(imageName == null) continue;
                ImageView imageView = imageViewWithLayout(imageName);
                container.getChildren().add(imageView);
                //TODO
            }
            tabContents.set(tabNames.indexOf(name), container);
        }
    }

    private HBox boxWithLayout(){
        HBox box = new HBox(10);
        box.setPrefHeight(100);
        box.setPadding(new Insets(5,20,5,20));
        return box;
    }

    private ImageView imageViewWithLayout(String imageName){
        Image image = view.getResourceForImageName(imageName);
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(90);
        imageView.setOnMouseClicked(event -> {
            // TODO
            selectedBuilding = imageName;
            System.out.println("Auf "+imageName+" geklickt");
        });
        return imageView;
    }

    private void removeDrawedImagesBecauseOfHover(){
        int xCoordBefore = (int) hoveredTileBefore.getX();
        int yCoordBefore = (int) hoveredTileBefore.getY();
        Field[][] fields = model.getFieldGridOfMap();
        drawHoveredImageBefore(xCoordBefore, yCoordBefore, fields);
        drawHoveredImageBefore(xCoordBefore+1, yCoordBefore, fields);
        drawHoveredImageBefore(xCoordBefore, yCoordBefore+1, fields);
        drawHoveredImageBefore(xCoordBefore+1, yCoordBefore+1, fields);

        // Bei Bildern, die über das Feld hinausschauen, müssen auch angrenzende Felder neu gezeichnet werden
    }

    private void drawHoveredImageBefore(int xCoordBefore, int yCoordBefore, Field[][] fields){
        Image hoveredImageBefore = view.getSingleFieldImage(yCoordBefore, xCoordBefore, fields);
        view.drawTileImage(yCoordBefore, xCoordBefore, hoveredImageBefore, false);
    }

    /**
     *
     * @param mouseEvent
     * @param transparent
     * @return Gibt die Koordinaten des Tiles zurück, auf das gezeichnet wurde
     */
    private Point2D drawHoveredImage(MouseEvent mouseEvent, boolean transparent){
        double mouseX = mouseEvent.getX();
        double mouseY = mouseEvent.getY();
        Point2D isoCoord = view.findTileCoord(mouseX, mouseY, view.getCanvasCenterWidth(), view.getCanvasCenterHeight());
        int xCoord = (int) isoCoord.getX();
        int yCoord = (int) isoCoord.getY();
        Image image = view.getResourceForImageName(selectedBuilding, view.getTileWidth(), view.getTileHeight());
        view.drawTileImage(yCoord, xCoord, image, transparent);
        return isoCoord;
    }

    private void setCanvasEvents(){
        canvas.setOnMouseMoved(event -> {
            if(selectedBuilding != null){

                if(hoveredTileBefore != null){
                    removeDrawedImagesBecauseOfHover();
                }

                hoveredTileBefore = drawHoveredImage(event, true);
            }
        });
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED,
                event -> {
                    if(event.getButton().compareTo(MouseButton.SECONDARY) == 0) {
                        selectedBuilding = null;
                        removeDrawedImagesBecauseOfHover();
                    }
                    else if(
                            event.getButton().compareTo(MouseButton.PRIMARY) == 0 &&
                            selectedBuilding != null)
                    {
                        drawHoveredImage(event, false);
                        selectedBuilding = null;

                        // TODO Speichere platziertes Bauwerk im Model und rufe drawMap auf statt drawHoveredImage
                    }
                });
    }

}
