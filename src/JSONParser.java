import javafx.scene.control.Alert;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * Parst JSON-Datei, wirft eine Exception im Fehlerfall
 * Befüllt Flughafen-Objekt
 */
public class JSONParser {

    private JSONObject json;
    private String[] requiredRootAttributes = {"commodities", "buildings", "vehicles", "map"};

    /**
          * Zeigt eine Fehlermeldung an, wenn JSON-Datei fehlerhaft
     * @param filename - JSON-Dateiname zum Laden
     * @return - true im Erfolgsfall, sonst false
     */
    public boolean parse(String filename) {
        // JSON-Datei wird eingelesen
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            showAlert("File " + filename + " not found");
            return false;
        }
        try {
            json = new JSONObject(new JSONTokener(inputStream));
            // Pflichtattribute alle da?
            handleRootAttributes();
            handleCommoditiesContent(json.getJSONArray("commodities"));
            handleBuildingsContent(json.getJSONArray("buildings"));
            handleVehiclesContent(json.getJSONArray("vehicles"));
            handleMapContent(json.getJSONArray("map"));


            //
            //handleContentAsStringList(json.getJSONArray("commodities"));

        }
        catch(JSONException e){
                showAlert("File " + filename + " has no json format");
                return false;
            }
        catch(JSONParserException e){
                showAlert(e.getMessage());
                return false;
            }
            return true;
    }

    /**
     * Prüft ob String-Wert valide ist
     * @param name
     * @throws JSONParserException
     */
    private void handleContentAsString(String name) throws JSONParserException{
        try {
            String value = json.getString(name);
        }
        catch(JSONException e) {
            throw new JSONParserException ("no string format defined");
        }
    }

    /**
     * Prüft ob Integer-Wert valide ist
     * @param name
     * @throws JSONParserException
     */
    private void handleContentAsInteger(String name, Integer lower, Integer upper) throws JSONParserException{
        try {
            int value = json.getInt(name);
            if (lower != null && value < lower) {
                throw new JSONParserException ("attribute " + name + " has invalid lower bound");
            }
            if (upper != null && value > upper) {
                throw new JSONParserException ("attribute " + name + " has invalid upper bound");
            }
        }
        catch(JSONException e) {
            throw new JSONParserException ("no integer format for attribute " + name + " defined");
        }
    }

    /**
     * Prüft Inhalt des Map-Attributs
     * @throws JSONParserException
     */
    private void handleMapContent(JSONArray array) throws JSONParserException {
        // gültige Map-Attribute
        String[] children = {"mapgen", "gamemode", "width", "depth"};
        // Alle Kinder von Map auslesen
        for (int i = 0; i < array.length(); i++) {
            String child = children[i];
            JSONObject node = array.getJSONObject(i);
            // falls Kind nicht vorhanden
            if (!node.has(children[i])) {
                throw new JSONParserException("Attribute " + child + " for map not found");
            }
            if ("width".equals(child) || "depth".equals(child)) {
                handleContentAsInteger(child, 100, null);
            } else {
                handleContentAsString(child);
            }
        }
    }

    private List<String> commodities = new ArrayList<>();

    /**
     * Prüft commdities-Attribut und befüllt die Liste mit möglichen Werten
     * @param array
     * @throws JSONParserException
     */
    private void handleCommoditiesContent(JSONArray array) throws JSONParserException {
        if (array.isEmpty()) {
            throw new JSONParserException("commoditites are empty ");
        }
        for (int j = 0; j < array.length(); j++) {
            try {
                commodities.add(array.getString(j));
            }
            catch(JSONException e) {
                throw new JSONParserException("no string format defined");
            }
        }
    }

    /**
     * Prüft Inhalt des Vehicles-Attributs
     * @throws JSONParserException
     */
    private void handleVehiclesContent(JSONArray array) throws JSONParserException {
        // Länge prüfen
        int length = array.length();
        if (length < 3) {
            throw new JSONParserException("Too less vehicles ");
        }
        // Alle Kinder von Vehicles auslesen
        for (int i = 0; i < array.length(); i++) {

            JSONObject node = array.getJSONObject(i);
            // TODO kind/graphic/cargo/speed checken
        }
    }

    private void handleBuildingsContent(JSONArray array) throws JSONParserException {
        //TODO
    }



    /**
     * Fehlermeldung als Popup anzeigen
     * @param errorMessage
     */
    private void showAlert(String errorMessage) {
        //TODO: in die View refactorn
        Alert alert = new Alert(Alert.AlertType.ERROR, errorMessage);
        alert.show();
    }


    /**
     * Prüft ob alle Pflichtattribute der Wurzelknoten vorhanden sind
     * @throws JSONParserException
     */
    private void handleRootAttributes() throws JSONParserException {
        for (String rootAttribute: requiredRootAttributes) {
            if (json.has(rootAttribute)) {
                throw new JSONParserException("Attribute " + rootAttribute + " not found");
            }
        }
    }

}
