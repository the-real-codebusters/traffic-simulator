package model;

import java.util.List;
import java.util.Random;

public class RoadTrafficLine extends TrafficLine{

    public RoadTrafficLine(int desiredNumberOfVehicles, BasicModel model) {
        super(desiredNumberOfVehicles, model, TrafficType.ROAD);
    }

    public void addNewVehicle(){

        // TODO Welches Vehicle?
        List<Vehicle> vehicleTypes = model.getVehiclesForType(TrafficType.ROAD);
        Random randomGenerator = new Random();
        int randomInt = randomGenerator.nextInt(vehicleTypes.size()) -1;
        Vehicle vehicle = vehicleTypes.get(randomInt).getNewInstance();
        System.out.println(vehicle.getKind());
        System.out.println("Speed "+vehicle.getSpeed());
    }

}
