package model;

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
    private Queue<TrafficLine> newCreatedOrIncompleteTrafficLines = new ArrayDeque<>();

    // Alle Verkehrslinien mit mehr als einer Station, die schon Verkehrsmittel auf sich fahren haben sollten
    private List<TrafficLine> activeTrafficLines = new ArrayList<>();

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
     * @return eine Liste von aktiven Fahrzeugen zurück
     */
    public List<VehicleMovement> simulateOneDay(){

        // In der Zeit einer Runde, also seit dem letzten Aufruf dieser Methode, können Haltestellen platziert worden
        // sein, die zu neuen, unverbundenen Stationen führen. Eine unverbundene Station stellt erstmal eine neue
        // Verkehrslinie dar. Diese neue Verkehrslinie wurde der Queue newCreatedOrIncompleteTrafficLines hinzugefügt
        List<TrafficLine> incompleteTrafficLines = new ArrayList<>();
        while(!newCreatedOrIncompleteTrafficLines.isEmpty()){
            TrafficLine newOrIncompleteTrafficLine = newCreatedOrIncompleteTrafficLines.remove();

            if(newOrIncompleteTrafficLine.checkIfMoreThanOneStation()){
                if(newOrIncompleteTrafficLine.getTrafficType().equals(TrafficType.ROAD)){
                    activeTrafficLines.add(newOrIncompleteTrafficLine);

                    //TODO Andere TrafficTypes fehlen noch
                }
            }
            // Eine Station, die nur eine Station hat, ist eine unfertige Verkehrslinie
            else {
                incompleteTrafficLines.add(newOrIncompleteTrafficLine);
            }
        }
        newCreatedOrIncompleteTrafficLines.addAll(incompleteTrafficLines);

        //TODO Es funktioniert, wenn eine Station direkt an eine Verkehrslinie gebaut wird. Es funkltioniert noch nicht,
        // wenn zwei Stationen erst im Nachhinein mit Straßen verbunden werden

        List<Vehicle> activeVehicles = new ArrayList<>();
        for(TrafficLine activeLine: activeTrafficLines){
            if(activeLine.getDesiredNumberOfVehicles() > activeLine.getVehicles().size()){
                activeLine.addNewVehicle();
            }
            activeVehicles.addAll(activeLine.getVehicles()); //TODO
        }
        System.out.println("activeTrafficLines "+activeTrafficLines);

        List<VehicleMovement> movements = new ArrayList<>();
        for(Vehicle vehicle :activeVehicles){
            VehicleMovement movement = vehicle.getMovementForNextDay();
            movements.add(movement);
            vehicle.setPosition(movement.getLastPair().getKey());
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
    public List<Vehicle> getVehicleTypesForTrafficType(TrafficType type){
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
                    Building combinedBuilding = getBuildingByName(newBuildingName);
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
     * @return Eine Liste von buildings, die alle den Namen name haben
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

    public Queue<TrafficLine> getNewCreatedOrIncompleteTrafficLines() {
        return newCreatedOrIncompleteTrafficLines;
    }

    public void setNewCreatedOrIncompleteTrafficLines(Queue<TrafficLine> newCreatedOrIncompleteTrafficLines) {
        this.newCreatedOrIncompleteTrafficLines = newCreatedOrIncompleteTrafficLines;
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

    public List<TrafficLine> getActiveTrafficLines() {
        return activeTrafficLines;
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
