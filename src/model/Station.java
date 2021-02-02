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

    private List<Station> directlyConnectedStations = new ArrayList<>();


    public Station(BasicModel model, TrafficLine roadTrafficLine,  TrafficLine railTrafficLine, TrafficLine airTrafficLine) {
        // Stations haben unendliche Lagerkapazität
        Map<String, Integer> maximumCargo = new HashMap<>();
        for(String commodity: model.getCommodities()) {
            maximumCargo.put(commodity, Integer.MAX_VALUE);
        }
        storage = new Storage(maximumCargo);

        this.roadTrafficLine = roadTrafficLine;
        this.railTrafficLine = railTrafficLine;
        this.airTrafficLine = airTrafficLine;
    }

    public boolean addBuilding(Stop building){
        building.setStation(this);
        if(building instanceof Tower){
            int maxplanes = ((Tower) building).getMaxplanes();
            if(maxplanes > this.maxPlanes) this.maxPlanes = maxplanes;
        }
        return components.add(building);
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

    public List<Station> getDirectlyConnectedStations() {
        return directlyConnectedStations;
    }

    public void setDirectlyConnectedStations(List<Station> directlyConnectedStations) {
        this.directlyConnectedStations = directlyConnectedStations;
    }
}
