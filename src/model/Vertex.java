package model;

import java.util.List;

public class Vertex extends PositionOnTilemap {
    // TODO: bei Einlesen aus JSON sollte sichergestellt werden, dass Name unique ist
    private String name;

    // Gibt an, ob ein Verkehrsmittel auf diesem Knoten halten darf. Ist true, wenn der Knoten zu einer Station gehört
    private boolean isPointOfStation = false;

    private Station station = null;

    // Liste von Bauwerken, zu denen der Punkt gehört
    // Wenn ein Punkt zu einer Haltestelle gehört, darf ein Fahrzeug darauf anhalten
    private List<Building> buildings;

    private int actualSearchLevel;


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
