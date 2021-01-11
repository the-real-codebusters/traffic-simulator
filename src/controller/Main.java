package controller;

import model.Model;
import view.View;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    public void start(Stage stage) throws Exception {
        View view = new View(stage);
        Controller controller = new Controller(view);
        stage.setTitle("Green tiles");
        stage.show();
        Model model = new Model();
        Controller controller = new Controller(model, view);
    }

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }

}
