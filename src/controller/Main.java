package controller;

import model.BasicModel;
import model.JSONParser;
import view.View;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    public void start(Stage stage) throws Exception {
        JSONParser parser = new JSONParser();
        BasicModel model = new BasicModel();
        if (parser.parse("resources/planverkehr/planverkehr.json", model)) {
            View view = new View(stage, model);
            stage.setTitle("Green tiles");
            stage.show();
            Controller controller = new Controller(view);
        }
    }

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }

}
