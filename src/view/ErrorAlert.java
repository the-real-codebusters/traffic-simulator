package view;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;

import java.util.ResourceBundle;

/**
 * Fehlermeldung wird ausgegeben
 */
public class ErrorAlert {

      /**
     * Fehlermeldung als Popup anzeigen
     * @param errorMessage
     */
    public static void showAlert(String errorMessage, ResourceBundle resourceBundle) {
        Alert alert = new Alert(Alert.AlertType.ERROR,errorMessage, ButtonType.OK);
        alert.setTitle(resourceBundle.getString("error"));
        alert.setHeaderText(resourceBundle.getString("anErrorOccurred"));
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }
}
