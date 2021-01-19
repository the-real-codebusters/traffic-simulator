package controller;

import model.BasicModel;
import model.JSONParser;
import model.MapModel;
import view.View;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashSet;

public class Main extends Application {

    public void start(Stage stage) throws Exception {
        JSONParser parser = new JSONParser();

        MapModel map = new MapModel( 30, 40);
        BasicModel model = new BasicModel(new HashSet<>(), 0, 1.0, map, "planverkehr",
                null, new ArrayList<>());

        if (parser.parse("resources/planverkehr/planverkehr.json", model)) {
            model.printModelAttributes();
            View view = new View(stage, model);
            stage.setTitle("Planverkehr");
            stage.show();
            Controller controller = new Controller(view, map, model);
        }
    }

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }

}
