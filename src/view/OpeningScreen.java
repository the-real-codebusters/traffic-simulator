package view;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
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

    private final File mediaFile = new File("resources/Codebusters.mp4");
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
        resourceBundle = ResourceBundle.getBundle("Bundle", locale);

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


        VBox vbox = new VBox(20);

        final String MEDIA_URL = mediaFile.toURI().toString();
        Media media = new Media(MEDIA_URL);
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        MediaView mediaView = new MediaView(mediaPlayer);
        mediaView.setFitWidth(1920/2);
        mediaView.setFitHeight(1080/2);
        mediaPlayer.setAutoPlay(true);

//        Pane filePane = new Pane();
        openButton.setPrefSize(200, 50);
//        openButton.setAlignment(Pos.CENTER);
        vbox.setAlignment(Pos.CENTER);
//        filePane.getChildren().add(openButton);
//        filePane.setLayoutX(270);
//        filePane.setLayoutY(mediaView.getFitHeight());

//        languages.setLayoutX(325);
//        languages.setLayoutY((600+filePane.getHeight()));
//        filePane.getChildren().add(languages);


        vbox.getChildren().addAll(mediaView, openButton, languages);

//        Group root = new Group();
//        root.getChildren().add(vbox);
        Scene scene = new Scene(vbox);
//        root.getChildren().add(filePane);
//        root.getChildren().add(languages);
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
