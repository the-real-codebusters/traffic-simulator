package controller;

import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import model.BasicModel;
import model.JSONParser;
import view.OpeningScreen;
import view.View;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {

    public void start(Stage stage) throws Exception {

//        OpeningScreen opening = new OpeningScreen(stage);
//        Button openButton = opening.getOpenButton();
//        openButton.setOnAction(e -> {
//            File file = new FileChooser().showOpenDialog(stage);
//            String pathToFile = file.getPath();
//
//            System.out.println("Program started");
//            JSONParser parser = new JSONParser();
//            BasicModel model = new BasicModel();
//            boolean result = parser.parse(pathToFile, model);
//            if (result) {
//                View view = new View(stage, model);
//                Controller controller = new Controller(view, model);
//            }
//            String title = pathToFile.substring(pathToFile.lastIndexOf('\\') + 1, pathToFile.length()-5);
//            stage.setTitle(title);
//        });
//
//        stage.show();
//    }


        System.out.println("Program started");
        JSONParser parser = new JSONParser();
        BasicModel model = new BasicModel();

        if (parser.parse("resources/planverkehr/planverkehr.json", model)) {
//            model.printModelAttributes();
            View view = new View(stage, model);
            stage.setTitle("Vita Ex Machina");
            Controller controller = new Controller(view, model);

//            stage.setMaximized(true);
            stage.show();
        }
    }

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }

}
