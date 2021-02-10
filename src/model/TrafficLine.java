package model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Stellt einen Teil des Verkehrsnetzes dar, der verbunden ist und von einem bestimmten TrafficType ist
 */
public class TrafficLine {

    protected List<Station> stations = new ArrayList<>();
    protected int desiredNumberOfVehicles;
    protected List<Vehicle> vehicles = new ArrayList<>();
    protected BasicModel model;
    private TrafficType trafficType;

    // Der Knoten an dem neue Fahrzeuge starten
    private Vertex startVertexForNewVehicles;
    // Die Station an der neue Fahrzeuge starten
    private Station startStation;

    public TrafficLine(int desiredNumberOfVehicles, BasicModel model, TrafficType trafficType, Station firstStation) {
        this.desiredNumberOfVehicles = desiredNumberOfVehicles;
        this.model = model;
        this.trafficType = trafficType;
        stations.add(firstStation);
    }

    /**
     * Soll eine neues Fahrzeug zu der Liste der Fahzeuge hinzufügen. Gibt das erstellte fahrzeug zurück
     */
    public Vehicle addNewVehicle(){

        // TODO Es wird bisher einfach zufällig ein Fahrzeugtyp ausgewählt, eventuell sollte das mal komplexer werden
        List<Vehicle> vehicleTypes = model.getVehicleTypesForTrafficType(trafficType);
        Random randomGenerator = new Random();
        int randomInt = randomGenerator.nextInt(vehicleTypes.size());
        Vehicle vehicle = vehicleTypes.get(randomInt).getNewInstance();

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
        System.out.println(vehicle.getKind());
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
                if(sortedStations.get(i).isDirectlyConnectedTo(unsorted)){
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


    public boolean checkIfMoreThanOneStation(){
        return stations.size() >= 2;
    }

    /**
     * Fügt eine Station hinzu und setzt alle direkt verbundenen Stationen.
     * Updatet außerdem den Anfangsknoten für neue Fahrzeuge, falls nötig.
     * Sortiert die Stationen in stations außerdem.
     * @param station
     */
    public void addStationAndUpdateConnectedStations(Station station){
        System.out.println("addStation called");
        stations.add(station);

        station.updateDirectlyConnectedStations();
        setStartVertexAndStartStationForNewVehicles();
        //sortStationsAsPathFromStartStationToLastStation();
    }

    /**
     * Fügt diese verkehrslinie und die angegene verkehrslinie zu einer Verkehrslinie zusammen
     * @param otherLine
     */
    public void mergeWithTrafficLine(TrafficLine otherLine){
        if(!otherLine.getTrafficType().equals(trafficType)) throw new IllegalArgumentException("Tried to merge lines " +
                "of different trafficTypes");
        System.out.println("Stations "+stations);
        for(Station otherStation: otherLine.getStations()){
            otherStation.setRoadTrafficLine(this);
            otherStation.updateDirectlyConnectedStations();
            stations.add(otherStation);
        }
        System.out.println("Stations "+stations);
        desiredNumberOfVehicles += otherLine.getDesiredNumberOfVehicles();
        vehicles.addAll(otherLine.getVehicles());
        setStartVertexAndStartStationForNewVehicles();
        sortStationsAsPathFromStartStationToLastStation();
        System.out.println("Stations "+stations);
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
            int numberOfConnectedStations = station.getDirectlyConnectedStations().size();
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
                startVertexForNewVehicles = stop.getVertices().get(0);
                break;
            }
        }
        this.startStation = startStation;

        //TODO eventuell unfertig?
    }

    public void setStations(List<Station> stations) {
        this.stations = stations;
    }

    public int getDesiredNumberOfVehicles() {
        return desiredNumberOfVehicles;
    }

    public void setDesiredNumberOfVehicles(int desiredNumberOfVehicles) {
        this.desiredNumberOfVehicles = desiredNumberOfVehicles;
    }

    public List<Station> getStations() {
        return stations;
    }

    public BasicModel getModel() {
        return model;
    }

    public void setModel(BasicModel model) {
        this.model = model;
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
}
