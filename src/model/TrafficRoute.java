package model;

import java.util.List;

public class TrafficRoute {
    // Entspricht sozusagen der Liste der abzuarbeitenden Waypoints aus Vorprojekt, die jetzt vom Typ Station sind
    // Stationen können Schnittstellen zwischen Verkehrslinien mit verschiedenen Verkehrsmitteln sein
    private List<Station> stations;
    // Bestimmt welcher Verkehrstyp diese Linie benutzen darf
    private String vehicleType;
    private int desiredNumberOfVehicles;
    private List<Vehicle> vehicles;
    // Knoten, aus denen die Route besteht
    private List<Vertex> vertexes;
}
