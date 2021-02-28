package controller;

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

public class Main extends Application {

    private View view;
    private BasicModel model;
    private boolean parserSuccessful = false;
    private String pathToJSONFile;

    private OpeningScreen openingScreen;

    public void start(Stage stage) throws Exception {

        openingScreen = new OpeningScreen(stage);

        Button openButton = openingScreen.getOpenButton();
        openButton.setOnAction(e -> {
            File file = new FileChooser().showOpenDialog(stage);
            if(file!= null){
                pathToJSONFile = file.getPath();

                //Wenn VitaExMachina im Namen vorkommt, zeige ein kurzes Einleitungsvideo des Szenarios
                if(pathToJSONFile.contains("VitaExMachina")){
                    showVitaExMachinaAnimation(stage);
                    startGame(pathToJSONFile, stage, openingScreen);
                }
                else {
                    parseAndCreateModel();
                    startGame(pathToJSONFile, stage, openingScreen);
                    stage.setScene(view.getScene());
                }
            }
        });
        stage.show();
    }

    private void showVitaExMachinaAnimation(Stage stage){
        File mediaFile = new File("resources/VitaExMachina/introduction.mp4");
        final String MEDIA_URL = mediaFile.toURI().toString();
        Media media = new Media(MEDIA_URL);
        MediaPlayer mediaPlayer = new MediaPlayer(media);

        MediaView mediaView = new MediaView(mediaPlayer);

        mediaView.setFitWidth(720);
        mediaView.setFitHeight(480);
        mediaPlayer.setAutoPlay(true);
        Scene scene = new Scene(new AnchorPane(mediaView));

        stage.setScene(scene);

        //Zeige eigentliches Programm nach Video
        mediaPlayer.setOnEndOfMedia(() -> {
            stage.setScene(view.getScene());
        });
        parseAndCreateModel();
    }

    private void parseAndCreateModel(){
        JSONParser parser = new JSONParser();
        model = new BasicModel();
        parserSuccessful = parser.parse(pathToJSONFile, model, openingScreen.getResourceBundle());
        model.generateMap();
    }

    private void startGame(String pathToFile, Stage stage, OpeningScreen opening){

        if (parserSuccessful) {
            view = new View(stage, model);
            new Controller(view, model, opening.getResourceBundle());
            //Zeige name der JSON-Datei als Titel
            String title = pathToFile.substring(pathToFile.lastIndexOf('\\') + 1, pathToFile.length()-5);
            stage.setTitle(title);
        }
    }

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }
}
