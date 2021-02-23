package view;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

import java.io.File;

// Sorgt für die Anzeige einer Animation beim Öffnen des Programms und enthält einen Button
public class OpeningScreen {

    private final File mediaFile = new File("resources/IMG_7659.mp4");
    final Button openButton = new Button("Choose the json file");


    public OpeningScreen(Stage stage) {
        final String MEDIA_URL = mediaFile.toURI().toString();
        Media media = new Media(MEDIA_URL);
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        MediaView mediaView = new MediaView(mediaPlayer);
        mediaView.setFitHeight(750);
        mediaView.setFitWidth(725);
        mediaPlayer.setAutoPlay(true);

        Pane filePane = new Pane();
        openButton.setPrefSize(200, 50);
        filePane.getChildren().add(openButton);
        filePane.setLayoutX(270);
        filePane.setLayoutY(470);

        Group root = new Group();
        root.getChildren().add(mediaView);
        Scene scene = new Scene(root);
        root.getChildren().add(filePane);
        stage.setScene(scene);
    }


    public Button getOpenButton() {
        return openButton;
    }
}
