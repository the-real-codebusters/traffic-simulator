package view;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import model.BasicModel;

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

    // Wenn null, kein Bauwerk ist ausgewählt
    private String selectedBuilding;

    // x, y
    private Point2D hoveredTileBefore;
    private Image hoveredImageBefore;

    public MenuPane(BasicModel model, View view, Canvas canvas) {
        this.model = model;
        this.view = view;
        this.canvas = canvas;

        canvas.setOnMouseMoved(event -> {

            // TODO Benutze echte Buildings aus Model


            if(selectedBuilding != null){

                if(hoveredImageBefore != null){
                    view.drawTileImage((int) hoveredTileBefore.getX(), (int) hoveredTileBefore.getY(), hoveredImageBefore);
                }

                double mouseX = event.getX();
                double mouseY = event.getY();
                Point2D isoCoord = view.findTileCoord(mouseX, mouseY, view.getCanvasCenterWidth(), view.getCanvasCenterHeight());
                Image image = view.getResourceForImageName(selectedBuilding, true);
                view.drawTileImage((int) isoCoord.getX(), (int) isoCoord.getY(), image);

                hoveredImageBefore = image;
                hoveredTileBefore = isoCoord;
            }


        });

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

}
