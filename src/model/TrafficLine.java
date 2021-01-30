package model;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;

public class TrafficLine {

    protected List<Station> stations = new ArrayList<>();
    protected int desiredNumberOfVehicles;
    protected List<Vehicle> vehicles = new ArrayList<>();
    protected BasicModel model;

    public TrafficLine(int desiredNumberOfVehicles) {
        this.desiredNumberOfVehicles = desiredNumberOfVehicles;
    }

    public boolean checkIfMoreThanOneStation(){
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
