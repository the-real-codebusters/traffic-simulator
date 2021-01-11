package controller;

import view.View;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    public void start(Stage stage) throws Exception {
        View view = new View(stage);
        stage.setTitle("Green tiles");
        stage.show();
        Controller controller = new Controller(view);
    }

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }

}
