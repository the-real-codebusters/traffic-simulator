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

    public Station(BasicModel model) {
        // Stations haben unendliche Lagerkapazität
        Map<String, Integer> maximumCargo = new HashMap<>();
        for(String commodity: model.getCommodities()) {
            maximumCargo.put(commodity, Integer.MAX_VALUE);
        }
        storage = new Storage(maximumCargo);
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
}
