package model;

import java.util.*;

public class Railblock {

    Set<Vertex> vertices = new HashSet<>();
    Set<Tile> tilesOfSignals = new HashSet<>();

    Map<Integer, Vehicle> reservations = new HashMap<>();

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

    public boolean isReservedAtDay(int day){
        return reservations.containsKey(day);
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

    public Map<Integer, Vehicle> getReservations() {
        return reservations;
    }


}
