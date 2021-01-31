package model;

import java.util.ArrayList;
import java.util.List;

public class TrafficLine {

    protected List<Station> stations = new ArrayList<>();
    protected int desiredNumberOfVehicles;
    protected List<Vehicle> vehicles = new ArrayList<>();
    protected BasicModel model;
    private Vertex startVertexForNewVehicles;
    private TrafficType trafficType;

    public TrafficLine(int desiredNumberOfVehicles, BasicModel model, TrafficType trafficType) {
        this.desiredNumberOfVehicles = desiredNumberOfVehicles;
        this.model = model;
        this.trafficType = trafficType;
    }

    public boolean checkIfMoreThanOneStation(){
        return stations.size() >= 2;
    }

    public List<Station> getStations() {
        return stations;
    }

    public void addStationAndUpdateConnectedStations(Station station){
        stations.add(station);

        // Mache eine Breitensuche auf dem Graph um alle direkt verbundenen Stationen zu finden
        List<Station> nextStations = model.getPathfinder().findAllDirectlyConnectedStations(station);
        System.out.println("Connected Stations for Station "+station.getId());
        for(Station n: nextStations){
            System.out.println("Next Station "+n.getId());
        }
        station.setDirectlyConnectedStations(nextStations);
    }

    private void setStartVertexForNewVehicles(){
        Station startStation = null;
        int minimalNumberOfConnectedStations = 100000;
        for(Station station: stations){
            int numberOfConnectedStations = station.getDirectlyConnectedStations().size();
            // Wenn die Station nur eine direkt verbundene Station hat, dann kann sie als Start taugen
            if( numberOfConnectedStations < minimalNumberOfConnectedStations){
                startStation = station;
                minimalNumberOfConnectedStations = numberOfConnectedStations;
            }
        }
        if(startStation == null){
            throw new RuntimeException("No startStation found in setStartVertexForNewVehicles() in Class TrafficLine");
        }

        for(Stop stop: startStation.getComponents()){
            if(stop.getTrafficType().equals(trafficType)){
                startVertexForNewVehicles = stop.getVertices().get(0);
                break;
            }
        }
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
}
