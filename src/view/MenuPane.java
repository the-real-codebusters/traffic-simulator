package view;

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

    public MenuPane(BasicModel model, View view, Canvas canvas) {
        this.model = model;
        this.view = view;
        this.canvas = canvas;

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
            List<String> buildings = model.getBuildingNamesForBuildmenu(name);
            for(String building: buildings){
                ImageView imageView = imageViewWithLayout(building);
                container.getChildren().add(imageView);
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
        Image image = view.getResourceForImageName(imageName, false);
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
    }

    private void drawHoveredImageBefore(int xCoordBefore, int yCoordBefore, Field[][] fields){
        Image hoveredImageBefore = view.getSingleFieldImage(xCoordBefore, yCoordBefore, fields);
        view.drawTileImage(xCoordBefore, yCoordBefore, hoveredImageBefore);
    }

    private void setCanvasEvents(){
        canvas.setOnMouseMoved(event -> {
            if(selectedBuilding != null){

                if(hoveredTileBefore != null){
                    removeDrawedImagesBecauseOfHover();
                }

                double mouseX = event.getX();
                double mouseY = event.getY();
                Point2D isoCoord = view.findTileCoord(mouseX, mouseY, view.getCanvasCenterWidth(), view.getCanvasCenterHeight());
                int xCoord = (int) isoCoord.getX();
                int yCoord = (int) isoCoord.getY();
                Image image = view.getResourceForImageName(selectedBuilding, true);
                view.drawTileImage(xCoord, yCoord, image);
                hoveredTileBefore = isoCoord;
            }
        });
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED,
                event -> {
                    if(event.getButton().compareTo(MouseButton.SECONDARY) == 0) {
                        selectedBuilding = null;
                        removeDrawedImagesBecauseOfHover();
                    }
                });
    }

}
