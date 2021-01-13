package model;

import java.util.List;
import java.util.Objects;

// Entspricht sozusagen einem Waypoint aus Vorprojekt, außerdem dürfen Fahrzeuge auf einer Station halten

public class Station {
    // Bauwerke, aus denen eine Haltestelle besteht (z.B. dürfen ein Bahnhof und eine Bushaltestelle gemeinsam eine
    // Station bilden, um Warenumstieg zu ermöglichen. Eine Station kann aber auch nur aus einem Bauwerk bestehen
    private List<Building> components;

    private String id;

    public Station(String id, List<Building> components) {
        this.id = id;
        this.components = components;
    }

    //Die Methoden equals() und hashCode() gehen davon aus, dass die id eines Knotens unique ist
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Station station = (Station) o;
        return id.equals(station.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
