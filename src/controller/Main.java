package controller;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
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

    View view;

    public void start(Stage stage) throws Exception {

        OpeningScreen opening = new OpeningScreen(stage);

        Button openButton = opening.getOpenButton();
        openButton.setOnAction(e -> {
            File file = new FileChooser().showOpenDialog(stage);
            if(file!= null){
                String pathToFile = file.getPath();

                if(pathToFile.contains("VitaExMachina")){
                    System.out.println("VitaExMachina contained");
                    showVitaExMachinaAnimation(stage);
                    startGame(pathToFile, stage, opening);
                }
                else {
                    startGame(pathToFile, stage, opening);
                    stage.setScene(view.getScene());
                }


            }

        });

        stage.show();
    }

    private void showVitaExMachinaAnimation(Stage stage){
        File mediaFile = new File("resources/Codebusters.mp4");
        System.out.println("media file line ready");
        final String MEDIA_URL = mediaFile.toURI().toString();
        Media media = new Media(MEDIA_URL);
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        System.out.println("media player line ready");

        MediaView mediaView = new MediaView(mediaPlayer);
        System.out.println("media view line ready");

        mediaView.setFitWidth(1920/2);
        mediaView.setFitHeight(1080/2);
        mediaPlayer.setAutoPlay(true);
        Scene scene = new Scene(new AnchorPane(mediaView));
        System.out.println("scene line ready");

        stage.setScene(scene);
        System.out.println("scene set line ready");

        mediaPlayer.setOnEndOfMedia(() -> {
            System.out.println("on end reached");
            stage.setScene(view.getScene());
        });
    }

    private void startGame(String pathToFile, Stage stage, OpeningScreen opening){
        System.out.println("Program started");
        JSONParser parser = new JSONParser();
        BasicModel model = new BasicModel();
        boolean result = parser.parse(pathToFile, model, opening.getResourceBundle());
        if (result) {
            view = new View(stage, model);
            Controller controller = new Controller(view, model, opening.getResourceBundle());
            String title = pathToFile.substring(pathToFile.lastIndexOf('\\') + 1, pathToFile.length()-5);
            stage.setTitle(title);
        }
    }




//        System.out.println("Program started");
//        JSONParser parser = new JSONParser();
//        BasicModel model = new BasicModel();
////        Locale locale = new Locale("en", "US");
//        Locale locale = new Locale("de", "DE");
//        ResourceBundle resourceBundle = ResourceBundle.getBundle("Bundle", locale);
//
////        if (parser.parse("resources/planverkehr/planverkehr.json", model, resourceBundle)) {
//   if (parser.parse("resources/VitaExMachina/VitaExMachina.json", model, resourceBundle)) {
//
//
////            model.printModelAttributes();
//            View view = new View(stage, model);
//            stage.setTitle("Planverkehr");
//            Controller controller = new Controller(view, model, resourceBundle);
//
////            stage.setMaximized(true);
//            stage.show();
//        }
//    }

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }

}
