package model;

import javafx.geometry.Point2D;

public class PositionOnTilemap {

    // Werte, wie in "points" in der JSON-Datei. Werte zwischen 0 und 1, Änderung innerhalb des Tiles, ausgehend von
    // der "linken" Ecke auf der Graphik
    protected double xCoordinateRelativeToTileOrigin;
    protected double yCoordinateRelativeToTileOrigin;

    // Gibt an, auf welchem Feld sich die Position befindet.
    protected int xCoordinateInGameMap;
    protected int yCoordinateInGameMap;

    public PositionOnTilemap(double xCoordinateRelativeToTileOrigin, double yCoordinateRelativeToTileOrigin, int xCoordinateInGameMap, int yCoordinateInGameMap) {
        this.xCoordinateRelativeToTileOrigin = xCoordinateRelativeToTileOrigin;
        this.yCoordinateRelativeToTileOrigin = yCoordinateRelativeToTileOrigin;
        this.xCoordinateInGameMap = xCoordinateInGameMap;
        this.yCoordinateInGameMap = yCoordinateInGameMap;
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


    public double getDistanceToPosition(PositionOnTilemap position){
        Point2D start = this.coordsRelativeToMapOrigin();
        Point2D end = position.coordsRelativeToMapOrigin();
        return start.distance(end);
    }

    public VehiclePosition getnewPositionShiftedTowardsGivenPointByGivenDistance(Point2D desiredPoint, double distance){
        Point2D startPoint = coordsRelativeToMapOrigin();
        Point2D endPoint = startPoint.add(desiredPoint.subtract(startPoint).normalize().multiply(distance));

        return new VehiclePosition(endPoint);
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
}
