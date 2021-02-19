package model;

import javafx.geometry.Point2D;
import javafx.geometry.Pos;

import java.util.*;

public class BasicModel {
    private Set<String> commodities;
    //    private List<Reservation> reservations;

    // ein day = ein tick
    private int day;
    private double speedOfDay;
    private MapModel map;

    private String gamemode;
//    private ToolsModel tools;

    private Set<String> buildmenus = new HashSet<>();
    private List<Building> buildings = new ArrayList<>();
    private List<Vehicle> vehiclesTypes = new ArrayList<>();

    // Die Verkehrslinien, die seit dem letzten Tag neu erstellt wurden oder nur eine Station haben und damit unfertig sind.
    private Queue<ConnectedTrafficPart> newCreatedOrIncompleteConnectedTrafficParts = new ArrayDeque<>();

    // Alle Verkehrslinien mit mehr als einer Station, die schon Verkehrsmittel auf sich fahren haben sollten
    private List<ConnectedTrafficPart> activeConnectedTrafficParts = new ArrayList<>();

    private Pathfinder pathfinder;

    public BasicModel(Set<String> commodities, int day, double speedOfDay, MapModel map, String gamemode,
                      Set<String> buildmenus, ArrayList<Building> buildings, TrafficGraph roadsGraph) {
        this.commodities = commodities;
        this.day = day;
        this.speedOfDay = speedOfDay;
        this.map = map;
        this.gamemode = gamemode;
        this.buildmenus = buildmenus;
        this.buildings = buildings;
    }

    public BasicModel() {
        this.commodities = new HashSet<String>();
        this.day = 0;
        this.speedOfDay = 1.0;
        this.map = null;
        this.gamemode = null;
        this.buildmenus = null;
    }


    /**
     * Soll einen Tag, also eine Runde, simulieren
     * @return eine Liste von Fahrzeugbewegungen des aktuellen Tags
     */
    public List<VehicleMovement> simulateOneDay(){
//        System.out.println("simulate day "+day);

        // runway platzieren (2x) -> Koordinaten speichern
        // gelbe Punkte platzieren (?)
        // flugzeug von a nach b sich bewegen und zurück
        //



        // In der Zeit einer Runde, also seit dem letzten Aufruf dieser Methode, können Haltestellen platziert worden
        // sein, die zu neuen, unverbundenen Stationen führen. Eine unverbundene Station stellt erstmal eine neue
        // Verkehrslinie dar. Diese neue Verkehrslinie wurde der Queue newCreatedOrIncompleteTrafficLines hinzugefügt
        List<ConnectedTrafficPart> incompleteConnectedTrafficParts = new ArrayList<>();
        while(!newCreatedOrIncompleteConnectedTrafficParts.isEmpty()){
            ConnectedTrafficPart newOrIncompleteConnectedTrafficPart = newCreatedOrIncompleteConnectedTrafficParts.remove();

            if(newOrIncompleteConnectedTrafficPart.checkIfMoreThanOneStation()){
                if(newOrIncompleteConnectedTrafficPart.getTrafficType().equals(TrafficType.ROAD)){
                    activeConnectedTrafficParts.add(newOrIncompleteConnectedTrafficPart);

                    //TODO Andere TrafficTypes fehlen noch
                }
                if(newOrIncompleteConnectedTrafficPart.getTrafficType().equals(TrafficType.AIR)){
                    activeConnectedTrafficParts.add(newOrIncompleteConnectedTrafficPart);
                }
            }
            // Eine Station, die nur eine Station hat, ist eine unfertige Verkehrslinie
            else {
                incompleteConnectedTrafficParts.add(newOrIncompleteConnectedTrafficPart);
            }


        }
        newCreatedOrIncompleteConnectedTrafficParts.addAll(incompleteConnectedTrafficParts);

        //TODO Es funktioniert, wenn eine Station direkt an eine Verkehrslinie gebaut wird. Es funkltioniert noch nicht,
        // wenn eine existierende Station so erweitert wird, dass sie an eine existierende TrafficLine anschließt

        List<Vehicle> activeVehicles = new ArrayList<>();
        for(ConnectedTrafficPart activePart: activeConnectedTrafficParts){
            // Für jede aktive Verkehrslinie wird ein neues Fahrzeug hinzugefügt, wenn es weniger Fahrzeuge gibt als die gewünschte
            // Anzahl
            for(TrafficLine trafficLine : activePart.getTrafficLines()){
                System.out.println("active traffic Line "+trafficLine.getName());
                if(trafficLine.getTotalDesiredNumbersOfVehicles() > trafficLine.getVehicles().size()){
                    Vehicle newVehicle = trafficLine.getMissingVehicleOrNull().getNewInstance();
                    trafficLine.addNewVehicle(newVehicle);
                }
                // Der Liste der aktiven Fahrzeuge werden die Fahrzeuge jeder aktiven Linie hinzugefügt
                activeVehicles.addAll(trafficLine.getVehicles()); //TODO
            }
        }

        //newCreatedOrIncompleteTrafficLines.peek().get

        //VehicleMovement airplaneMovement = new VehicleMovement(new PositionOnTilemap());

        List<VehicleMovement> movements = new ArrayList<>();

        Collections.sort(activeVehicles, (v1, v2) -> {
            if(v1.isHasWaitedInLastRound() && !v2.isHasWaitedInLastRound()){
                return -1;
            }
            // -1 - less than, 1 - greater than, 0 - equal
            return v1.getSpeed() < v2.getSpeed() ? -1 : (v1.getSpeed() > v2.getSpeed()) ? 1 : 0;
        });
        System.out.println("activeVehicles: "+activeVehicles);
        List<InstantReservation> reservations = new ArrayList<>();
        for(int i=0; i<activeVehicles.size(); i++){
            Vehicle vehicle = activeVehicles.get(i);
            // Für jedes Fahrzeug wird sich die Bewegung für den aktuellen Tag gespeichert
            VehicleMovement movement = vehicle.getMovementForNextDay();
//            PositionOnTilemap lastPosition = movement.getLastPair().getKey();
            Set<InstantReservation> newRes = new HashSet<>();
            List<PositionOnTilemap> allPositions = movement.getAllPositions();
            for(PositionOnTilemap pos: allPositions){
                newRes.add(new InstantReservation(pos.getxCoordinateInGameMap(), pos.getyCoordinateInGameMap(), movement));
            }
            movements.add(movement);
            int numberOfAddedReservations = 0;
            boolean shouldWait = false;
            for(InstantReservation res : newRes){
                if(reservations.contains(res)){
                    VehicleMovement movementThatReservedTile = reservations.get(reservations.indexOf(res)).getMovement();
                    boolean directionsContrawise = checkIfDirectionsContrawise(movementThatReservedTile.getDirectionOfLastMove(),
                            movement.getDirectionOfLastMove());
                    if(!directionsContrawise){
                        //Dann soll sich das Auto nicht bewegen, da das Tile schon besetzt ist
                        shouldWait = true;
                    }
                    else{
                        System.out.println("############Bewegung in entgegengesetzte Richtung entdeckt##########");
                    }
                }
            }
            if(shouldWait){
                PositionOnTilemap startPos = movement.getStartPosition();
                reservations.add(new InstantReservation(startPos.getxCoordinateInGameMap(), startPos.getyCoordinateInGameMap(), movement));
                vehicle.revertMovementForNextDay();
                movement.setWait(true);
                vehicle.setHasWaitedInLastRound(true);
            }
            else {
                reservations.addAll(newRes);
                // Die Startposition für den nächsten tag ist die letzte Position des aktuellen Tages
                vehicle.setPosition(movement.getLastPair().getKey());
                vehicle.setHasWaitedInLastRound(false);
            }
        }

        day++;
        return movements;
    }

    /**
     * Gibt Vehicle-Objekte zurück, die zu dem angegebenen TrafficType passen. Aus diesen Vehicle-Objekten können
     * dann Instanzen über getNewInstance() erzeugt werden.
     * @param type
     * @return
     */
    public List<Vehicle> getVehicleTypesForName(TrafficType type){
        List<Vehicle> desiredVehicles = new ArrayList<>();
        for(Vehicle v: vehiclesTypes){
            //TODO hier wird manchmal eine exception geworfen. Warum?
            System.out.println("Typ eines Vehicles : "+v.getKind());
            if(v.getKind().equals(type)){
                desiredVehicles.add(v);
            }
        }
        return desiredVehicles;
    }

    private boolean checkIfDirectionsContrawise(int[] d1, int[] d2){
        boolean contrawise = false;
        if(d1[0] == d2[0] && d1[0] == 0){
            if(Math.abs(d1[1] - d2[1]) == 2) contrawise = true;
        }
        else if(Math.abs(d1[0] - d2[0]) == 2){
            if(d1[1] == d2[1] && d1[1] == 0) contrawise = true;
        }
        System.out.println("checkIfDirectionsContrawise "+contrawise);
        return contrawise;
    }

    /**
     * Gibt ein Vehicle-Objekt zurück, das zu dem angegebenen Name passt. Aus diesen Vehicle-Objekt können
     * dann Instanzen über getNewInstance() erzeugt werden.
     * @return
     */
    public Vehicle getVehicleTypesForName(String name){
        for(Vehicle v: vehiclesTypes){
            if(v.getGraphic().equals(name)){
                return v;
            }
        }
        return null;
    }

    /**
     * @param buildmenu
     * @return Eine Liste von buildings, die alle das buildmenu haben
     */
    public List<Building> getBuildingsForBuildmenu(String buildmenu) {
        List<Building> bs = new ArrayList<>();

        for (Building building : buildings) {
            String menu = building.getBuildmenu();
            if (menu != null && menu.equals(buildmenu)) {
                bs.add(building);
            }
        }
        return bs;
    }

    /**
     * Erzeugt eine Liste mit Elementen des Typs Road aus den Buildings im Model
     *
     * @return eine Liste mit Elementen des Typs Road
     */
    public List<Road> getRoadsFromBuildings() {

        List<Road> roads = new ArrayList<>();
        for (Building building : buildings) {
            if (building.getBuildingName() != null && building.getBuildingName().contains("road")) {
                roads.add((Road) building);
            }
        }
        return roads;
    }


    /**
     *
     * @param special
     * @return Eine Liste von buildings, die alle die Specialfunktion special haben
     */
    public List<Special> getBuildingsForSpecialUse(String special) {
        List<Special> bs = new ArrayList<>();

        for (Building building : buildings) {
            if (building instanceof Special && ((Special) building).getSpecial().equals(special)) {
                bs.add((Special) building);
            }
        }
        return bs;
    }

    /**
     * Überprüft, ob das zu platzierende Straßenfeld mit dem ausgewählten Straßenfeld auf der Map Feld kombiniert
     * werden kann. Falls dies der Fall ist, wird ein neues building-objekt erzeugt und zurückgegeben, ansonsten wird
     * das selbe building Objekt zurückgegeben
     * @param xCoord x-Koordinate des angeklickten Feldes, auf das die Straße gebaut werden soll
     * @param yCoord y-Koordinate des angeklickten Feldes, auf das die Straße gebaut werden soll
     */
    public Building checkCombines(int xCoord, int yCoord, Building sBuilding) {

        Tile selectedTile = getMap().getTileGrid()[xCoord][yCoord];
        Building buildingOnSelectedTile = selectedTile.getBuilding();
        Map<String, String> combinations = null;
        if (sBuilding instanceof Road) {
            combinations = ((Road) sBuilding).getCombines();
        }
        else if(sBuilding instanceof Rail) {
            combinations = ((Rail) sBuilding).getCombines();
        }
        if(combinations != null){
            for (Map.Entry<String, String> entry : combinations.entrySet()) {
                if (buildingOnSelectedTile.getBuildingName().equals(entry.getKey())) {
                    String newBuildingName = entry.getValue();

//                    System.out.println(sBuilding.getBuildingName() + " and " +
//                            buildingOnSelectedTile.getBuildingName() + " can be combined to " + newBuildingName);
                    Building combinedBuilding = getBuildingByName(newBuildingName).getNewInstance();
                    // Wenn eine Kombination einmal gefunden wurde, soll nicht weiter gesucht werden
                    return combinedBuilding;
                }
            }
        }
        return sBuilding;
    }

    /**
     *
     * @param name
     * @return Einen Building-Typ, von dem noch eine Instanz erzeugt werden muss
     */
    public Building getBuildingByName(String name){
        for (Building building : buildings) {
            if (building.getBuildingName().equals(name)) {
                return building;
            }
        }
        return null;
    }

    public List<Vehicle> getVehiclesTypes() {
        return vehiclesTypes;
    }

    public void setVehiclesTypes(List<Vehicle> vehiclesTypes) {
        this.vehiclesTypes = vehiclesTypes;
    }

    public void addCommodities(List<String> commodities) {
        this.commodities.addAll(commodities);
    }

    public Set<String> getCommodities() {
        return commodities;
    }

    public void setCommodities(Set<String> commodities) {
        this.commodities = commodities;
    }

//    public List<Reservation> getReservations() {
//        return reservations;
//    }
//
//    public void setReservations(List<Reservation> reservations) {
//        this.reservations = reservations;
//    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public Pathfinder getPathfinder() {
        return pathfinder;
    }

    public void setPathfinder(Pathfinder pathfinder) {
        this.pathfinder = pathfinder;
    }

    public double getSpeedOfDay() {
        return speedOfDay;
    }

    public void setSpeedOfDay(double speedOfDay) {
        this.speedOfDay = speedOfDay;
    }

    public MapModel getMap() {
        return map;
    }

    public void setMap(MapModel map) {
        this.map = map;
    }

    public Queue<ConnectedTrafficPart> getNewCreatedOrIncompleteTrafficParts() {
        return newCreatedOrIncompleteConnectedTrafficParts;
    }

    public void setNewCreatedOrIncompleteTrafficLines(Queue<ConnectedTrafficPart> newCreatedOrIncompleteConnectedTrafficParts) {
        this.newCreatedOrIncompleteConnectedTrafficParts = newCreatedOrIncompleteConnectedTrafficParts;
    }


    //    public ToolsModel getTools() {
//        return tools;
//    }
//
//    public void setTools(ToolsModel tools) {
//        this.tools = tools;
//    }

    public Set<String> getBuildmenus() {
        return buildmenus;
    }

    public void setBuildmenus(Set<String> buildmenus) {
        this.buildmenus = buildmenus;
    }

    public String getGamemode() {
        return gamemode;
    }

    public void setGamemode(String gamemode) {
        this.gamemode = gamemode;
    }

    public List<Building> getBuildings() {
        return buildings;
    }

    public void setBuildings(List<Building> buildings) {
        this.buildings = buildings;
    }

    public Tile[][] getFieldGridOfMap() {
        return map.getTileGrid();
    }

    public List<ConnectedTrafficPart> getActiveTrafficParts() {
        return activeConnectedTrafficParts;
    }

    public void printModelAttributes() {
        System.out.println("Model attributes: ");
        System.out.print("Commodities: ");
        for (String commodity : commodities) {
            System.out.print(commodity + ", ");
        }
        System.out.println();
        System.out.println("Day: " + day);
        System.out.println("Speed of day: " + speedOfDay);
        System.out.println("Map with following attributes:\n    Width: " + map.getWidth() + "\n    Depth: " + map.getDepth());
        System.out.println("Gamemode: " + gamemode);
        System.out.print("Buildings: ");
        for (Building building : buildings) {
            System.out.print(building.getBuildingName() + ", ");
        }
        System.out.println("");
        System.out.print("Vehicles: ");
        for (Vehicle vehicle : vehiclesTypes) {
            System.out.print(vehicle.getGraphic() + ", ");
        }
    }
}
