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
import java.util.Locale;
import java.util.ResourceBundle;

public class Main extends Application {

    public void start(Stage stage) throws Exception {

//        OpeningScreen opening = new OpeningScreen(stage);
//
//        Button openButton = opening.getOpenButton();
//        openButton.setOnAction(e -> {
//            File file = new FileChooser().showOpenDialog(stage);
//            String pathToFile = file.getPath();
//
//            System.out.println("Program started");
//            JSONParser parser = new JSONParser();
//            BasicModel model = new BasicModel();
//            boolean result = parser.parse(pathToFile, model, opening.getResourceBundle());
//            if (result) {
//                View view = new View(stage, model);
//                Controller controller = new Controller(view, model, opening);
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
//        Locale locale = new Locale("en", "US");
        Locale locale = new Locale("de", "DE");
        ResourceBundle resourceBundle = ResourceBundle.getBundle("Bundle", locale);


        if (parser.parse("resources/planverkehr/planverkehr.json", model, resourceBundle)) {
//            if (parser.parse("resources/VitaExMachina/VitaExMachina.json", model)) {


//            model.printModelAttributes();
            View view = new View(stage, model);
            stage.setTitle("Planverkehr");
            Controller controller = new Controller(view, model, resourceBundle);

//            stage.setMaximized(true);
            stage.show();
        }
    }

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }

}
