package model;

import java.util.ArrayList;
import java.util.List;

public class TrafficLine {

    private List<Station> stations = new ArrayList<>();
    private int desiredNumberOfVehicles;
    private List<Vehicle> vehicles = new ArrayList<>();

    public TrafficLine(int desiredNumberOfVehicles) {
        this.desiredNumberOfVehicles = desiredNumberOfVehicles;
    }

    private boolean checkIfMoreThanOneStation(){
        return stations.size() >= 2;
    }

    public List<Station> getStations() {
        return stations;
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

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }
}
