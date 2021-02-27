package model;

import java.util.*;

public class Railblock {

    Set<Vertex> vertices = new HashSet<>();
    Set<Tile> tilesOfSignals = new HashSet<>();

    Map<Integer, Train> reservations = new HashMap<>();

//
//    public Railblock(Set<Vertex> vertices) {
//        addVertices(vertices);
//    }

    public void addVertices(Set<Vertex> vertices){
        this.vertices.addAll(vertices);
        for(Vertex vertex : vertices){
            vertex.setRailblock(this);
        }
    }

    public void mergeWithRailblock(Railblock otherBlock){
        addVertices(otherBlock.getVertices());
        tilesOfSignals.addAll(otherBlock.getTilesOfSignals());
        //TODO Fall ingnoriert, wenn es schon Reservierungen gibt
    }

    public void clearMapForAllPastDays(int day){
        reservations.entrySet().removeIf(entry -> entry.getKey() < day);
    }

    public boolean isReservedAtDay(int day, Train ownTrain){
        boolean dayContained = reservations.containsKey(day);
        boolean isNotReservedByOwnTrain = true;
//        System.out.println("asked reserved at day: " + day);
//        reservations.forEach((x,y) -> System.out.println("Day: " + x + " reserved by: " +  y.getName()));

        if(dayContained){
            if(reservations.get(day).equals(ownTrain)){
                isNotReservedByOwnTrain = false;
            }
        }

        return dayContained && isNotReservedByOwnTrain;
    }

    public Vehicle getReservedByAtDay(int day){
        return reservations.get(day);
    }

    public Set<Vertex> getVertices() {
        return vertices;
    }

    public Set<Tile> getTilesOfSignals() {
        return tilesOfSignals;
    }

    public Map<Integer, Train> getReservations() {
        return reservations;
    }


}
