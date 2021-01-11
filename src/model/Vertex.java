package model;

import java.util.List;

public class Vertex {
    private String name;
    private double xCoordinateRelativeToTileOrigin;
    private double yCoordinateRelativeToTileOrigin;

    // TODO: Funktion zum Umrechnen der Koordinaten

    // Liste von Buildings, die diesen Punkt haben
    private List<Building> buildings;
    // Liste von TrafficRoutes, die diesen Punkt beinhalten
    private List<TrafficRoute> trafficRoutes;

    public String getName() {
        return name;
    }
}
