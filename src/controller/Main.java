package controller;

import model.BasicModel;
import model.JSONParser;
import model.MapModel;
import view.View;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.ArrayList;

public class Main extends Application {

    public void start(Stage stage) throws Exception {
        JSONParser parser = new JSONParser();

        MapModel map = new MapModel( 300, 300);
        BasicModel model = new BasicModel(null, 0, 1.0, map, "planverkehr",
                null, new ArrayList<>());

        if (parser.parse("resources/planverkehr/planverkehr.json", model)) {
            View view = new View(stage, model);
            stage.setTitle("Green tiles");
            stage.show();
            Controller controller = new Controller(view, map, model);
        }
    }

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }

}
