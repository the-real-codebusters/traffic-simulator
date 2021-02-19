package model;

import javafx.geometry.Point2D;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Die Punkte (exklusiv der Startposition), die das Fahrzeug nacheinander ansteuern soll und die jeweilige Distanz vom vorherigen Punkt
//zu diesem Punkt. Außerdem ist die startPosition gespeichert
public class VehicleMovement {

    private PositionOnTilemap startPosition;
    private List<PositionOnTilemap> positionsOnMap = new ArrayList<>();
    private List<Double> travelDistances = new ArrayList<>();
    private String vehicleName;
    private boolean wait;

    public VehicleMovement(PositionOnTilemap startPosition, String vehicleName, boolean wait) {
        this.vehicleName = vehicleName;
        this.startPosition = startPosition;
        this.wait = wait;
    }

    public Pair<PositionOnTilemap, Double> getPairOfPositionAndDistance(int i){
        return new Pair<PositionOnTilemap, Double>(positionsOnMap.get(i), travelDistances.get(i));
    }

    public PositionOnTilemap getStartPosition() {
        return startPosition;
    }


    public void appendPairOfPositionAndDistance(PositionOnTilemap positionOnTilemap, Double distance){
        positionsOnMap.add(positionOnTilemap);
        travelDistances.add(distance);
    }

    public void removeLastPair(){
        if(positionsOnMap.size() > 0){
            positionsOnMap.remove(positionsOnMap.size()-1);
            travelDistances.remove(travelDistances.size()-1);
        }
    }

    public int getNumberOfPoints(){
        return positionsOnMap.size();
    }

    public Pair<PositionOnTilemap, Double> getLastPair(){
        int last = positionsOnMap.size()-1;
        if(last != travelDistances.size()-1)
        {
            throw new RuntimeException("positionsOnMap and travelDistances are not of " +
                    "same length");
        }

        if(positionsOnMap.size() == 0){
            return new Pair(startPosition, 0.0);
        }
        else return new Pair(positionsOnMap.get(last), travelDistances.get(last));
    }

    public double getWholeDistance(){
        double distance = 0;
        for(double d:travelDistances) distance+=d;
        return distance;
    }

    public boolean hasMoreThanOnePoint(){
        return positionsOnMap.size() > 0;
    }

    public int[] getDirectionOfLastMove(){
        Point2D lastPosition = getLastPair().getKey().coordsRelativeToMapOrigin();
        Point2D secondLastPosition = positionsOnMap.get(positionsOnMap.size()-2).coordsRelativeToMapOrigin();
        //x, y
        int[] direction = new int[2];
        if (secondLastPosition.getX() == lastPosition.getX()) {
            if (secondLastPosition.getY() > lastPosition.getY()) {
                direction[1] = -1;
            }
            else {
                direction[1] = 1;
            }
        }
        else{
            // nach link oben fahren
            //System.out.print("nach links");
            if (secondLastPosition.getX() > lastPosition.getX()) {
                direction[0] = -1;
            }
            else {
                direction[0] = 1;
            }
        }
        return direction;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public boolean isWait() {
        return wait;
    }

    public void setWait(boolean wait) {
        this.wait = wait;
    }
}
