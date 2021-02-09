package model;

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

    public Pair<PositionOnTilemap, Double> getPairOfPositionAndDistance(int i){
        return new Pair<PositionOnTilemap, Double>(positionsOnMap.get(i), travelDistances.get(i));
    }

    public PositionOnTilemap getStartPosition() {
        return startPosition;
    }

    public VehicleMovement(PositionOnTilemap startPosition) {
        this.startPosition = startPosition;
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

        return new Pair(positionsOnMap.get(last), travelDistances.get(last));
    }

    public double getWholeDistance(){
        double distance = 0;
        for(double d:travelDistances) distance+=d;
        return distance;
    }

}
