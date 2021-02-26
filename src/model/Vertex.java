package model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Vertex extends PositionOnTilemap {
    // TODO: bei Einlesen aus JSON sollte sichergestellt werden, dass Name unique ist
    private String name;

    // Gibt an, ob ein Verkehrsmittel auf diesem Knoten halten darf. Ist true, wenn der Knoten zu einer Station gehört
    private boolean isPointOfStation = false;

    private Station station = null;
    private Railblock railblock = null;

    // Liste von Bauwerken, zu denen der Punkt gehört
    // Wenn ein Punkt zu einer Haltestelle gehört, darf ein Fahrzeug darauf anhalten
//    private List<Building> buildings

    private Set<PartOfTrafficGraph> buildings = new HashSet<>();
    private int actualSearchLevel;
    // Knotennamen (sw, r0, ...)
    private String direction;

    public Vertex(String name, double xCoordinateRelativeToTileOrigin, double yCoordinateRelativeToTileOrigin,
        int xCoordinateInGameMap, int yCoordinateInGameMap, String direction) {

        super(xCoordinateRelativeToTileOrigin, yCoordinateRelativeToTileOrigin, xCoordinateInGameMap,
                yCoordinateInGameMap);
        this.name = name;
        this.direction = direction;
    }

    public Vertex(String name, double xCoordinateRelativeToTileOrigin, double yCoordinateRelativeToTileOrigin,
                  int xCoordinateInGameMap, int yCoordinateInGameMap) {

        super(xCoordinateRelativeToTileOrigin, yCoordinateRelativeToTileOrigin, xCoordinateInGameMap,
                yCoordinateInGameMap);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setActualSearchLevel(int temporarDistanceToStartVertexInBreathFirstSearch) {
        this.actualSearchLevel = temporarDistanceToStartVertexInBreathFirstSearch;
    }

    public int getActualSearchLevel() {
        return actualSearchLevel;
    }

    public boolean isPointOfStation() {
        return isPointOfStation;
    }

    public void setPointOfStation(boolean pointOfStation) {
        isPointOfStation = pointOfStation;
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public Set<PartOfTrafficGraph> getBuildings() {
        return buildings;
    }

    public Railblock getRailblock() {
        return railblock;
    }

    public void setRailblock(Railblock railblock) {
        this.railblock = railblock;
    }

    public String getDirection() {
        return direction;
    }

    public boolean isFirst() {
        return direction.equals("r0");
    }

    public boolean isLast() {
        return direction.equals("se");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vertex vertex = (Vertex) o;

        if (Double.compare(vertex.xCoordinateRelativeToTileOrigin, xCoordinateRelativeToTileOrigin) != 0)
            return false;
        if (Double.compare(vertex.yCoordinateRelativeToTileOrigin, yCoordinateRelativeToTileOrigin) != 0)
            return false;
        if (xCoordinateInGameMap != vertex.xCoordinateInGameMap) return false;
        if (yCoordinateInGameMap != vertex.yCoordinateInGameMap) return false;
        return name.equals(vertex.name);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = name.hashCode();
        temp = Double.doubleToLongBits(xCoordinateRelativeToTileOrigin);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(yCoordinateRelativeToTileOrigin);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + xCoordinateInGameMap;
        result = 31 * result + yCoordinateInGameMap;
        return result;
    }
}
