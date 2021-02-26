package model;

import java.util.ArrayList;
import java.util.List;

//Aktuell noch nicht benutzt
public class Plane extends Vehicle{

    List<VehicleMovement> movements = new ArrayList<>();


    /**
     * Speichert den Weg zur nächsten Station ab
     * @param startVertex
     */
    @Override
    public void savePathToNextStation(Vertex startVertex){
        movements.clear();
        pathToNextStation = pathfinder.findPathToDesiredStation(nextStation, startVertex, trafficType);
        while(pathToNextStation.size() != 0) {
            VehicleMovement movement = getMovementForNextDay();
            movements.add(movement);
        }
    }

//    public VehicleMovement getPlaneMovementForNextDay(){
//
//    }
}
