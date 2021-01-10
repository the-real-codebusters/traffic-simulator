package view;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;

public class MenuPane extends AnchorPane {

    private List<Node> tabContents = new ArrayList();
    private List<String> tabNames = List.of("buildings", "nature", "height", "vehicles");
    private HBox hBox;
    private TabPane tabPane = new TabPane();

    public MenuPane() {
        hBox = new HBox(tabPane, new ImageView(new Image(getClass().getResource("/greentile.png").toString())));
        this.getChildren().add(hBox);

        generateTabContents();

        for (int i=0; i<tabNames.size(); i++){
            addTab(tabNames.get(i), tabContents.get(i));
        }

        Tab tab = new Tab();
        tab.setText("teest");
        tab.setContent(new Button("test"));
        tabPane.getTabs().add(tab);
    }

    private void addTab(String name, Node content){
        Tab tab = new Tab();
        tab.setText(name);
        tab.setContent(content);
        tabPane.getTabs().add(tab);
    }

    private void generateTabContents(){

        // dummys:
        for (int i=0; i<tabNames.size(); i++){
            tabContents.add(new ProgressBar());
        }

        tabContents.set(0, new ImageView(new Image(getClass().getResource("/greentile.png").toString())));
    }
}
