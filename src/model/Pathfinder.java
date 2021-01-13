package model;

import java.util.ArrayList;
import java.util.List;

// Die Wegfindung wird in die Klasse Pathfinder ausgelagert, die einen Graph mit seinen Knoten bekommt
// Die einzige Aufgabe von Pathfinder ist also die Wegsuche entsprechend dem gewählen Verkehrsmittel
public class Pathfinder {
    private TrafficGraph trafficGraph;

    public Pathfinder(TrafficGraph trafficGraph) {
        this.trafficGraph = trafficGraph;
    }

    // Je nach Verkehrsmittel ändert sich die Art der Wegfindung, dazu soll Pathfinder verschiedene Methoden anbieten
    // TODO Wegfindung für unterschiedliche Verkehrsmittel umsetzen (alternative Umsetzung evtl. mit Vererbung,
    //  z.B planePathfinder, roadVehiclePathfinder anstatt mit Methoden in einer einzigen Klasse)

    public List<Vertex> findPathForRoadVehicle(Vertex startVertex, Vertex destinationVertex){
        return new ArrayList<>();
    };

    public List<Vertex> findPathForRoadRailway(Vertex startVertex, Vertex destinationVertex){
        return new ArrayList<>();
    };

    public List<Vertex> findPathForPlane(Vertex startVertex, Vertex destinationVertex){
        return new ArrayList<>();
    };
}
