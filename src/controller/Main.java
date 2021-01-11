package controller;

import model.JSONParser;
import model.Model;
import view.View;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    public void start(Stage stage) throws Exception {
        JSONParser parser = new JSONParser();
        Model model = new Model();
        if (parser.parse("resources/planverkehr.json", model)) {
            View view = new View(stage, model);
            stage.setTitle("Green tiles");
            stage.show();
            Controller controller = new Controller(model, view);
        }
    }

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }

}
