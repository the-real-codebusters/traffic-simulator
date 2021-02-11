package view;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;


public class PopupDesiredVehicles extends Application {

    private int result;

    public void start(Stage primaryStage) throws Exception {
//        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        final TextField textField = new TextField();
        final Text text = new Text("Please set the desired number of vehicles");
        text.setLayoutY(20);
        textField.setDisable(false);
        textField.setLayoutY(50);
        final Button submitButton = new Button("Submit");
        submitButton.setLayoutY(100);
        submitButton.setDefaultButton(true);
        submitButton.setOnAction(e -> {
            primaryStage.close();
            setResult(textField.getText());
        });
        Group group = new Group();
        group.getChildren().add(text);
        group.getChildren().add(textField);
        group.getChildren().add(submitButton);
        Scene scene = new Scene(group);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setResult(String r) {
        this.result = Integer.parseInt(r);
        System.out.print(result + " vehicles");
    }

    public int getResult() {
        return result;
    }
}
