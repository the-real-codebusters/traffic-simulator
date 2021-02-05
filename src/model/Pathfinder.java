package model;

import java.util.*;

// Die Wegfindung wird in die Klasse Pathfinder ausgelagert, die einen Graph mit seinen Knoten bekommt
public class Pathfinder {
    private TrafficGraph trafficGraph;


    public Pathfinder(TrafficGraph trafficGraph) {
        this.trafficGraph = trafficGraph;
    }

    // Je nach Verkehrsmittel ändert sich die Art der Wegfindung, dazu soll Pathfinder verschiedene Methoden anbieten



    /**
     * Sucht sich einen Weg vom Startknoten zum Zielknoten unter Benutzung der Breitensuche.
     * @param startVertex           Anfangsknoten der Suche
     * @param destinationVertex     Zielknoten der Suche
     * @return Liste des Wegs vom Startknoten zum Zielknoten, inklusive des Startknotens
     */

    public List<Vertex> findPathForRoadVehicle(Vertex startVertex, Vertex destinationVertex){

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
                if (currentNode != null && currentNode.equals(destinationVertex)) {

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
                    childs.addAll(trafficGraph.getAdjacencyMap().get(currentNode.getName()));

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
     * Sucht sich einen Weg vom Startknoten zu einem Gebäude vom Typ des angegebenen Gebäudes
     * @param actualStation Die aktuelle Station, die nicht gefunden werden soll, da sie ja Ausgangspunkt der Suche ist
     * @return Liste des Wegs vom Startknoten zum Zielknoten, inklusive des Startknotens
     */

    public List<Vertex> findPathToNextStation(Station actualStation){

        Vertex startVertex = actualStation.getComponents().get(0).getVertices().get(0);
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

                if(trafficGraph.getAdjacencyMap().get(currentNode.getName())==null) throw new NullPointerException("" +
                        "ein Name eines Vertexes in der Adjazenzliste war nicht auffindbar. Das kann doch nicht sein");

                childs.addAll(trafficGraph.getAdjacencyMap().get(currentNode.getName()));
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
    public List<Station> findAllDirectlyConnectedStations(Station actualStation){

        Vertex startVertex = actualStation.getComponents().get(0).getVertices().get(0);
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
            if (currentNode != null && currentNode.isPointOfStation() && currentNode.getStation() != actualStation) {

                foundStations.add(currentNode.getStation());
            }
            // Wenn wir in den else-Teil gehen, haben wir noch kein Ziel gefunden
            else {
                // Speichere alle in Verbindung stehenden Knoten mit dem aktuellen Knoten in childs
                List<Vertex> childs = new ArrayList<>();
                System.out.println("current Node : "+currentNode.getName());
                childs.addAll(trafficGraph.getAdjacencyMap().get(currentNode.getName()));

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




    public List<Vertex> findPathForRoadRailway(Vertex startVertex, Vertex destinationVertex){
        return new ArrayList<>();
    }

    public List<Vertex> findPathForPlane(Vertex startVertex, Vertex destinationVertex){
        return new ArrayList<>();
    }
}
