package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Stellt einen Teil des Verkehrsnetzes dar, der verbunden ist und von einem bestimmten TrafficType ist
 */
public class ConnectedTrafficPart {

    private List<Station> stations = new ArrayList<>();
    private BasicModel model;
    private TrafficType trafficType;
    private List<TrafficLine> trafficLines = new ArrayList<>();

    public ConnectedTrafficPart(BasicModel model, TrafficType trafficType, Station firstStation) {
        this.model = model;
        this.trafficType = trafficType;
        stations.add(firstStation);
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
    }

    /**
     * Fügt diesen Verkehrsteil und den angegebenen Verkehrsteil zu einem Verkehrsteil zusammen
     * @param otherPart
     */
    public void mergeWithTrafficPart(ConnectedTrafficPart otherPart){
        if(!otherPart.getTrafficType().equals(trafficType)) throw new IllegalArgumentException("Tried to merge parts " +
                "of different trafficTypes");
        System.out.println("Stations "+stations);
        for(Station otherStation: otherPart.getStations()){
            otherStation.setRoadTrafficPart(this);
            otherStation.updateDirectlyConnectedStations();
            stations.add(otherStation);
        }
        System.out.println("Stations "+stations);
        trafficLines.addAll(otherPart.getTrafficLines());
    }

    public void setStations(List<Station> stations) {
        this.stations = stations;
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

    public TrafficType getTrafficType() {
        return trafficType;
    }

    public void setTrafficType(TrafficType trafficType) {
        this.trafficType = trafficType;
    }

    public List<TrafficLine> getTrafficLines() {
        return trafficLines;
    }

    public void setTrafficLines(List<TrafficLine> trafficLines) {
        this.trafficLines = trafficLines;
    }
}
