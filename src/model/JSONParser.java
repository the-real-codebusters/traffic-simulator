package model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

import static view.ErrorAlert.showAlert;


/**
 * Parst JSON-Datei, wirft eine Exception im Fehlerfall
 * Befüllt Flughafen-Objekt
 */
public class JSONParser {

    private JSONObject json;
    private List<String> requiredRootAttributes = Arrays.asList("commodities", "buildings", "vehicles", "map");
    private List<String> commodities = new ArrayList<>();

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
            // pruefe root-attribute nacheinander
            handleCommoditiesContent(json.getJSONArray("commodities"));
            handleBuildingsContent(json.getJSONObject("buildings"));
            handleVehiclesContent(json.getJSONObject("vehicles"));
            handleMapContent(json.getJSONObject("map"));

       }
        catch(JSONException e){
            System.out.println(e.getMessage());
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
    private String handleContentAsString(JSONObject json, String name) throws JSONParserException{
        try {
            String value = json.getString(name);
            return value;
        }
        catch(JSONException e) {
            throw new JSONParserException ("no string format for attribute " + name + " defined");
        }
    }

    /**
     * Prüft ob Integer-Wert valide ist
     * @param name
     * @throws JSONParserException
     */
    private int handleContentAsInteger(JSONObject json, String name, Integer lower, Integer upper) throws JSONParserException{
        try {
            int value = json.getInt(name);
            if (lower != null && value < lower) {
                throw new JSONParserException ("attribute " + name + " has invalid lower bound");
            }
            if (upper != null && value > upper) {
                throw new JSONParserException ("attribute " + name + " has invalid upper bound");
            }
            return value;
        }
        catch(JSONException e) {
            throw new JSONParserException ("no integer format for attribute " + name + " defined");
        }
    }

    /**
     * Prüft ob Double-Wert valide ist
     * @param name
     * @throws JSONParserException
     */
    private double handleContentAsDouble(JSONObject json, String name, Double lower, Double upper) throws JSONParserException{
        try {
            double value = json.getDouble(name);
            if (lower != null && value < lower) {
                throw new JSONParserException ("attribute " + name + " has invalid lower bound");
            }
            if (upper != null && value > upper) {
                throw new JSONParserException ("attribute " + name + " has invalid upper bound");
            }
            return value;
        }
        catch(JSONException e) {
            throw new JSONParserException ("no double format for attribute " + name + " defined");
        }
    }

    /**
     * Prüft Inhalt des Map-Attributs
     * @throws JSONParserException
     */
    private void handleMapContent(JSONObject map) throws JSONParserException {
        // gültige Map-Attribute
        String[] children = {"mapgen", "gamemode", "width", "depth"};
        // Alle Kinder von Map auslesen
        for (int i = 0; i < map.length(); i++) {
            if (!map.has(children[i])) {
                throw new JSONParserException("Attribute " + children[i] + " for map not found");
            }
            if ("width".equals(children[i]) || "depth".equals(children[i])) {
                handleContentAsInteger(map, children[i], 100, null);
            } else {
                handleContentAsString(map, children[i]);
            }

        }
   }



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
     * prüfe Vehicle-Details
     * @param vehicles
     * @return
     * @throws JSONParserException
     */
    private String handleVehiclesDetails(JSONObject vehicles) throws JSONParserException {
        // gültige Vehicle-Attribute
        String[] children = {"kind", "graphic", "cargo", "speed"};

        String kind = "";

        // Alle Kinder von Vehicles auslesen
        for (int i = 0; i < vehicles.length(); i++) {
            // Engine-Type hat kein Cargo
            if (children[i].equals("cargo") && kind.equals("engine")) {
                continue;
            }
            if (!vehicles.has(children[i])) {
                throw new JSONParserException("Attribute " + children[i] + " for vehicles not found");
            }
            if ("speed".equals(children[i])) {
                handleContentAsDouble(vehicles, children[i], 0.0, null);
            }  else if ("kind".equals(children[i])) {
                kind = handleContentAsString(vehicles, children[i]);
            } else if ("graphic".equals(children[i])) {
                handleContentAsString(vehicles, children[i]);
            } else if( "cargo".equals(children[i])) {
                handleCargoContent(vehicles, children[i]);
            }

        }
        return kind;
    }

    /**
     * Prüfe Inhalt von Cargo-Attribut
     * @param vehicles
     * @param name
     * @throws JSONParserException
     */
    private void handleCargoContent(JSONObject vehicles, String name) throws JSONParserException {
        Object cargo = vehicles.get(name);
        // Cargo kommt entweder einzeln oder als Array vor
        if (cargo instanceof JSONObject) {
            checkCargoData((JSONObject)cargo, name);
        }
        else if (cargo instanceof  JSONArray){
            JSONArray array = (JSONArray) cargo;

            if (array.isEmpty()) {
                throw new JSONParserException(name + " is empty ");
            }
            for (int i = 0; i < array.length(); i++) {
                try {
                    JSONObject cargoDetails = array.getJSONObject(i);
                    checkCargoData(cargoDetails, name);
                } catch (JSONException e) {
                    throw new JSONParserException("no string format defined");
                }
            }
        }
        else {
            throw new JSONParserException(name + " has invalid format ");
        }

    }

    /**
     * prüft die Cargo-Daten und meldet eine Fehlermeldung, falls Commodity-Type oder Wert falsch ist
     * @param cargoDetails
     * @param name
     * @throws JSONParserException
     */
    private void checkCargoData(JSONObject cargoDetails, String name) throws  JSONParserException {
        Iterator<String> keys = cargoDetails.keys();
        while (keys.hasNext()) {
            String data = keys.next();
            // Prüfe ob der Typ einer der Commodity-Typen ist
            if (!commodities.contains(data)) {
                throw new JSONParserException("invalid cargo commodity type: " + data);
            }
            // Wert des Cargo-Typen
            String value = cargoDetails.optString(data);
            try {
                int value_int = Integer.valueOf(value);
                if (value_int <= 0) {
                    throw new JSONParserException ("attribute " + name + " has negative value");
                }
            }
            catch(NumberFormatException e){
                throw new JSONParserException ("no integer format for attribute " + name + " defined");
            }
        }
    }
    /**
     * Prüft Inhalt des Vehicles-Attributs
     * @throws JSONParserException
     */
    private void handleVehiclesContent(JSONObject vehicles) throws JSONParserException {
        // zähler für mögliche Vehicle-Typen
        int roadVehicleCounter = 0, planeCounter = 0, engineCounter = 0, wagonCounter = 0;
        // hole alle möglichen Vehicle-Typen
        Iterator<String> keys = vehicles.keys();
        while(keys.hasNext()) {
            String vehicle =keys.next();
            JSONObject vehicleDetails = vehicles.getJSONObject(vehicle);
            String kind = handleVehiclesDetails(vehicleDetails);
            // vehicle-Typen aufzählen
            switch (kind) {
                case "road vehicle": roadVehicleCounter++;break;
                case "plane": planeCounter++;break;
                case "engine": engineCounter++;break;
                case "wagon": wagonCounter++;break;
                default:  throw new JSONParserException("Invalid value " + kind + " for attribute kind");
            }
        }
        // Fehler falls mind. 1 Typ fehlt
        if (roadVehicleCounter == 0) {
            throw new JSONParserException("At least one road vehicle should be definied");
        }
        if (planeCounter == 0) {
            throw new JSONParserException("At least one plane should be definied");
        }
        if (engineCounter == 0) {
            throw new JSONParserException("At least one engine should be definied");
        }
        if (wagonCounter == 0) {
            throw new JSONParserException("At least one wagon should be definied");
        }

    }

    private List<Building> buildings = new ArrayList<>();

    public List<Building> getBuildings() {
        return buildings;
    }

    public void getBuildingsAsString() {
        for (Building b: buildings) {
            System.out.println(b);
        }
    }

    /**
     * Prueft buildings auf Richtigkeit und befüllt die Liste mit erzeugten Objekten
     * @param buildings
     * @throws JSONParserException
     */
    private void handleBuildingsContent(JSONObject buildings) throws JSONParserException {
        Iterator<String> keys = buildings.keys();
        while (keys.hasNext()) {
            String data = keys.next();
            JSONObject buildingsDetails = buildings.getJSONObject(data);
            Building building = handleBuildMenuContent(buildingsDetails);
            if (building != null) {
                this.buildings.add(building);
            }
        }
    }

    /**
     * prueft alle Buildings-Attribute mit vorhandenem Element buildmenu mit Wert 'road' und liefert ein neues Building-Objekt zurück
     * @param buildings
     * @return
     * @throws JSONParserException
     */
    private Building handleBuildMenuContent(JSONObject buildings) throws JSONParserException {
        if (buildings.has("buildmenu")) {
            Building building = new Building();
            // TODO: welche gültigen buildmenus sind erlaubt?
            String buildmenu = handleContentAsString(buildings, "buildmenu");
            if (buildmenu.equals("road")) {
                int width = handleContentAsInteger(buildings, "width", null, null);
                int depth = handleContentAsInteger(buildings, "depth", null, null);
                int dz = handleContentAsInteger(buildings, "dz", null, null);
                Map<String, List<Double>> pointMap = handleBuildMenuPoints(buildings.getJSONObject("points"));
                List<List<String>> roadList = handleBuildMenuRoads(buildings.getJSONArray("roads"));
                Map<String, String> combinesMap = new HashMap<>();
                if (buildings.has("combines")) {
                    combinesMap = handleBuildMenuCombines(buildings.getJSONObject("combines"));
                }
                building.setType(buildmenu);
                building.setWidth(width);
                building.setDepth(depth);
                building.setDz(dz);
                building.setPoints(pointMap);
                building.setRoads(roadList);
                building.setCombines(combinesMap);
                return building;
            }


        }
        return null;
    }

    /**
     * Prüfe combines und befuelle entsprechend die Map
     * @param combines
     * @return
     * @throws JSONParserException
     */
    private Map<String, String> handleBuildMenuCombines(JSONObject combines) throws JSONParserException {
        Map<String, String> combineData = new HashMap<>();
        Iterator<String> keys = combines.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            //TODO: prüfe ob Wert stimmt
            String value = combines.optString(key);
            combineData.put(key,value);
        }
        return combineData;
    }

    /**
     * Prüfe roads und befuelle entsprechend die zweidimensionale Liste
     * @param roads
     * @return
     * @throws JSONParserException
     */
    private List<List<String>> handleBuildMenuRoads(JSONArray roads) throws JSONParserException {
        if (roads.isEmpty()) {
            throw new JSONParserException("roads are empty ");
        }
        List<List<String>> roadList = new ArrayList<>();

        for (int i = 0; i <roads.length(); i++) {
            JSONArray roadsData = roads.getJSONArray(i);
            if (roadsData.isEmpty()) {
                throw new JSONParserException("roads data are empty ");
            }
            List<String> innerList = new ArrayList<>();
            for (int j = 0; j <roadsData.length(); j++) {
                innerList.add(roadsData.getString(j));
            }
            roadList.add(innerList);
        }
        return roadList;
    }

    /**
     * Prüfe points und befuelle entsprechend die Map
     * @param points
     * @return
     * @throws JSONParserException
     */
    private Map<String, List<Double>> handleBuildMenuPoints(JSONObject points) throws JSONParserException {
        Map<String, List<Double>> pointMap = new HashMap<>();
        Iterator<String> keys = points.keys();
        while (keys.hasNext()) {
            String data = keys.next();
            //TODO: prüfe ob himmelsrichtung stimmt
            JSONArray values = points.optJSONArray(data);
            List<Double> pointData = new ArrayList<>();
            if (values.isEmpty()) {
                throw new JSONParserException("points data are empty ");
            }
            for (int i = 0; i < values.length(); i++) {
                try {
                    Double d = values.getDouble(i);
                    pointData.add(d);
                }
                catch(JSONException e) {
                    throw new JSONParserException("wrong double format for attribute " + data);
                }
            }
            pointMap.put(data,pointData );
        }
        return pointMap;
    }







    /**
     * Prüft ob alle Pflichtattribute der Wurzelknoten vorhanden sind
     * @throws JSONParserException
     */
    private void handleRootAttributes() throws JSONParserException {
        for (String rootAttribute: requiredRootAttributes) {
            if (! json.has(rootAttribute)) {
                throw new JSONParserException("Attribute " + rootAttribute + " not found");
            }
        }
    }

}
