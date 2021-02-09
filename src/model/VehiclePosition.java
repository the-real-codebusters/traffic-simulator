package model;

import javafx.geometry.Point2D;

// Die Position eines fahrzeugs in den Model-Koordinaten. ist eigentlich das gleiche wie PositionOnTilemap
public class VehiclePosition extends PositionOnTilemap {


    public VehiclePosition(double xCoordinateRelativeToTileOrigin, double yCoordinateRelativeToTileOrigin,
                           int xCoordinateInGameMap, int yCoordinateInGameMap) {

        super(xCoordinateRelativeToTileOrigin, yCoordinateRelativeToTileOrigin, xCoordinateInGameMap,
                yCoordinateInGameMap);
    }

    public VehiclePosition(Point2D point2D) {
        super(0,0,0,0);
        xCoordinateInGameMap = (int) point2D.getX();
        yCoordinateInGameMap = (int) point2D.getY();

        xCoordinateRelativeToTileOrigin = point2D.getX() - xCoordinateInGameMap;
        yCoordinateRelativeToTileOrigin = point2D.getY() - yCoordinateInGameMap;
    }

}
