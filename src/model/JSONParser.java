package model;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
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

    private BasicModel model;
    private ResourceBundle resourceBundle;
    private JSONObject json;
    private List<String> requiredRootAttributes = Arrays.asList("commodities", "buildings", "vehicles", "map");
    private List<String> commodities = new ArrayList<>();

    /**
     * Zeigt eine Fehlermeldung an, wenn JSON-Datei fehlerhaft
     * @param filename - JSON-Dateiname zum Laden
     * @return - true im Erfolgsfall, sonst false
     */
    public boolean parse(String filename, BasicModel model, ResourceBundle resourceBundle) {
        this.model = model;
        this.resourceBundle = resourceBundle;
        // JSON-Datei wird eingelesen
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            String file = resourceBundle.getString("file");
            String notFound = resourceBundle.getString("notFound");
            showAlert(file + notFound, resourceBundle);

            return false;
        }
        try {
            json = new JSONObject(new JSONTokener(inputStream));
            // Pflichtattribute alle da?
            handleRootAttributes();
            // pruefe root-attribute nacheinander
            handleCommoditiesContent(json.getJSONArray("commodities"));
            handleBuildingsContent(json.getJSONObject("buildings"), model);
            handleVehiclesContent(json.getJSONObject("vehicles"));
            handleMapContent(json.getJSONObject("map"));
       }
        catch(JSONException e){
            String hasNoJsonFormat = resourceBundle.getString("hasNoJsonFormat");
            String msg = resourceBundle.getString("file") + filename + hasNoJsonFormat;
            showAlert(msg, resourceBundle);
                return false;
            }
        catch(JSONParserException e){
                showAlert(e.getMessage(), resourceBundle);
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
            String noStringFormat = resourceBundle.getString("noStringFormat");
            String defined = resourceBundle.getString("defined");
            throw new JSONParserException (noStringFormat + name + defined);
        }
    }

    /**
     * Prüft ob Integer-Wert valide ist
     * @param name
     * @throws JSONParserException
     */
    private int handleContentAsInteger(JSONObject json, String name, Integer lower, Integer upper) throws JSONParserException{
        try {
            String attribute = resourceBundle.getString("attribute");
            int value = json.getInt(name);
            if (lower != null && value < lower) {
                String hasInvalidLowerBound = resourceBundle.getString("hasInvalidLowerBound");
                throw new JSONParserException (attribute + name + hasInvalidLowerBound);
            }
            if (upper != null && value > upper) {
                String hasInvalidUpperBound = resourceBundle.getString("hasInvalidUpperBound");
                throw new JSONParserException (attribute + name + hasInvalidUpperBound);
            }
            return value;
        }
        catch(JSONException e) {
            String noIntegerFormat = resourceBundle.getString("noIntegerFormat");
            throw new JSONParserException (noIntegerFormat + name + resourceBundle.getString("defined"));
        }
    }

    /**
     * Prüft ob Double-Wert valide ist
     * @param name
     * @throws JSONParserException
     */
    private double handleContentAsDouble(JSONObject json, String name, Double lower, Double upper) throws JSONParserException{
        try {
            String attribute = resourceBundle.getString("attribute");
            double value = json.getDouble(name);
            if (lower != null && value < lower) {
                String hasInvalidLowerBound = resourceBundle.getString("hasInvalidLowerBound");
                throw new JSONParserException (attribute + name + hasInvalidLowerBound);
            }
            if (upper != null && value > upper) {
                String hasInvalidUpperBound = resourceBundle.getString("hasInvalidUpperBound");
                throw new JSONParserException (attribute + name + hasInvalidUpperBound);
            }
            return value;
        }
        catch(JSONException e) {
            String noDoubleFormat = resourceBundle.getString("noDoubleFormat");
            throw new JSONParserException (noDoubleFormat + name + resourceBundle.getString("defined"));
        }
    }

    /**
     * Prüft Inhalt des Map-Attributs
     * @throws JSONParserException
     */
    private void handleMapContent(JSONObject map) throws JSONParserException {
        int width = 0;
        int depth = 0;
        String mapgen = "";
        String gamemode = "";
        // gültige Map-Attribute
        String[] children = {"mapgen", "gamemode", "width", "depth"};
        // Alle Kinder von Map auslesen
        for (int i = 0; i < map.length(); i++) {
            if (!map.has(children[i])) {
                System.out.println(resourceBundle.getString("attribute") + children[i]
                        + resourceBundle.getString("notFound"));
                throw new JSONParserException(resourceBundle.getString("attribute") + children[i]
                        + resourceBundle.getString("notFound"));
            }
            if ("width".equals(children[i])) {
                width = handleContentAsInteger(map, children[i], 100, null);
            } else if("depth".equals(children[i])){
                depth = handleContentAsInteger(map, children[i], 100, null);
            } else if ("mapgen".equals(children[i])) {
                mapgen = handleContentAsString(map, children[i]);
            } else {
                gamemode = handleContentAsString(map, children[i]);
            }
            // mapModel wird aus eingelesenen Werten erzeugt und dem model hinzugefügt
            MapModel mapModel = new MapModel(width, depth, model);
            mapModel.setMapgen(mapgen);
            // mapModel und eingelesener gamemode werden dem model hinzugefügt
            model.setMap(mapModel);
            model.setGamemode(gamemode);

        }
   }



    /**
     * Prüft commdities-Attribut und befüllt die Liste mit möglichen Werten
     * @param array
     * @throws JSONParserException
     */
    private void handleCommoditiesContent(JSONArray array) throws JSONParserException {
        if (array.isEmpty()) {
            String commoditiesAreEmpty = resourceBundle.getString("commoditiesAreEmpty");
            throw new JSONParserException(commoditiesAreEmpty);
        }
        for (int j = 0; j < array.length(); j++) {
            try {
                commodities.add(array.getString(j));
            }
            catch(JSONException e) {
                String noStringFormatDefined = resourceBundle.getString("noStringFormatDefined");
                throw new JSONParserException(noStringFormatDefined);
            }
        }
        // ausgelesene commodities werden dem model hinzugefügt
        model.addCommodities(commodities);
    }

    /**
     * prüfe Vehicle-Details
     * @param jsonVehicles
     * @return
     * @throws JSONParserException
     */
    private String handleVehiclesDetails(JSONObject jsonVehicles) throws JSONParserException {
        // gültige Vehicle-Attribute
        String[] children = {"kind", "graphic", "cargo", "speed"};

        String kind = "";

        Vehicle vehicle = new Vehicle();

        // Alle Kinder von Vehicles auslesen
        for (int i = 0; i < jsonVehicles.length(); i++) {
            for(int y=0; y<children.length; y++){
                String forJsonVehicles = resourceBundle.getString("forJsonVehicles");
                if (!jsonVehicles.has(children[y]) && !children[y].equals("cargo")) {
                    throw new JSONParserException(resourceBundle.getString("attribute") + children[y]
                            + forJsonVehicles + resourceBundle.getString("notFound"));
                }

                if ("speed".equals(children[y])) {
                    vehicle.setSpeed(handleContentAsDouble(jsonVehicles, children[y], 0.0, null));
                }  else if ("kind".equals(children[y])) {
                    kind = handleContentAsString(jsonVehicles, children[y]);
                    vehicle.setKind(kind);
                    if(kind.equals("road vehicle")){
                        vehicle.setTrafficType(TrafficType.ROAD);
                    }
                    else if(kind.equals("wagon")){
                        vehicle.setTrafficType(TrafficType.RAIL);
                    }
                    else if(kind.equals("engine")){
                        vehicle.setTrafficType(TrafficType.RAIL);
                    }
                    else if(kind.equals("plane")){
                        vehicle.setTrafficType(TrafficType.AIR);
                    }
                    else throw new JSONParserException(resourceBundle.getString("invalidVehicleKind"));
                } else if ("graphic".equals(children[y])) {
                    vehicle.setGraphic(handleContentAsString(jsonVehicles, children[y]));
                }
                // Engine-Type hat kein Cargo
                else if( "cargo".equals(children[y]) && !kind.equals("engine")) {
                    handleCargoContent(jsonVehicles, children[y], vehicle);
                }
            }
        }
        model.getVehiclesTypes().add(vehicle);
        return kind;
    }

    /**
     * Prüfe Inhalt von Cargo-Attribut
     * @param vehicles
     * @param name
     * @throws JSONParserException
     */
    private void handleCargoContent(JSONObject vehicles, String name, Vehicle vehicleInstance) throws JSONParserException {

        HashMap<String, Integer> cargoMaxima = new HashMap<>();
        Object cargo = vehicles.get(name);
        // Cargo kommt entweder einzeln oder als Array vor
        if (cargo instanceof JSONObject) {
            handleCargoData((JSONObject)cargo, name, cargoMaxima);
        }
        else if (cargo instanceof  JSONArray){
            JSONArray array = (JSONArray) cargo;

            if (array.isEmpty()) {
                throw new JSONParserException(name + resourceBundle.getString("isEmpty"));
            }
            for (int i = 0; i < array.length(); i++) {
                try {
                    JSONObject cargoDetails = array.getJSONObject(i);
                    handleCargoData(cargoDetails, name, cargoMaxima);
                } catch (JSONException e) {
                    throw new JSONParserException(resourceBundle.getString("noStringFormatDefined"));
                }
            }
        }
        else {
            throw new JSONParserException(name + resourceBundle.getString("hasInvalidFormat"));
        }

        Storage storage = new Storage(cargoMaxima);
        vehicleInstance.setStorage(storage);
    }

    /**
     * prüft die Cargo-Daten und meldet eine Fehlermeldung, falls Commodity-Type oder Wert falsch ist
     * @param cargoDetails
     * @param name
     * @throws JSONParserException
     */
    private void handleCargoData(JSONObject cargoDetails, String name, Map<String, Integer> cargoMaxima)
            throws  JSONParserException {
        Iterator<String> keys = cargoDetails.keys();
        while (keys.hasNext()) {
            String data = keys.next();
            // Prüfe ob der Typ einer der Commodity-Typen ist
            if (!commodities.contains(data)) {
                String invalidCargoCommodityType = resourceBundle.getString("invalidCargoCommodityType");
                throw new JSONParserException(invalidCargoCommodityType + data);
            }
            // Wert des Cargo-Typen
            String value = cargoDetails.optString(data);
            try {
                int value_int = Integer.valueOf(value);
                if (value_int <= 0) {
                    throw new JSONParserException (resourceBundle.getString("attribute") + name
                            + resourceBundle.getString("hasNegativeValue"));
                }
                cargoMaxima.put(data, value_int);
            }
            catch(NumberFormatException e){
                throw new JSONParserException (resourceBundle.getString("noIntegerFormat") + name
                        + resourceBundle.getString("defined"));
            }

        }
    }

    /**
     * Prüft Production-Daten und befüllt sie in ein Factory-Objekt
     * @param productionDetails
     * @param factory
     * @throws JSONParserException
     */
    private void checkProductionData(JSONObject productionDetails, Factory factory) throws  JSONParserException {
        Map<String, Integer> produceMap = new HashMap<>();
        Map<String, Integer> consumeMap = new HashMap<>();
        int duration = 0;
        Iterator<String> keys = productionDetails.keys();
        // über alle Elemente iterieren
        while (keys.hasNext()) {
            String data = keys.next();
            if (data.equals("produce")) {
                // produce Daten holen
                JSONObject prod = productionDetails.getJSONObject(data);
                Iterator<String> keysProd = prod.keys();
                while (keysProd.hasNext()) {
                    String type = keysProd.next();
                    int amount = prod.optInt(type);
                    produceMap.put(type, amount);
                }
            }
            else if (data.equals("consume")) {
                // consume Daten holen
                JSONObject cons = productionDetails.getJSONObject(data);
                Iterator<String> keysCons = cons.keys();
                while (keysCons.hasNext()) {
                    String type = keysCons.next();
                    int amount = cons.optInt(type);
                    consumeMap.put(type, amount);
                }
            }
            else if (data.equals("duration")) {
                duration = handleContentAsInteger(productionDetails, "duration", 0, null);
            }
            else {
                throw new JSONParserException (resourceBundle.getString("invalidAttributeForProduction") + data);
            }
            System.out.println("consume map: "+consumeMap);
            System.out.println("production map: "+produceMap);

            factory.getProductionSteps().add(new ProductionStep(produceMap, consumeMap, duration));
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
                default:  throw new JSONParserException(resourceBundle.getString("invalidValue") + kind
                        + resourceBundle.getString("forAttributeKind"));
            }
        }
        // Fehler falls mind. 1 Typ fehlt
        if (roadVehicleCounter == 0) {
            throw new JSONParserException(resourceBundle.getString("atLeastOneRoadVehicle"));
        }
        if (planeCounter == 0) {
            throw new JSONParserException(resourceBundle.getString("atLeastOnePlane"));
        }
        if (engineCounter == 0) {
            throw new JSONParserException(resourceBundle.getString("atLeastOneEngine"));
        }
        if (wagonCounter == 0) {
            throw new JSONParserException(resourceBundle.getString("atLeastOneWagon"));
        }

    }

    /**
     * Prueft buildings auf Richtigkeit und befüllt die Liste mit erzeugten Objekten
     * @param buildings
     * @throws JSONParserException
     */
    private void handleBuildingsContent(JSONObject buildings, BasicModel model) throws JSONParserException {
        Iterator<String> keys = buildings.keys();
        Set<String> buildMenus = new HashSet<>();
        while (keys.hasNext()) {
            String name = keys.next();
            JSONObject buildingsDetails = buildings.getJSONObject(name);
//            System.out.println("name "+name);
            Building building = handleBuildMenuContent(buildingsDetails);
            if (building != null) {
                building.setBuildingName(name);
                model.getBuildings().add(building);
                if(building.getBuildingName().contains("road")) {
//                    System.out.println("handleBuildingsContent " + name);
                }
            }
            if(buildingsDetails.has("buildmenu")){
                buildMenus.add(buildingsDetails.getString("buildmenu"));
                if(building.getBuildingName().contains("road")) {
//                    System.out.println("handleBuildingsContent " + name);
                }
            }
        }
        model.setBuildmenus(buildMenus);
    }


    /**
     * prueft alle Stop-Attribute auf Richtigkeit liefert ein neues Stop-Objekt zurück
     * @param json
     * @return
     * @throws JSONParserException
     */
    private Stop handleStopContent(JSONObject json, String kind) throws JSONParserException {
        Stop stop = new Stop();
        setDefaultAttributes(stop, json);

        String buildmenu = handleContentAsString(json, "buildmenu");
        checkBuildMenu(stop.getBuildmenu(), buildmenu);


        Map<String, List<Double>> pointMap = handleBuildMenuPoints(json.getJSONObject("points"));
        List<List<String>> railList = handleBuildMenuInfrastructure(json.getJSONArray(kind));

        stop.setPoints(pointMap);
        stop.setTransportations(railList);

        return stop;
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
        String buildmenu = json.has("buildmenu") ? handleContentAsString(json, "buildmenu") : null;
        building.setBuildmenu(buildmenu);
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

        setDefaultAttributes(tower, json);
        String buildmenu = handleContentAsString(json, "buildmenu");
        checkBuildMenu(tower.getBuildmenu(), buildmenu);

        int maxplanes = handleContentAsInteger(json, "maxplanes", 0, null);
        tower.setMaxplanes(maxplanes);

        return tower;
    }

    /**
     * prueft alle Road-Attribute auf Richtigkeit liefert ein neues Road-Objekt zurück. Buildmenu kann optional übergeben und geprüft werden
     * @param json
     * @return
     * @throws JSONParserException
     */
    private Road handleRoadContent(JSONObject json, String... buildmenu) throws JSONParserException {
        Road road = new Road();
        setDefaultAttributes(road, json);

        Map<String, List<Double>> pointMap = handleBuildMenuPoints(json.getJSONObject("points"));
        List<List<String>> roadList = handleBuildMenuInfrastructure(json.getJSONArray("roads"));
        if(roadList.get(0).get(0).equals("ne") && roadList.get(0).get(1).equals("c") && roadList.size() == 1) {
        }
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
        road.setTrafficType(TrafficType.ROAD);
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
            throw new JSONParserException(resourceBundle.getString("buildmenu") + actual
                    + " undefined, expected " + resourceBundle.getString("undefinedExpected"));
        }
        return true;
    }

    private Runway handleRunwayContent(JSONObject json) throws JSONParserException {
        Runway runway = new Runway();
        setDefaultAttributes(runway, json);
        String buildmenu = handleContentAsString(json, "buildmenu");
        checkBuildMenu(runway.getBuildmenu(), buildmenu);


        Map<String, List<Double>> pointMap = handleBuildMenuPoints(json.getJSONObject("points"));
        List<List<String>> planes = handleBuildMenuInfrastructure(json.getJSONArray("planes"));

        JSONArray entryJson = json.getJSONArray("entry");

        if (entryJson.isEmpty()) {
//            throw new JSONParserException("signal data is empty ");
            throw new JSONParserException(resourceBundle.getString("signalDataEmpty"));
        }
        List<String> entries = new ArrayList<>();
        for (int i = 0; i < entryJson.length(); i++) {
            entries.add(entryJson.getString(i));
        }

        runway.setPoints(pointMap);
        runway.setTransportations(planes);
        runway.setEntry(entries);

        return runway;
    }

    private Factory handleFactoryContent(JSONObject json) throws JSONParserException {
        Factory factory = new Factory();
        // [{"consume":{"glass":4, "silicone":1},"duration":8}, {"consume":{"solar panels":2},"duration":25}]

        setDefaultAttributes(factory, json);
        // prüfe Storage-Attribut
        Map<String, Integer> storageMap = new HashMap<>();
        if (json.has("storage")) {
            Object object  = json.get("storage");
            if(object instanceof JSONObject){
                JSONObject storage  = json.getJSONObject("storage");
                Iterator<String> keys = storage.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    int value = storage.optInt(key);
                    storageMap.put(key,value);
                }
            }
            else if(object instanceof JSONArray){
                JSONArray array = (JSONArray)object;
                for (int i = 0; i < array.length(); i++) {
                    JSONObject storage  = array.getJSONObject(i);
                    Iterator<String> keys = storage.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        int value = storage.optInt(key);
                        storageMap.put(key,value);
                    }
                }
            }
            else {
                throw new JSONParserException(object+" was no JSONArray or JSONObject");
            }
            Storage storage = new Storage(storageMap);
            System.out.println("storage in JSON-parser: "+storage);
            factory.setStorage(storage);
        }
//        else {
//            System.out.println("kein Storage bei factory ");
//        }

        Object productions = json.get("productions");
        // Productions kommt entweder einzeln oder als Array vor
        if (productions instanceof JSONObject) {
            checkProductionData((JSONObject)productions, factory);
        }
        else if (productions instanceof  JSONArray){
            JSONArray array = (JSONArray) productions;

            if (array.isEmpty()) {
                throw new JSONParserException(resourceBundle.getString("productionsEmpty"));
            }
            for (int i = 0; i < array.length(); i++) {
                try {
                    JSONObject prodDetails = array.getJSONObject(i);
                    checkProductionData((JSONObject)prodDetails, factory);
                } catch (JSONException e) {
                    throw new JSONParserException(resourceBundle.getString("noStringFormat"));
                }
            }
        }
        else {
            throw new JSONParserException(resourceBundle.getString("productionsInvalid"));
        }
        return factory;
    }

    private JustCombines handleJustCombinesContent(JSONObject json) throws JSONParserException {
        JustCombines justCombines = new JustCombines();

        String buildmenu = handleContentAsString(json, "buildmenu");
        justCombines.setBuildmenu(buildmenu);

        Map<String, String> combinesMap = new HashMap<>();
        if (json.has("combines")) {
            combinesMap = handleBuildMenuCombines(json.getJSONObject("combines"));
        }
        justCombines.setCombines(combinesMap);


        return justCombines;
    }
    private Nature handleNatureContent(JSONObject json) throws JSONParserException {
        Nature nature = new Nature();
        setDefaultAttributes(nature, json);
        if (json.has("buildmenu")) {
            String buildmenu = handleContentAsString(json, "buildmenu");
            checkBuildMenu(nature.getBuildmenu(), buildmenu);
        }

        return nature;
    }
    private Rail handleRailsContent(JSONObject json, String... buildmenu) throws JSONParserException {
        Rail rail = new Rail();
        setDefaultAttributes(rail, json);

        Map<String, List<Double>> pointMap = handleBuildMenuPoints(json.getJSONObject("points"));
        List<List<String>> railList = handleBuildMenuInfrastructure(json.getJSONArray("rails"));

        if (buildmenu.length == 1) {
            rail.setBuildmenu(buildmenu[0]);
        }

        List<String> signals = new ArrayList<>();
        if (json.has("signals")) {
            JSONArray signalJson = json.getJSONArray("signals");

            if (signalJson.isEmpty()) {
                throw new JSONParserException(resourceBundle.getString("signalDataEmpty"));
            }

            for (int i = 0; i < signalJson.length(); i++) {
                signals.add(signalJson.getString(i));
            }
        }


        rail.setPoints(pointMap);
        rail.setRails(railList);
        rail.setSignals(signals);
        rail.setTrafficType(TrafficType.RAIL);
        return rail;
    }

    /**
     * prueft alle Buildings-Attribute geordnet nach Attribut special und liefert ein neues Building-Objekt zurück
     * @param buildingsDetails
     * @return
     * @throws JSONParserException
     */
    private Building handleBuildMenuContent(JSONObject buildingsDetails) throws JSONParserException {
        if (buildingsDetails.has("special")) {
            // behandelt alle Elemente mit Attribut special
            String special = handleContentAsString(buildingsDetails, "special");
            Special specialObject;
            switch (special) {
                case "railstation": specialObject = handleStopContent(buildingsDetails, "rails"); break;
                case "tower": specialObject = handleTowerContent(buildingsDetails); break;
                case "terminal": specialObject = handleStopContent(buildingsDetails, "planes"); break;
                case "taxiway": specialObject = handleStopContent(buildingsDetails, "planes"); break;
                case "runway": specialObject = handleRunwayContent(buildingsDetails); break;
                case "nature": specialObject = handleNatureContent(buildingsDetails); break;
                case "justcombines": specialObject = handleJustCombinesContent(buildingsDetails); break;
                case "factory": specialObject = handleFactoryContent(buildingsDetails); break;
                case "busstop": specialObject = handleStopContent(buildingsDetails, "roads"); break;
                default:
                    throw new JSONParserException(resourceBundle.getString("special") + special
                            + resourceBundle.getString("notDefined"));
            }
            specialObject.setSpecial(special);
            return specialObject;
        }
        else {
            // behandelt alle Elemente ohne Attribut special (road, rail)
            if (buildingsDetails.has("buildmenu")) {
                String buildmenu = handleContentAsString(buildingsDetails, "buildmenu");
                switch (buildmenu) {
                    case "road": return handleRoadContent(buildingsDetails, buildmenu);
                    case "rail": return handleRailsContent(buildingsDetails, buildmenu);
                    default:
                        throw new JSONParserException(resourceBundle.getString("wrongBuildmenu") + buildmenu
                                + resourceBundle.getString("defined"));
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
                    throw new JSONParserException(resourceBundle.getString("wrongEntry"));
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
            throw new JSONParserException(resourceBundle.getString("roads/planes/railsAreEmpty"));
        }
        List<List<String>> roadList = new ArrayList<>();

        for (int i = 0; i <infrastructure.length(); i++) {
            JSONArray infrastructureData = infrastructure.getJSONArray(i);
            if (infrastructureData.isEmpty()) {
                throw new JSONParserException(resourceBundle.getString("roads/planes/railsDataAreEmpty"));
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
            JSONArray values = points.optJSONArray(data);
            List<Double> pointData = new ArrayList<>();
            if (values.isEmpty()) {
                throw new JSONParserException(resourceBundle.getString("pointsDataEmpty"));
            }
            for (int i = 0; i < values.length(); i++) {
                try {
                    Double d = values.getDouble(i);
                    pointData.add(d);
                }
                catch(JSONException e) {
                    throw new JSONParserException(resourceBundle.getString("wrongDoubleFormat") + data);
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
                throw new JSONParserException(resourceBundle.getString("attribute") + rootAttribute
                        + resourceBundle.getString("notFound"));
            }
        }
    }

}
