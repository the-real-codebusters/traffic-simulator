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
        BasicModel model = new BasicModel();
        JSONParser parser = new JSONParser();
        if (parser.parse("resources/planverkehr/planverkehr.json", model)) {
            model.printModelAttributes();
            View view = new View(stage, model);
            stage.setTitle("Planverkehr");
            stage.show();
            Controller controller = new Controller(view, model.getMap());
        }
    }

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }

}
