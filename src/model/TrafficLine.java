package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TrafficLine {

    private List<Station> stations = new ArrayList<>();
    private Map<Vehicle, Integer> desiredNumbersOfVehicles;
    private int totalDesiredNumbersOfVehicles;
    private List<Vehicle> vehicles = new ArrayList<>();
    private BasicModel model;
    private TrafficType trafficType;
    private String name;

    // Der Knoten an dem neue Fahrzeuge starten
    private Vertex startVertexForNewVehicles;
    // Die Station an der neue Fahrzeuge starten
    private Station startStation;

    public TrafficLine(BasicModel model, TrafficType trafficType, List<Station> stations,
                       Map<Vehicle, Integer> desiredNumbersOfVehicles, String name) {
        this.model = model;
        this.trafficType = trafficType;
        this.stations = stations;
        this.name = name;
        setDesiredNumbersOfVehicles(desiredNumbersOfVehicles);

        System.out.println("Stations in TrafficLine: "+stations);

        if(trafficType.equals(TrafficType.ROAD)){
            for(Station station : stations){
                station.setRoadTrafficLine(this);
            }
        }
        else if(trafficType.equals(TrafficType.AIR)){
            for(Station station : stations){
                station.setAirTrafficLine(this);
            }
        }

        else if(trafficType.equals(TrafficType.RAIL)){
            for(Station station : stations){
                station.setRailTrafficLine(this);
            }
        }
    }

    //TODO Wenn Vehicles kurz vor der station sind, werden sie schneller. Was ist da los?

    /**
     * Soll eine neues Fahrzeug zu der Liste der Fahzeuge hinzufügen. Gibt das erstellte fahrzeug zurück
     */
    public Vehicle addNewVehicle(Vehicle vehicle){

        if(startVertexForNewVehicles == null) throw new NullPointerException("startVertexForNewVehicles was null");
        int rowInTileGrid = startVertexForNewVehicles.getxCoordinateInGameMap();
        int columnInTileGrid = startVertexForNewVehicles.getyCoordinateInGameMap();

        double shiftToDepthInOneTile = startVertexForNewVehicles.getxCoordinateRelativeToTileOrigin();
        double shiftToWidthInOneTile = startVertexForNewVehicles.getyCoordinateRelativeToTileOrigin();
        VehiclePosition position = new VehiclePosition(shiftToWidthInOneTile, shiftToDepthInOneTile,
                rowInTileGrid, columnInTileGrid);

        vehicle.setPathfinder(model.getPathfinder());
        vehicle.setPosition(position);
        vehicle.setNextStation(stations.get(0));
        if (trafficType != TrafficType.AIR) {
            vehicle.updateNextStation();
        }

        vehicle.savePathToNextStation(startVertexForNewVehicles);

        vehicles.add(vehicle);
        System.out.println(vehicle.getTrafficType());
        System.out.println("Speed of new vehicle"+vehicle.getSpeed());
        return vehicle;
    }

    /**
     * Sortiert die Liste stations in eine Reihenfolge von der startStation zu einer End-Station
     */
    private void sortStationsAsPathFromStartStationToLastStation(){
        List<Station> sortedStations = new ArrayList<>();
        sortedStations.add(startStation);

        for(int i=0; i<sortedStations.size(); i++){
            for(Station unsorted : stations){
                if(sortedStations.get(i).isDirectlyConnectedTo(unsorted, trafficType)){
                    if(! sortedStations.contains(unsorted)){
                        sortedStations.add(unsorted);
                    }
                }
            }
        }
        stations = sortedStations;

        System.out.println("sorted Stations");
        stations.forEach(x -> System.out.println("station "+x.getId()));
    }


    /**
     * Gib die nächste Station aus der Liste zurück, für die angegebene Station. Deshalb ist die Liste sortiert
     * @param previousStation
     * @param forwardMovement Ob das fahrzeug die Liste momentan vorwärts oder rückwärts durchläuft
     * @param vehicle
     * @return
     */
    public Station getNextStation(Station previousStation, boolean forwardMovement, Vehicle vehicle){
        int indexOfPreviousStation = stations.indexOf(previousStation);
        int indexOfNextStation;
        if(indexOfPreviousStation == 0){
            indexOfNextStation = 1;
            // Set forward movement
            vehicle.setMovementInTrafficLineGoesForward(true);
        }
        else if(indexOfPreviousStation == stations.size()-1){
            indexOfNextStation = stations.size()-2;
            vehicle.setMovementInTrafficLineGoesForward(false);
        }
        else {
            if(forwardMovement){
                indexOfNextStation = indexOfPreviousStation+1;
            }
            else indexOfNextStation = indexOfPreviousStation-1;
        }
        return stations.get(indexOfNextStation);
    }

    /**
     * Setzt den Anfangsknoten aus dem Graph für neu hinzugefügte Fahrzeuge. Setzt außerdem die zugehörige Anfangsstation
     */
    public void setStartVertexAndStartStationForNewVehicles(){

        // Finde Station an der das Auto platziert werden soll
        Station startStation = null;
        // Die Zahl ist so groß, damit die erste Station sicher temporär zu der Station mit den minimalen Verbindungen wird
        int minimalNumberOfConnectedStations = Integer.MAX_VALUE;
        // Finde die Station, die am wenigsten Verbindungen hat
        for(Station station: stations){
            int numberOfConnectedStations = station.getDirectlyConnectedStations(trafficType).size();
            // Wenn die Station nur eine direkt verbundene Station hat bzw diese Zahl minimal ist, dann kann sie als Start taugen
            if( numberOfConnectedStations < minimalNumberOfConnectedStations){
                startStation = station;
                minimalNumberOfConnectedStations = numberOfConnectedStations;
            }
        }
        if(startStation == null){
            throw new RuntimeException("No startStation found in setStartVertexForNewVehicles() in Class TrafficLine");
        }

        // Finde den Knoten um das Auto zu platzieren
        for(Stop stop: startStation.getComponents()){
            if(stop.getTrafficType().equals(trafficType)){
                startVertexForNewVehicles = stop.getVertices().iterator().next();
                break;
            }
        }
        this.startStation = startStation;

        //TODO eventuell unfertig?
    }

    public Vehicle getMissingVehicleOrNull(){
        for (Map.Entry<Vehicle, Integer> entry : desiredNumbersOfVehicles.entrySet()) {
            if(getNumberOfVehicleInstances(entry.getKey().getGraphic()) < entry.getValue()){
                System.out.println("tried to add new Vehicle : "+entry.getKey().getGraphic());
                return entry.getKey();
            }
        }
        return null;
    }

    private int getNumberOfVehicleInstances(String vehicleName){
        int number = 0;
        for(Vehicle v: vehicles){
            if(v.getGraphic().equals(vehicleName)) number++;
        }
        return number;
    }


    public List<Station> getStations() {
        return stations;
    }

    public void setStations(List<Station> stations) {
        this.stations = stations;
    }

    public Integer getDesiredNumberOfVehiclesForVehicle(Vehicle vehicle) {
        return desiredNumbersOfVehicles.get(vehicle);
    }

    public void setDesiredNumbersOfVehicles(Map<Vehicle, Integer> desiredNumbersOfVehicles) {
        this.desiredNumbersOfVehicles = desiredNumbersOfVehicles;
        int total = 0;
        for (Map.Entry<Vehicle, Integer> entry : desiredNumbersOfVehicles.entrySet()) {
            total+=entry.getValue();
        }
        totalDesiredNumbersOfVehicles = total;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public TrafficType getTrafficType() {
        return trafficType;
    }

    public void setTrafficType(TrafficType trafficType) {
        this.trafficType = trafficType;
    }

    public Vertex getStartVertexForNewVehicles() {
        return startVertexForNewVehicles;
    }

    public void setStartVertexForNewVehicles(Vertex startVertexForNewVehicles) {
        this.startVertexForNewVehicles = startVertexForNewVehicles;
    }

    public Station getStartStation() {
        return startStation;
    }

    public void setStartStation(Station startStation) {
        this.startStation = startStation;
    }

    public int getTotalDesiredNumbersOfVehicles() {
        return totalDesiredNumbersOfVehicles;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
