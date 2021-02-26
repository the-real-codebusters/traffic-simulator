package model;

import javafx.geometry.Point2D;

import java.util.*;


public class TrafficLineGraph {
    
    private Map<Long, Map<Station, Double>> weightedAdjacencyMap = new LinkedHashMap<>();
    private Map<Long, Station> mapOfStations = new LinkedHashMap<>();


    /**
     * Fügt den Knoten zum Graph hinzu
     *
     * @param station
     */
    public boolean addStation(Station station) {
        Long name = station.getId();
        if (!weightedAdjacencyMap.containsKey(name)) {
            weightedAdjacencyMap.put(name, new HashMap<>());
            mapOfStations.put(name, station);
            return true;
        } else return false;
    }

    /**
     * Löscht einen Knoten aus dem Graph
     *
     * @param id Name des zu löschenden Knotens
     */
    public void removeStation(Long id) {
        Station station = mapOfStations.get(id);
        weightedAdjacencyMap.values().stream().forEach(adjajencyList -> adjajencyList.remove(station));
        weightedAdjacencyMap.remove(id);
        mapOfStations.remove(id);

        System.out.println("Station " + id + " removed");
    }


//    /**
//     * Fügt gerichtete Kante zwischen zwei Knoten zum Graph hinzu
//     *
//     * @param start  Name des Knotens, von dem der Pfeil ausgeht, also die "Quelle" der Kante
//     * @param target Name des Knotens, auf den der Pfeil zeigt, also das "Ziel" der Kante
//     */
//    public void addEdge(Long start, Long target, double weight) {
//        Station targetStation = mapOfStations.get(target);
//        if (!start.equals(target)) {
//            if (!weightedAdjacencyMap.get(start).containsKey(targetStation)) {
//                weightedAdjacencyMap.get(start).put(targetStation, weight);
//            }
//        }
//    }


    /**
     * Fügt ungerichtete Kante zwischen zwei Knoten zum Graph hinzu
     *
     * @param idOfStation1 Name des ersten Knotens
     * @param idOfStation2 Name des zweiten Knotens
     */
    public void addEdgeBidirectional(Long idOfStation1, Long idOfStation2, double weight) {
        Station station1 = mapOfStations.get(idOfStation1);
        Station station2 = mapOfStations.get(idOfStation2);
        System.out.println("idOfStation1: " + idOfStation1);
        System.out.println("station2: " + station2);
        System.out.println(weightedAdjacencyMap.get(idOfStation1));
        if (!weightedAdjacencyMap.get(idOfStation1).containsKey(station2)) {
            weightedAdjacencyMap.get(idOfStation1).put(station2, weight);
        }
        if (!weightedAdjacencyMap.get(idOfStation2).containsKey(station1)) {
            weightedAdjacencyMap.get(idOfStation2).put(station1, weight);
        }
    }


//    /**
//     * Löscht gerichtete Kante zwischen zwei Knoten
//     *
//     * @param start  Name des Knotens, von dem der Pfeil ausgeht, also die "Quelle" der Kante
//     * @param target Name des Knotens, auf den der Pfeil zeigt, also das "Ziel" der Kante
//     */
//    public void removeEdge(String start, String target) {
//        Station targetStation = mapOfStations.get(target);
//        List<Station> destinationsOfStart = weightedAdjacencyMap.get(start);
//        if (destinationsOfStart != null) destinationsOfStart.remove(targetStation);
//    }


    /**
     * Löscht ungerichtete Kante zwischen zwei Knoten
     *
     * @param idOfStation1 Name des ersten Knotens
     * @param idOfStation2 Name des zweiten Knotens
     */
    public void removeEdgeBidirectional(Long idOfStation1, Long idOfStation2) {
        Station station1 = mapOfStations.get(idOfStation1);
        Station station2 = mapOfStations.get(idOfStation2);
        Map<Station, Double> connectionsOfStation1 = weightedAdjacencyMap.get(idOfStation1);
        Map<Station, Double> connectionsOfStation2 = weightedAdjacencyMap.get(idOfStation2);
        if (connectionsOfStation1 != null) connectionsOfStation1.remove(station2);
        if (connectionsOfStation2 != null) connectionsOfStation2.remove(station1);
    }

//    public boolean hasBidirectionalEdge(String nameOfStation1, String nameOfStation2) {
//        Station station2 = mapOfStations.get(nameOfStation2);
//
//        boolean hasEdge = weightedAdjacencyMap.get(nameOfStation1).contains(station2);
//
//        System.out.println("hasEdge between " + nameOfStation1 + " and " + nameOfStation2 + " was " + hasEdge);
//        return hasEdge;
//    }


    public void generateEntriesFromStationList(List<Station> stations){
        for (Station station : stations){
            mapOfStations.putIfAbsent(station.getId(), station);
            weightedAdjacencyMap.putIfAbsent(station.getId(), new HashMap<>());
        }
        for (int i = 0; i < stations.size()-1; i++){
            Station station1 = stations.get(i);
            Station station2 = stations.get(i+1);
            Point2D point1 = station1.getComponents().get(0).getVertices().iterator().next().coordsRelativeToMapOrigin();
            Point2D point2 = station2.getComponents().get(0).getVertices().iterator().next().coordsRelativeToMapOrigin();
            double distance = point1.distance(point2);

            addEdgeBidirectional(station1.getId(), station2.getId(), distance);

        }
    }


    /**
     * Fügt zwei Knoten zusammen. Die ausgehenden und eingehenden Kanten von station2 werden station1 hinzugefügt.
     * Anschließend wird station2 aus dem Graph entfernt.
     *
     * @param station1 Dieser Knoten bleibt nach der Zusammenlegung bestehen.
     * @param station2 Dieser Knoten wird nach der Zusammenlegung aus dem Graph entfernt.
     */
    public Station joinVertices(Station station1, Station station2) {

        System.out.println("join Vertices called");
        Map<Station, Double> connectionsFromStation2 = weightedAdjacencyMap.get(station2.getId());

        // Ausgehende Kanten von station2 werden zu station1 hinzugefügt
        for (Map.Entry<Station, Double> entry : connectionsFromStation2.entrySet()) {
            addEdgeBidirectional(station1.getId(), entry.getKey().getId(), entry.getValue());
        }

        // Eingehende Kanten von station2 werden gesucht und zu station1 hinzugefügt
//        List<Station> connectionsToStation2 = new ArrayList<>();
//        weightedAdjacencyMap.entrySet().stream().forEach(adjajencyList -> {
//            if (adjajencyList.getValue().contains(station2)) {
//                Station key = mapOfStations.get(adjajencyList.getKey());
//                connectionsToStation2.add(key);
//            }
//        });
//        for (Station connection : connectionsToStation2) {
//            addEdge(connection.getId(), station1.getId());
//        }
        // station2 wird aus Graph zusammen mit seinen Kanten entfernt
        removeStation(station2.getId());
        return station1;
    }

//    /**
//     * Soll überprüfen, ob es im Graph Punkte gibt, die sich "an der gleichen Stelle" befinden (also die gleichen
//     * Koordinaten relativ zum Ursprung der der game map haben)
//     */
//    public List<Station> checkForDuplicatePoints() {
//        List<Station> joinedVertices = new ArrayList<>();
//        for (int i = 0; i < mapOfStations.size(); i++) {
//            for (int j = 0; j < mapOfStations.size(); j++) {
//                String nameOfStation1 = mapOfStations.keySet().toArray()[i].toString();
//                Station v1 = mapOfStations.get(nameOfStation1);
//                String nameOfStation2 = mapOfStations.keySet().toArray()[j].toString();
//                Station v2 = mapOfStations.get(nameOfStation2);
//                if (!v1.equals(v2)) {
//                    double differenceX = Math.abs(v1.coordsRelativeToMapOrigin().getX() - v2.coordsRelativeToMapOrigin().getX());
//                    double differenceY = Math.abs(v1.coordsRelativeToMapOrigin().getY() - v2.coordsRelativeToMapOrigin().getY());
//                    // da laut Aufgabenstellung double-Werte nicht auf Gleichheit getestet werden sollen (siehe S. 10),
//                    // wird hier geprüft, ob der Unterschied unter 0.1 liegt
//                    if (Math.abs(differenceX) < 0.1 && Math.abs(differenceY) < 0.1) {
////                        System.out.println("Joining vertices " + v1.getName() + " " + v2.getName() + " with coords: "
////                                + v1.coordsRelativeToMapOrigin() + " " + v2.coordsRelativeToMapOrigin());
//                        joinedVertices.add(joinVertices(v1, v2));
//                        j--;
//                    }
//                }
//            }
//        }
//        return joinedVertices;
//    }


    public Map<Long, Map<Station, Double>> getWeightedAdjacencyMap() {
        return weightedAdjacencyMap;
    }

    public Map<Long, Station> getMapOfStations() {
        return mapOfStations;
    }

    //Die Methode gibt den Graph auf der Kommandozeile aus. Ist nur zu Testzwecken vorhanden
    public void printGraph() {
        for (Map.Entry<Long, Station> entry : this.mapOfStations.entrySet()) {
            System.out.print("StationGraph " + entry.getKey() + " is connected to: ");
            this.weightedAdjacencyMap.get(entry.getKey()).forEach((v,x) -> System.out.print(" " + v.getId() + " weight: " + x));
            System.out.println();
        }
    }
}
