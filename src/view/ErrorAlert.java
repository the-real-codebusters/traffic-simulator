package view;

import javafx.scene.control.Alert;

/**
 * Fehlermeldung wird ausgegeben
 */
public class ErrorAlert {

      /**
     * Fehlermeldung als Popup anzeigen
     * @param errorMessage
     */
    public static void showAlert(String errorMessage) {
        //TODO: Dialog modifizieren
        Alert alert = new Alert(Alert.AlertType.ERROR, errorMessage);
        alert.show();
    }
}
