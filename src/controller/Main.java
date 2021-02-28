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

    View view;
    private JSONParser parser;
    private BasicModel model;
    private boolean result = false;
    private String pathToFile;

    private OpeningScreen opening;

    public void start(Stage stage) throws Exception {

        opening = new OpeningScreen(stage);

        Button openButton = opening.getOpenButton();
        openButton.setOnAction(e -> {
            File file = new FileChooser().showOpenDialog(stage);
            if(file!= null){
                pathToFile = file.getPath();

                if(pathToFile.contains("VitaExMachina")){
                    showVitaExMachinaAnimation(stage);
                    startGame(pathToFile, stage, opening);
                }
                else {
                    setModel();
                    startGame(pathToFile, stage, opening);
                    stage.setScene(view.getScene());
                }


            }
        });
        stage.show();
    }

    private void showVitaExMachinaAnimation(Stage stage){
        File mediaFile = new File("resources/fin.mp4");
        final String MEDIA_URL = mediaFile.toURI().toString();
        Media media = new Media(MEDIA_URL);
        MediaPlayer mediaPlayer = new MediaPlayer(media);

        MediaView mediaView = new MediaView(mediaPlayer);

        mediaView.setFitWidth(1920/2);
        mediaView.setFitHeight(1080/2);
        mediaPlayer.setAutoPlay(true);
        Scene scene = new Scene(new AnchorPane(mediaView));

        stage.setScene(scene);


        mediaPlayer.setOnEndOfMedia(() -> {
            stage.setScene(view.getScene());
        });

        setModel();
    }

    private void setModel(){
        parser = new JSONParser();
        model = new BasicModel();
        result = parser.parse(pathToFile, model, opening.getResourceBundle());
        model.generateMap();
    }

    private void startGame(String pathToFile, Stage stage, OpeningScreen opening){
        System.out.println("Program started");

        if (result) {
            view = new View(stage, model);
            Controller controller = new Controller(view, model, opening.getResourceBundle());
            String title = pathToFile.substring(pathToFile.lastIndexOf('\\') + 1, pathToFile.length()-5);
            stage.setTitle(title);
        }
    }

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }

}
