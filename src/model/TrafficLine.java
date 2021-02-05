package model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class TrafficLine {

    protected List<Station> stations = new ArrayList<>();
    protected int desiredNumberOfVehicles;
    protected List<Vehicle> vehicles = new ArrayList<>();
    protected BasicModel model;
    private Vertex startVertexForNewVehicles;
    private TrafficType trafficType;
    private Station startStation;

    public TrafficLine(int desiredNumberOfVehicles, BasicModel model, TrafficType trafficType) {
        this.desiredNumberOfVehicles = desiredNumberOfVehicles;
        this.model = model;
        this.trafficType = trafficType;
    }

    /**
     * Soll eine neues Fahrzeug zu der Liste der Fahzeuge hinzufügen
     */
    public void addNewVehicle(){

        // TODO Es wird bisher einfach zufällig ein Fahrzeugtyp ausgewählt, eventuell sollte das mal kompklexer werden
        List<Vehicle> vehicleTypes = model.getVehicleTypesForTrafficType(trafficType);
        Random randomGenerator = new Random();
        int randomInt = randomGenerator.nextInt(vehicleTypes.size()) -1;
        Vehicle vehicle = vehicleTypes.get(randomInt).getNewInstance();

        if(startVertexForNewVehicles == null) throw new NullPointerException("startVertexForNewVehicles was null");
        int rowInTileGrid = startVertexForNewVehicles.getxCoordinateInGameMap();
        int columnInTileGrid = startVertexForNewVehicles.getyCoordinateInGameMap();

        Tile startTile = model.getMap().getTileGrid()[rowInTileGrid][columnInTileGrid];
        double shiftToDepthInOneTile = startVertexForNewVehicles.getxCoordinateRelativeToTileOrigin();
        double shiftToWidthInOneTile = startVertexForNewVehicles.getyCoordinateRelativeToTileOrigin();
        VehiclePosition position = new VehiclePosition(startTile, shiftToWidthInOneTile, shiftToDepthInOneTile);
        vehicle.setPosition(position);


        vehicles.add(vehicle);
        System.out.println(vehicle.getKind());
        System.out.println("Speed "+vehicle.getSpeed());
    }

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
        stations.add(station);

        // Mache eine Breitensuche auf dem Graph um alle direkt verbundenen Stationen zu finden
        List<Station> nextStations = model.getPathfinder().findAllDirectlyConnectedStations(station);
        System.out.println("Connected Stations for Station "+station.getId());
        for(Station n: nextStations){
            System.out.println("Next Station "+n.getId());
            n.getDirectlyConnectedStations().add(station);
        }
        station.setDirectlyConnectedStations(nextStations);
        setStartVertexAndStartStationForNewVehicles();
        sortStationsAsPathFromStartStationToLastStation();
    }

    /**
     * Setzt den Anfangsknoten aus dem Graph für neu hinzugefügte Fahrzeuge
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
