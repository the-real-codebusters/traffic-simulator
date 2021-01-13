package view;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
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

    public MenuPane(BasicModel model) {
        this.model = model;
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

        // Hbox erstellen
        // Vom Model alle Buildings dazu geben lassen
        // Hbox mit Graphiken zu buildings befüllen

        //nature
        HBox natureContainer = boxWithLayout();
        ImageView tree = imageViewWithLayout("tree");
        natureContainer.getChildren().add(tree);
        tabContents.set(tabNames.indexOf("nature"), natureContainer);
    }

    private HBox boxWithLayout(){
        HBox box = new HBox(10);
        box.setPrefHeight(100);
        box.setPadding(new Insets(5,20,5,20));
        return box;
    }

    private ImageView imageViewWithLayout(String imageName){
        Image image = new Image(getClass().getResource("/"+imageName+".png").toString());
        ImageView view = new ImageView(image);
        view.setPreserveRatio(true);
        view.setFitHeight(90);
        view.setOnMouseClicked(event -> {
            // TODO
            System.out.println("Auf "+imageName+" geklickt");
        });
        return view;
    }

    private VBox buildingContent(String typ){
        //TODO Befülle nach konkret vorhandenen baubaren Bauwerken für jeweiligen Typ

        VBox vBox = new VBox(10);
        HBox hBox = new HBox(10);
        if(typ.equals("road")){
            ImageView street = imageViewWithLayout("street");
            hBox.getChildren().add(street);
        }
        else if(typ.equals("rail")){
            ImageView street = imageViewWithLayout("railroad");
            hBox.getChildren().add(street);
        }
        else if(typ.equals("airport")){
            ImageView street = imageViewWithLayout("tower");
            hBox.getChildren().add(street);
        }
        vBox.getChildren().addAll(new Text(typ), hBox);
        return vBox;
    }
}
