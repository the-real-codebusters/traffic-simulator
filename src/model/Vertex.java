package model;

import javafx.geometry.Point2D;

import java.util.List;

public class Vertex {
    // TODO: bei Einlesen aus JSON sollte sichergestellt werden, dass Name unique ist
    private String name;

    // Werte, die aus "points" aus JSON-Datei eingelesen werden
    private double xCoordinateRelativeToTileOrigin;
    private double yCoordinateRelativeToTileOrigin;

    // Gibt an, auf welchem Feld sich der Knoten befindet
    private int xCoordinateInGameMap;
    private int yCoordinateInGameMap;

    // Gibt an, ob ein Verkehrsmittel auf diesem Knoten halten darf. Ist true, wenn der Knoten zu einer Station gehört
    private boolean isPointOfStation = false;

    private Station station = null;

    // Liste von Bauwerken, zu denen der Punkt gehört
    // Wenn ein Punkt zu einer Haltestelle gehört, darf ein Fahrzeug darauf anhalten
    private List<Building> buildings;

    private int actualSearchLevel;



    public Vertex(String name, double xCoordinateRelativeToTileOrigin, double yCoordinateRelativeToTileOrigin,
        int xCoordinateInGameMap, int yCoordinateInGameMap) {
        this.name = name;
        this.xCoordinateRelativeToTileOrigin = xCoordinateRelativeToTileOrigin;
        this.yCoordinateRelativeToTileOrigin = yCoordinateRelativeToTileOrigin;
        this.xCoordinateInGameMap = xCoordinateInGameMap;
        this.yCoordinateInGameMap = yCoordinateInGameMap;
    }

    public String getName() {
        return name;
    }

    /**
     * Koordinaten, die relativ zum Ursprung des jeweiligen Feldes zu verstehen sind, sollen umgerechnet werden,
     * so dass sie als Koordinaten innerhalb der gesamten Map zu verstehen sind
     * @return ein Point2D mit den Koordinaten eines Vertex innerhalb der Map
     */
    public Point2D coordsRelativeToMapOrigin() {
        double x = xCoordinateRelativeToTileOrigin + xCoordinateInGameMap;
        double y = yCoordinateRelativeToTileOrigin + yCoordinateInGameMap;
        return new Point2D(x, y);
    }

    /**
     * Koordinaten werden entsprechend der Werte der points umgerechnet
     * @param pointOnMap die linke Ecke des Ursprungstiles
     * @return ein Point2D mit den Koordinaten des Points
     */
    public Point2D moveCoordinatesByPointCoordinates(Point2D pointOnMap) {
        return pointOnMap.subtract(xCoordinateRelativeToTileOrigin, yCoordinateRelativeToTileOrigin);
    }

    public int getxCoordinateInGameMap() {
        return xCoordinateInGameMap;
    }

    public int getyCoordinateInGameMap() {
        return yCoordinateInGameMap;
    }

    public double getxCoordinateRelativeToTileOrigin() {
        return xCoordinateRelativeToTileOrigin;
    }

    public double getyCoordinateRelativeToTileOrigin() {
        return yCoordinateRelativeToTileOrigin;
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
