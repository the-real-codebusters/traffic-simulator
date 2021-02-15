package view;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TrafficLineCreationDialog {

    public TrafficLineCreationDialog(){

        VBox listBox = new VBox();

        Label message = new Label("Create a new Traffic Line");
        listBox.getChildren().add(message);


        //Name eingeben
        TextField textField = new TextField("name of Traffic Line");
        listBox.getChildren().add(textField);

        Scene scene = new Scene(listBox);

        Stage window = new Stage();
        window.setScene(scene);

        window.setTitle("New Traffic Line");

        window.show();
    }



}
