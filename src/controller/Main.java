package controller;

import model.JSONParser;
import view.View;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    public void start(Stage stage) throws Exception {
        JSONParser parser = new JSONParser();
        if (parser.parse("resources/planverkehr.json")) {
            parser.getBuildingsAsString();
            View view = new View(stage);
            stage.setTitle("Green tiles");
            stage.show();
            Controller controller = new Controller(view);
        }
    }

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }

}
