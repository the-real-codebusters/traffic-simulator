package view;

import javafx.collections.FXCollections;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

import java.io.File;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

// Sorgt für die Anzeige einer Animation beim Öffnen des Programms und enthält einen Button
public class OpeningScreen {

    private final File mediaFile = new File("resources/IMG_7659.mp4");
    final Button openButton = new Button("Choose the json file");
    private Locale locale;


    private ResourceBundle resourceBundle = new ResourceBundle() {
        @Override
        protected Object handleGetObject(String key) {
            return null;
        }

        @Override
        public Enumeration<String> getKeys() {
            return null;
        }
    };



    public OpeningScreen(Stage stage) {
        openButton.setStyle("-fx-font-size: 15px");

        ChoiceBox<String> languages = new ChoiceBox<>(
                FXCollections.observableArrayList("English", "German")
        );
        // wenn Auswahl nicht verändert wird, ist Spiel auf English
        languages.getSelectionModel().select(0);
        languages.setStyle("-fx-font-size: 15px");

        // Locale ist per Default auf english
        locale = new Locale("en", "US");

        // Prüft ob eine Sprachauswahl getroffen wird und erstellt entsprechend eine Locale und ein ResourceBundle
        languages.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> {

                    String language = languages.getSelectionModel().getSelectedItem();

                    if (language.equals("English")){
                        locale = new Locale("en", "US");
                    } else if (language.equals("German")){
                        locale = new Locale("de", "DE");
                    }

                    resourceBundle = ResourceBundle.getBundle("Bundle", locale);
                    String openButtonText = resourceBundle.getString("openButtonText");
                    openButton.setText(openButtonText);

                });




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

        languages.setLayoutX(325);
        languages.setLayoutY(650);
        filePane.getChildren().add(languages);

        Group root = new Group();
        root.getChildren().add(mediaView);
        Scene scene = new Scene(root);
        root.getChildren().add(filePane);
        root.getChildren().add(languages);
        stage.setScene(scene);
    }


    public Button getOpenButton() {
        return openButton;
    }

    public Locale getLocale() {
        return locale;
    }

    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }
}
