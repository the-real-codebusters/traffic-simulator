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
    private TrafficType trafficType;
    boolean lastMovementBeforeRemove = false;
    private Vehicle vehicle;


    public VehicleMovement(PositionOnTilemap startPosition, String vehicleName, boolean wait, TrafficType trafficType, Vehicle vehicle) {
        this.vehicleName = vehicleName;
        this.startPosition = startPosition;
        this.wait = wait;
        this.trafficType = trafficType;
        this.vehicle = vehicle;
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
        Point2D lastPosition;
        Point2D secondLastPosition;
        if(positionsOnMap.size()>1){
            lastPosition = getLastPair().getKey().coordsRelativeToMapOrigin();
            secondLastPosition = positionsOnMap.get(positionsOnMap.size()-2).coordsRelativeToMapOrigin();
        }
        else {
            secondLastPosition = startPosition.coordsRelativeToMapOrigin();
            lastPosition = getLastPair().getKey().coordsRelativeToMapOrigin();
        }
        //x, y
//        System.out.println("getDirectionOfLastMove: secondLastPositionX:"+secondLastPosition.getX()+" lastPositionX: "+lastPosition.getX());
//        System.out.println("getDirectionOfLastMove: secondLastPositionY:"+secondLastPosition.getY()+" lastPositionY: "+lastPosition.getY());

        int[] direction = new int[] {0,0};
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
//        System.out.println("getDirectionOfLastMove "+direction[0] +"   "+direction[1]);
        return direction;
    }

    public int[] getDirectionOfFirstMove(){
        Point2D secondPosition;
        Point2D firstPosition;
        if(positionsOnMap.size()>0){
            secondPosition = positionsOnMap.get(0).coordsRelativeToMapOrigin();
            firstPosition = startPosition.coordsRelativeToMapOrigin();
        }
        else {
            secondPosition = startPosition.coordsRelativeToMapOrigin();
            firstPosition = startPosition.coordsRelativeToMapOrigin();
        }

        int[] direction = new int[] {0,0};
        if (firstPosition.getX() == secondPosition.getX()) {
            if (firstPosition.getY() > secondPosition.getY()) {
                direction[1] = -1;
            }
            else {
                direction[1] = 1;
            }
        }
        else{
            // nach links oben fahren
            if (firstPosition.getX() > secondPosition.getX()) {
                direction[0] = -1;
            }
            else {
                direction[0] = 1;
            }
        }
        return direction;
    }

    public boolean checkIfDirectionsStayTheSame(){
        int[] first = getDirectionOfFirstMove();
        int[] last = getDirectionOfLastMove();
        return first[0] == last[0] && first[1] == last[1];
    }

    public boolean checkIfSameLastDirection(int[] otherDirection){
        int[] last = getDirectionOfLastMove();
        return otherDirection[0] == last[0] && otherDirection[1] == last[1];
    }

    public List<PositionOnTilemap> getAllPositions(){
        List<PositionOnTilemap> pos = new ArrayList<>(positionsOnMap);
        pos.add(startPosition);
        return pos;
    }

    public VehicleMovement getNewShiftedMovement(double shift, int direction[], String vehicleName, Vehicle vehicle){
        Point2D shiftedPoint = startPosition.coordsRelativeToMapOrigin().add(direction[0] * shift, direction[1]*shift);
        PositionOnTilemap startP = startPosition.getnewPositionShiftedTowardsGivenPointByGivenDistance(shiftedPoint, shift);
        VehicleMovement shiftedMovement = new VehicleMovement(startP, vehicleName, wait, trafficType, vehicle);
        for(int i=0; i<positionsOnMap.size(); i++){
            shiftedPoint = positionsOnMap.get(i).coordsRelativeToMapOrigin().add(direction[0] * shift, direction[1]*shift);
            PositionOnTilemap pos = positionsOnMap.get(i).getnewPositionShiftedTowardsGivenPointByGivenDistance(shiftedPoint, shift);
            shiftedMovement.appendPairOfPositionAndDistance(pos, travelDistances.get(i));
        }
        return shiftedMovement;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public Vehicle getVehicle() {
        return vehicle;
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

    public TrafficType getTrafficType() {
        return trafficType;
    }

    public boolean isLastMovementBeforeRemove() {
        return lastMovementBeforeRemove;
    }

    public void setLastMovementBeforeRemove(boolean lastMovementBeforeRemove) {
        this.lastMovementBeforeRemove = lastMovementBeforeRemove;
    }
}
