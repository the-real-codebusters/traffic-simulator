package model;

public class Pathfinder {
    private Vertex startVertex;
    private Vertex destinationVertex;

    public Pathfinder(Vertex startVertex, Vertex destinationVertex) {
        this.startVertex = startVertex;
        this.destinationVertex = destinationVertex;
    }

    // TODO Wegfindung für unterschiedliche Verkehrsmittel umsetzen (evtl. mit Vererbung,
    //  z.B planePathfinder, roadVehiclePathfinder mit eigenen Methoden für Wegfindung)
}
