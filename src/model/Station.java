package model;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

// Entspricht sozusagen einem Waypoint aus Vorprojekt, außerdem dürfen Fahrzeuge auf einer Station halten

public class Station {
    // Bauwerke, aus denen eine Haltestelle besteht (z.B. dürfen ein Bahnhof und eine Bushaltestelle gemeinsam eine
    // Station bilden, um Warenumstieg zu ermöglichen. Eine Station kann aber auch nur aus einem Bauwerk bestehen
    private List<Stop> components = new ArrayList<>();

    private Storage storage;

    private static final AtomicLong NEXT_ID = new AtomicLong(0);
    private final long id = NEXT_ID.getAndIncrement();
    private int maxPlanes = 0;
    private TrafficLine roadTrafficLine;
    private TrafficLine railTrafficLine;
    private TrafficLine airTrafficLine;

    private ConnectedTrafficPart roadTrafficPart;
    private ConnectedTrafficPart airTrafficPart;
    private ConnectedTrafficPart railTrafficPart;



    private Set<Station> directlyRoadConnectedStations = new HashSet<>();
    private Set<Station> directlyRailConnectedStations = new HashSet<>();
    private Set<Station> directlyAirConnectedStations = new HashSet<>();

    private Pathfinder pathfinder;


    public Station(BasicModel model, TrafficLine roadTrafficLine, TrafficLine railTrafficLine,
                   TrafficLine airTrafficLine, Pathfinder pathfinder, ConnectedTrafficPart roadTrafficPart,
                   ConnectedTrafficPart railTrafficPart, ConnectedTrafficPart airTrafficPart) {
        // Stations haben unendliche Lagerkapazität
        Map<String, Integer> maximumCargo = new HashMap<>();
        for(String commodity: model.getCommodities()) {
            maximumCargo.put(commodity, Integer.MAX_VALUE);
        }
        storage = new Storage(maximumCargo);

        this.roadTrafficLine = roadTrafficLine;
        this.railTrafficLine = railTrafficLine;
        this.airTrafficLine = airTrafficLine;
        this.pathfinder = pathfinder;
        this.roadTrafficPart = roadTrafficPart;
        this.railTrafficPart = railTrafficPart;
        this.airTrafficPart = airTrafficPart;
    }

    public Station(Pathfinder pathfinder, BasicModel model) {
        // Stations haben unendliche Lagerkapazität
        Map<String, Integer> maximumCargo = new HashMap<>();
        for(String commodity: model.getCommodities()) {
            maximumCargo.put(commodity, Integer.MAX_VALUE);
        }
        storage = new Storage(maximumCargo);

        this.pathfinder = pathfinder;
    }

    /**
     * Gibt zurück, ob die Station mit der angegebenen Station direkt durch Straßen verbunden ist
     * //TODO Sollte auch für Rail und AIR funktionieren ?
     * @param station
     * @return
     */
    public boolean isDirectlyConnectedTo(Station station, TrafficType trafficType){
        if(trafficType.equals(TrafficType.ROAD)){
            return directlyRoadConnectedStations.contains(station);
        }
        else if(trafficType.equals(TrafficType.RAIL)){
            return directlyRailConnectedStations.contains(station);
        }
        else if(trafficType.equals(TrafficType.AIR)){
            return directlyAirConnectedStations.contains(station);
        }
        else throw new IllegalArgumentException("Argument in isDirectlyConnectedTo was "+trafficType);
    }

    public TrafficLine getTrafficLineForTrafficType(TrafficType trafficType){
        if(trafficType.equals(TrafficType.ROAD)) return roadTrafficLine;
        if(trafficType.equals(TrafficType.RAIL)) return railTrafficLine;
        if(trafficType.equals(TrafficType.AIR)) return airTrafficLine;
        throw new RuntimeException("Unklarer Verkehrstyp in getTrafficLineForTrafficType");
    }

    /**
     * Setzt die Variable directlyConnectedStations für alle durch Straßen verbundenen Stationen.
     */
    public void updateDirectlyConnectedStations(TrafficType trafficType){
        // Mache eine Breitensuche auf dem Graph um alle direkt verbundenen Stationen zu finden
        List<Station> nextStations = pathfinder.findAllDirectlyConnectedStations(this, trafficType);
        System.out.println("Connected Stations for Station "+this.getId());
        for(Station n: nextStations){
            System.out.println("Next Station "+n.getId());
            n.getDirectlyConnectedStations(trafficType).add(this);
        }
        setDirectlyConnectedStations(nextStations, trafficType);
    }

//    public void updateDirectlyConnectedStationsForRunway(Station start){
//        // Mache eine Breitensuche auf dem Graph um alle direkt verbundenen Stationen zu finden
//        List<Station> nextStations = pathfinder.findPathForPlane(start, this);
//        System.out.println("Connected Stations for Station "+this.getId());
//        for(Station n: nextStations){
//            System.out.println("Next Station "+n.getId());
//            n.getDirectlyConnectedStations().add(this);
//        }
//        setDirectlyConnectedStations(nextStations);
//    }

    /**
     * Fügt der Station eine Haltestelle hinzu und setzt in der Haltestelle diese Station
     * @param building
     * @return
     */
    public boolean addBuildingAndSetStationInBuilding(Stop building){
        building.setStation(this);
        if(building instanceof Tower){
            int maxplanes = ((Tower) building).getMaxplanes();
            if(maxplanes > this.maxPlanes) this.maxPlanes = maxplanes;
        }
        return components.add(building);
    }

    public boolean hasPartOfTrafficType(TrafficType trafficType){
        return getTrafficPartForTrafficType(trafficType) != null;
    }

    public ConnectedTrafficPart getTrafficPartForTrafficType(TrafficType trafficType){
        if(trafficType.equals(TrafficType.AIR)) return airTrafficPart;
        else if(trafficType.equals(TrafficType.ROAD)) return roadTrafficPart;
        else if (trafficType.equals(TrafficType.RAIL)) return railTrafficPart;
        return null;
    }

    public void setTrafficPartForTrafficType(ConnectedTrafficPart trafficPart, TrafficType trafficType){
        if(trafficType.equals(TrafficType.AIR)) airTrafficPart = trafficPart;
        else if(trafficType.equals(TrafficType.ROAD)) roadTrafficPart = trafficPart;
        else if (trafficType.equals(TrafficType.RAIL)) railTrafficPart = trafficPart;
    }

    //Die Methoden equals() und hashCode() gehen davon aus, dass die id einer Station unique ist
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Station station = (Station) o;
        return id == station.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public List<Stop> getComponents() {
        return components;
    }

    public Storage getStorage() {
        return storage;
    }

    public long getId() {
        return id;
    }

    public ConnectedTrafficPart getAirTrafficPart() {
        return airTrafficPart;
    }

    public void setAirTrafficPart(ConnectedTrafficPart airTrafficPart) {
        this.airTrafficPart = airTrafficPart;
    }

    public Set<Station> getDirectlyConnectedStations(TrafficType type) {
        if(type.equals(TrafficType.ROAD)){
            return directlyRoadConnectedStations;
        }
        else if(type.equals(TrafficType.RAIL)){
            return directlyRailConnectedStations;
        }
        else if(type.equals(TrafficType.AIR)){
            return directlyAirConnectedStations;
        }
        throw new IllegalArgumentException("traffic type was "+type);
    }

    public void setDirectlyConnectedStations(List<Station> directlyConnectedStations, TrafficType type) {
        if(type.equals(TrafficType.ROAD)){
            directlyRoadConnectedStations = new HashSet<>(directlyConnectedStations);
        }
        else if(type.equals(TrafficType.RAIL)){
            directlyRailConnectedStations = new HashSet<>(directlyConnectedStations);
        }
        else if(type.equals(TrafficType.AIR)){
            directlyAirConnectedStations = new HashSet<>(directlyConnectedStations);
        }
        else throw new IllegalArgumentException("traffic type was "+type);
    }

    public ConnectedTrafficPart getRoadTrafficPart() {
        return roadTrafficPart;
    }

    public void setRoadTrafficPart(ConnectedTrafficPart roadTrafficPart) {
        this.roadTrafficPart = roadTrafficPart;
    }

    public TrafficLine getRoadTrafficLine() {
        return roadTrafficLine;
    }

    public void setRoadTrafficLine(TrafficLine roadTrafficLine) {
        this.roadTrafficLine = roadTrafficLine;
    }

    public TrafficLine getRailTrafficLine() {
        return railTrafficLine;
    }

    public void setRailTrafficLine(TrafficLine railTrafficLine) {
        this.railTrafficLine = railTrafficLine;
    }

    public TrafficLine getAirTrafficLine() {
        return airTrafficLine;
    }

    public void setAirTrafficLine(TrafficLine airTrafficLine) {
        this.airTrafficLine = airTrafficLine;
    }

    public ConnectedTrafficPart getRailTrafficPart() {
        return railTrafficPart;
    }

    public void setRailTrafficPart(ConnectedTrafficPart railTrafficPart) {
        this.railTrafficPart = railTrafficPart;
    }
}
