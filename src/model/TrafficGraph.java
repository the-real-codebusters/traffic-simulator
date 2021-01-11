package model;

import java.util.*;

/**
 * Klasse zur Darstellung des Vekehrsgraphen mittels Adjazenzliste. Der Graph ist gebildet aus Knotenpunkten und
 * Verbindungen zwischen diesen Punkten. Die Kanten können gerichtet oder ungerichtet sein.
 * Zusätzlich bekommt ein Graph einen Pathfinder, der sich um die Wegsuche im Graphen kümmern soll
 */
public class TrafficGraph {
    private Map<String, List<Vertex>> adjacencyMap;
    private Map<String, Vertex> mapOfVertexes = new HashMap<>();

    // Je nach Verkehrsmittel ändert sich der Mechanismus zur Wegfindung
    private Pathfinder pathfinder;

    public TrafficGraph(Map<String, List<Vertex>> adjacencyMap, Map<String, Vertex> mapOfVertexes, Pathfinder pathfinder) {
        this.adjacencyMap = adjacencyMap;
        this.mapOfVertexes = mapOfVertexes;
        this.pathfinder = pathfinder;
    }

    /**
     * Fügt den Knoten zum Graph hinzu
     * @param vertex
     */
    public void addVertex(Vertex vertex) {
        String name = vertex.getName();
        if (!adjacencyMap.containsKey(name)) {
            adjacencyMap.put(name, new ArrayList<>());
            mapOfVertexes.put(name, vertex);
        }
    }

    // TODO: verbinde Punkte, die sich "an der gleichen Stelle" befinden

    /**
     * Fügt gerichtete Kante zwischen zwei Knoten zum Graph hinzu, kann also für gerichtete Graphen verwendet werden
     * @param start Name des Knotens, von dem der Pfeil ausgeht, also die "Quelle" der Kante
     * @param target Name des Knotens, auf den der Pfeil zeigt, also das "Ziel" der Kante
     */
    public void addEdge(String start, String target) {
        Vertex startVertex = mapOfVertexes.get(target);
        adjacencyMap.get(start).add(startVertex);
    }

    /**
     * Fügt ungerichtete Kante zwischen zwei Knoten zum Graph hinzu, kann also für ungerichtete Graphen verwendet werden
     * @param nameOfVertex1 Name des ersten Knotens
     * @param nameOfVertex2 Name des zweiten Knotens
     */
    public void addEdgeBidirectional(String nameOfVertex1, String nameOfVertex2) {
        Vertex vertex1 = mapOfVertexes.get(nameOfVertex2);
        Vertex vertex2 = mapOfVertexes.get(nameOfVertex1);
        adjacencyMap.get(nameOfVertex1).add(vertex1);
        adjacencyMap.get(nameOfVertex2).add(vertex2);
    }

    public Map<String, List<Vertex>> getAdjacencyMap() {
        return adjacencyMap;
    }

    public Map<String, Vertex> getMapOfVertexes() {
        return mapOfVertexes;
    }

    //Die Methode gibt den Graph auf der Kommandozeile aus. Ist nur zu Testzwecken vorhanden
    public void printGraph(){
        Set<String> set = adjacencyMap.keySet();
        Iterator<String> iterator = set.iterator();
        while (iterator.hasNext()){
            String vertex = iterator.next();
            System.out.print("Vertex "+vertex+" is connected to: ");
            List<Vertex> list = adjacencyMap.get(vertex);
            for(Vertex v: list){
                System.out.print(v.getName() + " ");
            }
            System.out.println();
        }
    }
}
