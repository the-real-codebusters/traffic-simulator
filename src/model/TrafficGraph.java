package model;

import javafx.geometry.Point2D;

import java.util.*;

/**
 * Klasse zur Darstellung des Vekehrsgraphen mittels Adjazenzliste. Der Graph ist gebildet aus Knotenpunkten und
 * Verbindungen zwischen diesen Punkten. Die Kanten können gerichtet oder ungerichtet sein.
 * Jedes Verkehrsmittel kann einen eigenen Graphen bekommen.
 */
public class TrafficGraph {
    private Map<String, List<Vertex>> adjacencyMap = new LinkedHashMap<>();
    private Map<String, Vertex> mapOfVertexes = new LinkedHashMap<>();


    /**
     * Fügt den Knoten zum Graph hinzu
     * @param vertex
     */
    public boolean addVertex(Vertex vertex) {
        String name = vertex.getName();
        if (!adjacencyMap.containsKey(name)) {
            adjacencyMap.put(name, new ArrayList<>());
            mapOfVertexes.put(name, vertex);
            return true;
        }
        else return false;
    }

    /**
     * Löscht einen Knoten aus dem Graph
     * @param nameOfVertex Name des zu löschenden Knotens
     */
    public void removeVertex(String nameOfVertex) {
        Vertex vertex = mapOfVertexes.get(nameOfVertex);
        adjacencyMap.values().stream().forEach(adjajencyList -> adjajencyList.remove(vertex));
        adjacencyMap.remove(nameOfVertex);
        mapOfVertexes.remove(nameOfVertex);

        System.out.println("Vertex "+nameOfVertex+" removed");
    }


    /**
     * Fügt gerichtete Kante zwischen zwei Knoten zum Graph hinzu
     * @param start  Name des Knotens, von dem der Pfeil ausgeht, also die "Quelle" der Kante
     * @param target Name des Knotens, auf den der Pfeil zeigt, also das "Ziel" der Kante
     */
    public void addEdge(String start, String target) {
        Vertex startVertex = mapOfVertexes.get(target);
        if (!start.equals(target)) {
            if (!adjacencyMap.get(start).contains(startVertex)) {
                adjacencyMap.get(start).add(startVertex);
            }
        }
    }


    /**
     * Fügt ungerichtete Kante zwischen zwei Knoten zum Graph hinzu
     * @param nameOfVertex1 Name des ersten Knotens
     * @param nameOfVertex2 Name des zweiten Knotens
     */
    public void addEdgeBidirectional(String nameOfVertex1, String nameOfVertex2) {
        Vertex vertex1 = mapOfVertexes.get(nameOfVertex1);
        Vertex vertex2 = mapOfVertexes.get(nameOfVertex2);
        if (!adjacencyMap.get(nameOfVertex1).contains(vertex2)) {
            adjacencyMap.get(nameOfVertex1).add(vertex2);
        }
        if (!adjacencyMap.get(nameOfVertex2).contains(vertex1)) {
            adjacencyMap.get(nameOfVertex2).add(vertex1);
        }
    }


    /**
     * Löscht gerichtete Kante zwischen zwei Knoten
     * @param start  Name des Knotens, von dem der Pfeil ausgeht, also die "Quelle" der Kante
     * @param target Name des Knotens, auf den der Pfeil zeigt, also das "Ziel" der Kante
     */
    public void removeEdge(String start, String target) {
        Vertex targetVertex = mapOfVertexes.get(target);
        List<Vertex> destinationsOfStart = adjacencyMap.get(start);
        if (destinationsOfStart != null) destinationsOfStart.remove(targetVertex);
    }


    /**
     * Löscht ungerichtete Kante zwischen zwei Knoten
     * @param nameOfVertex1 Name des ersten Knotens
     * @param nameOfVertex2 Name des zweiten Knotens
     */
    public void removeEdgeBidirectional(String nameOfVertex1, String nameOfVertex2) {
        Vertex vertex1 = mapOfVertexes.get(nameOfVertex1);
        Vertex vertex2 = mapOfVertexes.get(nameOfVertex2);
        List<Vertex> connectionsOfVertex1 = adjacencyMap.get(nameOfVertex1);
        List<Vertex> connectionsOfVertex2 = adjacencyMap.get(nameOfVertex2);
        if (connectionsOfVertex1 != null) connectionsOfVertex1.remove(vertex2);
        if (connectionsOfVertex2 != null) connectionsOfVertex2.remove(vertex1);
    }


    /**
     * Fügt zwei Knoten zusammen. Die ausgehenden und eingehenden Kanten von vertex2 werden vertex1 hinzugefügt.
     * Anschließend wird vertex2 aus dem Graph entfernt.
     * @param vertex1 Dieser Knoten bleibt nach der Zusammenlegung bestehen.
     * @param vertex2 Dieser Knoten wird nach der Zusammenlegung aus dem Graph entfernt.
     */
    public Vertex joinVertices(Vertex vertex1, Vertex vertex2) {

        System.out.println("join Vertices called");
        List<Vertex> connectionsFromVertex2 = adjacencyMap.get(vertex2.getName());

        // Ausgehende Kanten von vertex2 werden zu vertex1 hinzugefügt
        for (Vertex connection : connectionsFromVertex2) {
            addEdge(vertex1.getName(), connection.getName());
        }

        // Eingehende Kanten von vertex2 werden gesucht und zu vertex1 hinzugefügt
        List<Vertex> connectionsToVertex2 = new ArrayList<>();
        adjacencyMap.entrySet().stream().forEach(adjajencyList -> {
            if (adjajencyList.getValue().contains(vertex2)) {
                Vertex key = mapOfVertexes.get(adjajencyList.getKey());
                connectionsToVertex2.add(key);
            }
        });
        for (Vertex connection : connectionsToVertex2) {
            addEdge(connection.getName(), vertex1.getName());
        }
        // vertex2 wird aus Graph zusammen mit seinen Kanten entfernt
        removeVertex(vertex2.getName());
        return vertex1;
    }

    /**
     * Soll überprüfen, ob es im Graph Punkte gibt, die sich "an der gleichen Stelle" befinden (also die gleichen
     * Koordinaten relativ zum Ursprung der der game map haben)
     */
    public List<Vertex> checkForDuplicatePoints() {
        List<Vertex> joinedVertices = new ArrayList<>();
        for (int i = 0; i < mapOfVertexes.size(); i++) {
            for (int j = 0; j < mapOfVertexes.size(); j++) {
                String nameOfVertex1 = mapOfVertexes.keySet().toArray()[i].toString();
                Vertex v1 = mapOfVertexes.get(nameOfVertex1);
                String nameOfVertex2 = mapOfVertexes.keySet().toArray()[j].toString();
                Vertex v2 = mapOfVertexes.get(nameOfVertex2);
                if (!v1.equals(v2)) {
                    double differenceX = Math.abs(v1.coordsRelativeToMapOrigin().getX() - v2.coordsRelativeToMapOrigin().getX());
                    double differenceY = Math.abs(v1.coordsRelativeToMapOrigin().getY() - v2.coordsRelativeToMapOrigin().getY());
                    // da laut Aufgabenstellung double-Werte nicht auf Gleichheit getestet werden sollen (siehe S. 10),
                    // wird hier geprüft, ob der Unterschied unter 0.1 liegt
                    if(Math.abs(differenceX) < 0.1 && Math.abs(differenceY) < 0.1) {
//                        System.out.println("Joining vertices " + v1.getName() + " " + v2.getName() + " with coords: "
//                                + v1.coordsRelativeToMapOrigin() + " " + v2.coordsRelativeToMapOrigin());
                        joinedVertices.add(joinVertices(v1, v2));
                        j--;
                    }
                }
            }
        }
        return joinedVertices;
    }


    public Map<String, List<Vertex>> getAdjacencyMap() {
        return adjacencyMap;
    }

    public Map<String, Vertex> getMapOfVertexes() {
        return mapOfVertexes;
    }

    //Die Methode gibt den Graph auf der Kommandozeile aus. Ist nur zu Testzwecken vorhanden
    public void printGraph() {
        for (Map.Entry<String, Vertex> entry : this.mapOfVertexes.entrySet()) {
            System.out.print("Vertex " + entry.getKey() + " is connected to: ");
            this.adjacencyMap.get(entry.getKey()).forEach(v -> System.out.print(" " + v.getName() + " "));
            System.out.println();
        }
    }

    // Code ab hier nur zum Testen der Funktionen in TrafficGraph vorhanden. Kann in Zukunft wieder gelöscht werden.
    // Diese Testfunktionen werden im Controller aufgerufen
    public void testGraphAdd() {
        // Testet Hinzufügen von Knoten und Kanten für einen gerichteten Graphen
        System.out.println("Hinzufügen von Knoten und Kanten für gerichteten Graphen");
        addVertex(new Vertex("Bob", 0.0, 0.0, 4, 5));
        addVertex(new Vertex("Alice", 0.0, 0.0, 6, 2));
        addVertex(new Vertex("Rob", 0.0, 0.0, 1, 0));
        addVertex(new Vertex("Mark", 0.0, 0.0, 4, 4));
        addVertex(new Vertex("Maria", 0.0, 0.0, 6, 8));
        addEdge("Bob", "Alice");
        addEdge("Bob", "Rob");
        addEdge("Alice", "Mark");
        addEdge("Rob", "Mark");
        addEdge("Alice", "Maria");
        addEdge("Rob", "Maria");
        addEdge("Mark", "Bob");
        addEdge("Mark", "Alice");
        printGraph();
    }

    public void testGraphRemove() {
        // Testet Löschen von Knoten und Kanten für einen gerichteten Graphen
        System.out.println("Löschen von Knoten und Kanten für gerichteten Graphen");
        removeVertex("Bob");
        removeEdge("Rob", "Maria");
        printGraph();
    }

    public void testGraphAddBidirectional() {
        // Testet Hinzufügen von Knoten und Kanten für einen ungerichteten Graphen
        System.out.println("Hinzufügen von Knoten und Kanten für ungerichteten Graphen");
        addVertex(new Vertex("Bob", 0.0, 0.0, 1, 1));
        addVertex(new Vertex("Alice", 0.0, 0.0, 4, 6));
        addVertex(new Vertex("Rob", 0.0, 0.0, 7, 8));
        addVertex(new Vertex("Mark", 0.0, 0.0, 5, 6));
        addVertex(new Vertex("Maria", 0.0, 0.0, 7, 3));
        addEdgeBidirectional("Bob", "Alice");
        addEdgeBidirectional("Bob", "Rob");
        addEdgeBidirectional("Alice", "Mark");
        addEdgeBidirectional("Rob", "Mark");
        addEdgeBidirectional("Alice", "Maria");
        addEdgeBidirectional("Rob", "Maria");
        addEdgeBidirectional("Mark", "Bob");
        addEdgeBidirectional("Mark", "Alice");
        printGraph();
    }

    public void testGraphRemoveBidirectional() {
        // Testet Löschen von Knoten und Kanten für einen ungerichteten Graphen
        System.out.println("Löschen von Knoten und Kanten für ungerichteten Graphen");
        removeVertex("Bob");
        removeEdgeBidirectional("Rob", "Maria");
        printGraph();
    }

    public void testJoinPoints() {
        System.out.println("Test joining Tom and Jerry");
        System.out.println("Graph before joining");
        Vertex vertex1 = new Vertex("Tom", 0.0, 0.0, 6, 3);
        Vertex vertex2 = new Vertex("Jerry", 0.0, 0.0, 1, 1);

        addVertex(vertex1);
        addVertex(vertex2);
        addVertex(new Vertex("Bob", 0.0, 0.0, 1, 1));
        addEdge("Bob", "Jerry");
        addEdge("Jerry", "Maria");
        addEdge("Alice", "Jerry");
        printGraph();
        joinVertices(vertex1, vertex2);
        System.out.println();
        System.out.println("Graph after joining");
        printGraph();
    }

    public void testChekForDuplicates(){
        System.out.println("Checking for points with same position on map");
        System.out.println("Vertices in graph with coordinates:");
        Vertex will = new Vertex("Will", 1, 0.5,
                1, 1);
        Vertex sarah = new Vertex("Sarah", 0, 0.5,
                2, 1);
        Vertex draco = new Vertex("Draco", 1, 0.5,
                3, 5);
        addVertex(will);
        addVertex(sarah);
        addVertex(draco);

        Point2D willPoints = will.coordsRelativeToMapOrigin();
        Point2D saraPoints = sarah.coordsRelativeToMapOrigin();
        Point2D dracoPoints = draco.coordsRelativeToMapOrigin();

        System.out.println("Will " + willPoints.getX() + " " + willPoints.getY());
        System.out.println("Sara " + saraPoints.getX() + " " + saraPoints.getY());
        System.out.println("Draco " + dracoPoints.getX() + " " + dracoPoints.getY());

        checkForDuplicatePoints();
    }
}
