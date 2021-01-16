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
    private boolean isPointOfStation;

    // Liste von Bauwerken, zu denen der Punkt gehört
    // Wenn ein Punkt zu einer Haltestelle gehört, darf ein Fahrzeug darauf anhalten
    private List<Building> buildings;


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
}
