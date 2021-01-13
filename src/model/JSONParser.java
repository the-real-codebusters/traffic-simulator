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
     * prueft alle Railstation-Attribute auf Richtigkeit liefert ein neues Railstation-Objekt zurück
     * @param json
     * @return
     * @throws JSONParserException
     */
    private Railstation handleRailstationContent(JSONObject json) throws JSONParserException {
        Railstation railstation = new Railstation();
        String buildmenu = handleContentAsString(json, "buildmenu");
        checkBuildMenu(railstation.getBuildmenu(), buildmenu);

        setDefaultAttributes(railstation, json);

        Map<String, List<Double>> pointMap = handleBuildMenuPoints(json.getJSONObject("points"));
        List<List<String>> railList = handleBuildMenuInfrastructure(json.getJSONArray("rails"));

        railstation.setPoints(pointMap);
        railstation.setRails(railList);

        return railstation;
    }

    /**
     * prueft alle Terminal-Attribute auf Richtigkeit liefert ein neues TerminalTerminal-Objekt zurück
     * @param json
     * @return
     * @throws JSONParserException
     */
    private Terminal handleTerminalContent(JSONObject json) throws JSONParserException {
        Terminal terminal = new Terminal();
        String buildmenu = handleContentAsString(json, "buildmenu");
        checkBuildMenu(terminal.getBuildmenu(), buildmenu);

        setDefaultAttributes(terminal, json);

        Map<String, List<Double>> pointMap = handleBuildMenuPoints(json.getJSONObject("points"));
        List<List<String>> planes = handleBuildMenuInfrastructure(json.getJSONArray("planes"));

        terminal.setPoints(pointMap);
        terminal.setPlanes(planes);

        return terminal;
    }

    /**
     * Setzt Standard-Attribute für das jeweilige Building-Objekt
     * @param building
     * @param json
     * @throws JSONParserException
     */
    private void setDefaultAttributes(Building building, JSONObject json)  throws JSONParserException {
        int width = handleContentAsInteger(json, "width", null, null);
        int depth = handleContentAsInteger(json, "depth", null, null);
        int dz = handleContentAsInteger(json, "dz", null, null);
        building.setWidth(width);
        building.setDepth(depth);
        building.setDz(dz);
    }

    /**
     * prueft alle Tower-Attribute auf Richtigkeit liefert ein neues Tower-Objekt zurück
     * @param json
     * @return
     * @throws JSONParserException
     */
    private Tower handleTowerContent(JSONObject json) throws JSONParserException {
        Tower tower = new Tower();
        String buildmenu = handleContentAsString(json, "buildmenu");
        checkBuildMenu(tower.getBuildmenu(), buildmenu);

        setDefaultAttributes(tower, json);

        int maxplanes = handleContentAsInteger(json, "maxplanes", 0, null);
        tower.setMaxplanes(maxplanes);

        return tower;
    }

    /**
     * prueft alle Road-Attribute auf Richtigkeit liefert ein neues Road-Objekt zurück
     * @param json
     * @return
     * @throws JSONParserException
     */
    private Road handleRoadContent(JSONObject json, String... buildmenu) throws JSONParserException {
        Road road = new Road();
        setDefaultAttributes(road, json);

        Map<String, List<Double>> pointMap = handleBuildMenuPoints(json.getJSONObject("points"));
        List<List<String>> roadList = handleBuildMenuInfrastructure(json.getJSONArray("roads"));
        Map<String, String> combinesMap = new HashMap<>();
        if (json.has("combines")) {
            combinesMap = handleBuildMenuCombines(json.getJSONObject("combines"));
        }
        if (buildmenu.length == 1) {
            road.setBuildmenu(buildmenu[0]);
        }

        road.setPoints(pointMap);
        road.setRoads(roadList);
        road.setCombines(combinesMap);
        return road;

    }



    /**
     * Prueft ob Buildmenu korrekten Inhalt hat
     * @param expected
     * @param actual
     * @return
     * @throws JSONParserException
     */
    private boolean checkBuildMenu(String expected, String actual) throws JSONParserException {
        if (!expected.equals(actual)) {
            throw new JSONParserException("build menue + " + actual + " undefined, expected " + expected);
        }
        return true;
    }

    //TODO:
    private Tower handleTaxiwayContent(JSONObject json) throws JSONParserException {
        return null;
    }
    private Tower handleRunwayContent(JSONObject json) throws JSONParserException {
        return null;
    }
    private Tower handleBusstopContent(JSONObject json) throws JSONParserException {
        return null;
    }
    private Tower handleFactoryContent(JSONObject json) throws JSONParserException {
        return null;
    }
    private Tower handleJustCombinesContent(JSONObject json) throws JSONParserException {
        return null;
    }
    private Tower handleNatureContent(JSONObject json) throws JSONParserException {
        return null;
    }
    private Tower handleRailsContent(JSONObject json, String... buildmenu) throws JSONParserException {
        return null;
    }

    /**
     * prueft alle Buildings-Attribute mit vorhandenem Element buildmenu mit Wert 'road' und liefert ein neues Building-Objekt zurück
     * @param buildingsDetails
     * @return
     * @throws JSONParserException
     */
    private Building handleBuildMenuContent(JSONObject buildingsDetails) throws JSONParserException {
        if (buildingsDetails.has("special")) {
            // behandelt alle Elemente mit Attribut special
            String special = handleContentAsString(buildingsDetails, "special");
            switch (special) {
                case "railstation": return handleRailstationContent(buildingsDetails);
                case "tower": return handleTowerContent(buildingsDetails);
                case "terminal": return handleTerminalContent(buildingsDetails);
                case "taxiway": return handleTaxiwayContent(buildingsDetails);
                case "runway": return handleRunwayContent(buildingsDetails);
                case "nature": return handleNatureContent(buildingsDetails);
                case "justcombines": return handleJustCombinesContent(buildingsDetails);
                case "factory": return handleFactoryContent(buildingsDetails);
                case "busstop": return handleBusstopContent(buildingsDetails);
                default:
                    throw new JSONParserException("special + " + special + " not defined");
            }
        }
        else {
            // behandelt alle Elemente ohne Attribut special (road, rail)
            if (buildingsDetails.has("buildmenu")) {
                String buildmenu = handleContentAsString(buildingsDetails, "buildmenu");
                switch (buildmenu) {
                    case "road": return handleRoadContent(buildingsDetails, buildmenu);
                    case "rails": return handleRailsContent(buildingsDetails, buildmenu);
                    default:
                        throw new JSONParserException("wrong buildmenu + " + buildmenu + "  defined");
                }
            }
            else {
                //kein buildmenu -> suche nach Attribut road oder rails
                if (buildingsDetails.has("rails")) {
                    return handleRailsContent(buildingsDetails);
                }
                else if (buildingsDetails.has("roads")) {
                    return handleRoadContent(buildingsDetails);
                }
                else {
                    // kein special, kein buildmenu, keine rails/roads
                    throw new JSONParserException("wrong entry in json");
                }
            }
        }
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
     * Prüfe roads/planes/raods und befuelle entsprechend die zweidimensionale Liste
     * @param infrastructure
     * @return
     * @throws JSONParserException
     */
    private List<List<String>> handleBuildMenuInfrastructure(JSONArray infrastructure) throws JSONParserException {
        if (infrastructure.isEmpty()) {
            throw new JSONParserException("roads/planes/railes are empty ");
        }
        List<List<String>> roadList = new ArrayList<>();

        for (int i = 0; i <infrastructure.length(); i++) {
            JSONArray infrastructureData = infrastructure.getJSONArray(i);
            if (infrastructureData.isEmpty()) {
                throw new JSONParserException("roads/planes/railes data are empty ");
            }
            List<String> innerList = new ArrayList<>();
            for (int j = 0; j <infrastructureData.length(); j++) {
                innerList.add(infrastructureData.getString(j));
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
