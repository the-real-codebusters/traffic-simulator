package model;

import java.util.*;

// Die Wegfindung wird in die Klasse Pathfinder ausgelagert, die einen Graph mit seinen Knoten bekommt
public class Pathfinder {
    private TrafficGraph roadGraph;
    private TrafficGraph railGraph;

    //TODO Klasse eventuell static machen?

    //TODO Eigene Graphen für AIR und RAIL; dann müssen die Methoden je nach Parameter TrafficType unterschiedliche
    // Graphen benutzen

    public Pathfinder(TrafficGraph roadGraph, TrafficGraph railGraph) {
        this.railGraph = railGraph;
        this.roadGraph = roadGraph;
    }

    /**
     * Sucht sich einen Weg von einem Knoten der angegebenen Station zu der nächsten Station
     * @param actualStation Die aktuelle Station, die nicht gefunden werden soll, da sie ja Ausgangspunkt der Suche ist
     * @return Liste des Wegs vom Startknoten zum Zielknoten, inklusive des Startknotens
     */
    public List<Vertex> findPathToNextStation(Station actualStation, TrafficType type){
        TrafficGraph graph = getGraphForTrafficType(type);

        Vertex startVertex = actualStation.getComponents().get(0).getVertices().iterator().next();
        System.out.println("startVertex in Pathfinder "+startVertex.getName());

        // Sollte der gefundene Weg von Startknoten zu gefundenem Zielnoten sein
        List<Vertex> path = new ArrayList<Vertex>();

        // Ebene der Breitensuche in dem Graph. Dies sollte auch der Entfernung zum Startknoten entsprechen
        int searchLevel = 0;
        startVertex.setActualSearchLevel(0);

        Queue<Vertex> queue = new ArrayDeque<>();
        queue.add(startVertex);

        // Knoten die schon in der Suche besucht wurden
        List<Vertex> alreadyVisited = new ArrayList<Vertex>();
        alreadyVisited.add(startVertex);

        // Child, Parent
        // Speichert den Parent eines Knotens, von dem die Breitensuche zu diesem Knoten gelangt ist
        Map<Vertex, Vertex> parentNodes = new HashMap<Vertex, Vertex>();

        Vertex currentNode;

        // Die Queue ist leer, wenn kein Zielknoten gefunden wurde
        while (!queue.isEmpty()) {
            currentNode = queue.remove();
            searchLevel = currentNode.getActualSearchLevel();

            //Wenn if-Bedingung erfüllt ist, dann haben wir das Ziel gefunden
            if (currentNode != null && currentNode.isPointOfStation() && currentNode.getStation() != actualStation) {
                System.out.println("ID current "+currentNode.getStation().getId());
                System.out.println("ID actual "+actualStation.getId());


                // Füge Zielknoten zu Weg hinzu
                path.add(currentNode);

                Vertex actualParentNode = currentNode;

                // Gehe Weg von Zielknoten zu Startknoten zurück und speichere den Weg ab
                // Brich Schleife ab, wenn Startknoten erreicht
                while (!actualParentNode.equals(startVertex)) {
                    actualParentNode = parentNodes.get(actualParentNode);
                    path.add(actualParentNode);
                }
                // Damit Liste start -> ziel anzeigt und nicht ziel -> start
                Collections.reverse(path);
                return path;
            }
            // Wenn wir in den else-Teil gehen, haben wir noch kein Ziel gefunden
            else {
                // Speichere alle in Verbindung stehenden Knoten mit dem aktuellen Knoten in childs
                List<Vertex> childs = new ArrayList<>();
                System.out.println("current Node in pathfinder "+currentNode.getName());

                if(graph.getAdjacencyMap().get(currentNode.getName())==null) throw new NullPointerException("" +
                        "ein Name eines Vertexes in der Adjazenzliste war nicht auffindbar. Das kann doch nicht sein");

                childs.addAll(graph.getAdjacencyMap().get(currentNode.getName()));
                System.out.println("childs in pathfinder "+childs);


                // Entferne alle bereits gesuchten Knoten aus den childs
                childs.removeAll(alreadyVisited);

                // Füge alle noch übrigen childs zu den bereits beuschten Knoten hinzu. Ist für nächste Durchlaufe
                // der Schleife wichtig
                alreadyVisited.addAll(childs);

                int searchLevelOfChild = searchLevel + 1;
                for (Vertex child : childs) {
                    child.setActualSearchLevel(searchLevelOfChild);
                }

                // Füge die übrigen Knoten der Queue hinzu
                queue.addAll(childs);

                for (Vertex child : childs) {
                    // Prüfe, ob ein Knoten mehrere Parents hat. Sollte nie ausgeführt werden
                    if (parentNodes.containsKey(child)){
                        throw new RuntimeException("Map parentNodes hat schon einen Parent für Child");
                    }
                    // Speichere für jedes child den Knoten ab, durch den die Breitensuche zu dem child gelangt ist
                    parentNodes.put(child, currentNode);
                }
            }
        }
        // Wenn kein Pfand gefunden, return leere Liste
        return new ArrayList<Vertex>();
    }

    /**
     * Sucht sich einen Weg von der angegebenen Station zu einer nächsten Station. Gibt alle so gefundenen Stationen zurück.
     * @param actualStation Die aktuelle Station, die nicht gefunden werden soll, da sie ja Ausgangspunkt der Suche ist
     * @return Liste der gefundenen direkt verbundenen Stationen
     */
    public List<Station> findAllDirectlyConnectedStations(Station actualStation, TrafficType type){

        TrafficGraph graph = getGraphForTrafficType(type);

        Vertex startVertex = actualStation.getComponents().get(0).getVertices().iterator().next();
        System.out.println("startVertex in Pathfinder "+startVertex.getName());

        List<Station> foundStations = new ArrayList<>();

        // Ebene der Breitensuche in dem Graph. Dies sollte auch der Entfernung zum Startknoten entsprechen
        int searchLevel = 0;
        startVertex.setActualSearchLevel(0);

        Queue<Vertex> queue = new ArrayDeque<>();
        queue.add(startVertex);

        // Knoten die schon in der Suche besucht wurden
        List<Vertex> alreadyVisited = new ArrayList<Vertex>();
        alreadyVisited.add(startVertex);

        // Child, Parent
        // Speichert den Parent eines Knotens, von dem die Breitensuche zu diesem Knoten gelangt ist
        Map<Vertex, Vertex> parentNodes = new HashMap<Vertex, Vertex>();

        Vertex currentNode;

        // Die Queue ist leer, wenn kein Zielknoten gefunden wurde
        while (!queue.isEmpty()) {
            currentNode = queue.remove();
            searchLevel = currentNode.getActualSearchLevel();

            //Wenn if-Bedingung erfüllt ist, dann haben wir das Ziel gefunden
            if (currentNode != null && currentNode.isPointOfStation() && currentNode.getStation() != actualStation) {

                foundStations.add(currentNode.getStation());
            }
            // Wenn wir in den else-Teil gehen, haben wir noch kein Ziel gefunden
            else {
                // Speichere alle in Verbindung stehenden Knoten mit dem aktuellen Knoten in childs
                List<Vertex> childs = new ArrayList<>();
                System.out.println("current Node : "+currentNode.getName());
                childs.addAll(graph.getAdjacencyMap().get(currentNode.getName()));

                // Entferne alle bereits gesuchten Knoten aus den childs
                childs.removeAll(alreadyVisited);

                // Füge alle noch übrigen childs zu den bereits beuschten Knoten hinzu. Ist für nächste Durchlaufe
                // der Schleife wichtig
                alreadyVisited.addAll(childs);

                int searchLevelOfChild = searchLevel + 1;
                for (Vertex child : childs) {
                    child.setActualSearchLevel(searchLevelOfChild);
                }

                // Füge die übrigen Knoten der Queue hinzu
                queue.addAll(childs);

            }
        }
        // Wenn kein Pfand gefunden, return leere Liste
        return foundStations;
    }

    /**
     * Sucht sich einen Weg von dem angegebenen Knoten zu allen verbundenen Gebäuden.
     * @return Liste der gefundenen verbundenen Knoten
     */
    public Set<PartOfTrafficGraph> findAllConnectedBuildings(Vertex startVertex, TrafficType type){

        TrafficGraph graph = getGraphForTrafficType(type);

        System.out.println("startVertex in Pathfinder "+startVertex.getName());

        Set<PartOfTrafficGraph> foundBuildings = new HashSet<>();

        // Ebene der Breitensuche in dem Graph. Dies sollte auch der Entfernung zum Startknoten entsprechen
        int searchLevel = 0;
        startVertex.setActualSearchLevel(0);

        Queue<Vertex> queue = new ArrayDeque<>();
        queue.add(startVertex);

        // Knoten die schon in der Suche besucht wurden
        List<Vertex> alreadyVisited = new ArrayList<Vertex>();
        alreadyVisited.add(startVertex);

        // Child, Parent
        // Speichert den Parent eines Knotens, von dem die Breitensuche zu diesem Knoten gelangt ist
        Map<Vertex, Vertex> parentNodes = new HashMap<Vertex, Vertex>();

        Vertex currentNode;

        // Die Queue ist leer, wenn kein Zielknoten gefunden wurde
        while (!queue.isEmpty()) {
            currentNode = queue.remove();
            searchLevel = currentNode.getActualSearchLevel();

            //Wenn if-Bedingung erfüllt ist, dann haben wir ein Ziel gefunden
            if (currentNode != null && currentNode.getBuildings().size() > 0) {

                foundBuildings.addAll(currentNode.getBuildings());
            }
            // Wenn wir in den else-Teil gehen, haben wir noch kein Ziel gefunden
                // Speichere alle in Verbindung stehenden Knoten mit dem aktuellen Knoten in childs
                List<Vertex> childs = new ArrayList<>();
                System.out.println("current Node : "+currentNode.getName());
                childs.addAll(graph.getAdjacencyMap().get(currentNode.getName()));

                // Entferne alle bereits gesuchten Knoten aus den childs
                childs.removeAll(alreadyVisited);

                // Füge alle noch übrigen childs zu den bereits beuschten Knoten hinzu. Ist für nächste Durchlaufe
                // der Schleife wichtig
                alreadyVisited.addAll(childs);

                int searchLevelOfChild = searchLevel + 1;
                for (Vertex child : childs) {
                    child.setActualSearchLevel(searchLevelOfChild);
                }

                // Füge die übrigen Knoten der Queue hinzu
                queue.addAll(childs);

        }
        // Wenn kein Pfand gefunden, return leere Liste
        return foundBuildings;
    }

    /**
     * Sucht sich einen Weg von dem angegebenen Knoten zu einer nächsten Station. Gibt alle so gefundenen Stationen zurück.
     * @return Liste der gefundenen direkt verbundenen Stationen
     */
    public List<Station> findAllDirectlyConnectedStations(Vertex startVertex, TrafficType type){

        TrafficGraph graph = getGraphForTrafficType(type);

        //TODO Mit anderer Methode findAllDirectlyConnectedStations mergen?
        System.out.println("startVertex in Pathfinder "+startVertex.getName());

        List<Station> foundStations = new ArrayList<>();

        // Sollte der gefundene Weg von Startknoten zu gefundenem Zielnoten sein
        List<Vertex> path = new ArrayList<Vertex>();

        // Ebene der Breitensuche in dem Graph. Dies sollte auch der Entfernung zum Startknoten entsprechen
        int searchLevel = 0;
        startVertex.setActualSearchLevel(0);

        Queue<Vertex> queue = new ArrayDeque<>();
        queue.add(startVertex);

        // Knoten die schon in der Suche besucht wurden
        List<Vertex> alreadyVisited = new ArrayList<Vertex>();
        alreadyVisited.add(startVertex);

        // Child, Parent
        // Speichert den Parent eines Knotens, von dem die Breitensuche zu diesem Knoten gelangt ist
        Map<Vertex, Vertex> parentNodes = new HashMap<Vertex, Vertex>();

        Vertex currentNode;

        // Die Queue ist leer, wenn kein Zielknoten gefunden wurde
        while (!queue.isEmpty()) {
            currentNode = queue.remove();
            searchLevel = currentNode.getActualSearchLevel();

            //Wenn if-Bedingung erfüllt ist, dann haben wir das Ziel gefunden
            if (currentNode != null && currentNode.isPointOfStation()) {

                foundStations.add(currentNode.getStation());
                if(currentNode == startVertex){
                    List<Vertex> childs = new ArrayList<>();
                    childs.addAll(graph.getAdjacencyMap().get(currentNode.getName()));
                    queue.addAll(childs);
                }
            }
            // Wenn wir in den else-Teil gehen, haben wir noch kein Ziel gefunden
            else {
                // Speichere alle in Verbindung stehenden Knoten mit dem aktuellen Knoten in childs
                List<Vertex> childs = new ArrayList<>();
                System.out.println("current Node : "+currentNode.getName());
                childs.addAll(graph.getAdjacencyMap().get(currentNode.getName()));

                // Entferne alle bereits gesuchten Knoten aus den childs
                childs.removeAll(alreadyVisited);

                // Füge alle noch übrigen childs zu den bereits beuschten Knoten hinzu. Ist für nächste Durchlaufe
                // der Schleife wichtig
                alreadyVisited.addAll(childs);

                int searchLevelOfChild = searchLevel + 1;
                for (Vertex child : childs) {
                    child.setActualSearchLevel(searchLevelOfChild);
                }

                // Füge die übrigen Knoten der Queue hinzu
                queue.addAll(childs);

            }
        }
        // Wenn kein Pfand gefunden, return leere Liste
        return foundStations;
    }


    /**
     * Sucht sich einen Weg vom Startknoten zu der angegebenen Station
     * @param desiredStation Die Station, zu der der Weg gefunden werden soll
     * @return Liste des Wegs vom Startknoten zum Zielknoten, exklusive des Startknotens
     */
    public List<Vertex> findPathToDesiredStation(Station desiredStation, Vertex startVertex, TrafficType trafficType){

        TrafficGraph graph = getGraphForTrafficType(trafficType);

        System.out.println("startVertex in Pathfinder "+startVertex.getName());

        // Sollte der gefundene Weg von Startknoten zu gefundenem Zielnoten sein
        List<Vertex> path = new ArrayList<Vertex>();

        // Ebene der Breitensuche in dem Graph. Dies sollte auch der Entfernung zum Startknoten entsprechen
        int searchLevel = 0;
        startVertex.setActualSearchLevel(0);

        Queue<Vertex> queue = new ArrayDeque<>();
        queue.add(startVertex);

        // Knoten die schon in der Suche besucht wurden
        List<Vertex> alreadyVisited = new ArrayList<Vertex>();
        alreadyVisited.add(startVertex);

        // Child, Parent
        // Speichert den Parent eines Knotens, von dem die Breitensuche zu diesem Knoten gelangt ist
        Map<Vertex, Vertex> parentNodes = new HashMap<Vertex, Vertex>();

        Vertex currentNode;

        // Die Queue ist leer, wenn kein Zielknoten gefunden wurde
        while (!queue.isEmpty()) {
            currentNode = queue.remove();
            searchLevel = currentNode.getActualSearchLevel();

            //Wenn if-Bedingung erfüllt ist, dann haben wir das Ziel gefunden
            if (currentNode != null && currentNode.isPointOfStation() && currentNode.getStation() == desiredStation) {

                // Füge Zielknoten zu Weg hinzu
                path.add(currentNode);

                Vertex actualParentNode = currentNode;

                // Gehe Weg von Zielknoten zu Startknoten zurück und speichere den Weg ab
                // Brich Schleife ab, wenn Startknoten erreicht
                while (!actualParentNode.equals(startVertex)) {
                    actualParentNode = parentNodes.get(actualParentNode);
                    path.add(actualParentNode);
                }
                // Damit Liste start -> ziel anzeigt und nicht ziel -> start
                Collections.reverse(path);

                //Entferne den Startknoten, da die Rückgabe exklusive des Startknotens sein soll
                path.remove(0);
                return path;
            }
            // Wenn wir in den else-Teil gehen, haben wir noch kein Ziel gefunden
            else {
                // Speichere alle in Verbindung stehenden Knoten mit dem aktuellen Knoten in childs
                List<Vertex> childs = new ArrayList<>();
                System.out.println("current Node in pathfinder "+currentNode.getName());

                if(graph.getAdjacencyMap().get(currentNode.getName())==null) throw new NullPointerException("" +
                        "ein Name eines Vertexes in der Adjazenzliste war nicht auffindbar. Das kann doch nicht sein");

                childs.addAll(graph.getAdjacencyMap().get(currentNode.getName()));
                System.out.println("childs in pathfinder "+childs);


                // Entferne alle bereits gesuchten Knoten aus den childs
                childs.removeAll(alreadyVisited);

                // Füge alle noch übrigen childs zu den bereits beuschten Knoten hinzu. Ist für nächste Durchlaufe
                // der Schleife wichtig
                alreadyVisited.addAll(childs);

                int searchLevelOfChild = searchLevel + 1;
                for (Vertex child : childs) {
                    child.setActualSearchLevel(searchLevelOfChild);
                }

                // Füge die übrigen Knoten der Queue hinzu
                queue.addAll(childs);

                for (Vertex child : childs) {
                    // Prüfe, ob ein Knoten mehrere Parents hat. Sollte nie ausgeführt werden
                    if (parentNodes.containsKey(child)){
                        throw new RuntimeException("Map parentNodes hat schon einen Parent für Child");
                    }
                    // Speichere für jedes child den Knoten ab, durch den die Breitensuche zu dem child gelangt ist
                    parentNodes.put(child, currentNode);
                }
            }
        }
        // Wenn kein Pfand gefunden, return leere Liste
        return new ArrayList<Vertex>();
    }

    public TrafficGraph getGraphForTrafficType(TrafficType trafficType){
        TrafficGraph graph;
        if(trafficType.equals(TrafficType.ROAD)){
            graph = roadGraph;
        }
        else if(trafficType.equals(TrafficType.RAIL)){
            graph = railGraph;
        }
        else {
            throw new IllegalArgumentException("Unfertiger Code");
        }
        return graph;
    }


    public List<Vertex> findPathForRoadRailway(Vertex startVertex, Vertex destinationVertex){
        return new ArrayList<>();
    }

    public List<Vertex> findPathForPlane(Vertex startVertex, Vertex destinationVertex){
        return Arrays.asList(startVertex, destinationVertex);
    }

    public List<Station> findPathForPlane(Station startStation, Station destinationStation){
        return Arrays.asList(startStation, destinationStation);
    }
}
