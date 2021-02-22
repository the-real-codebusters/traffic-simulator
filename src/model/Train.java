package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Train extends Vehicle {
    private List<Vehicle> wagons = new ArrayList<>();
    private Vehicle engine;
//    private List<VehicleMovement> movementsOfLastDay = new ArrayList<>();
    private final double wagonShift = 0.97;

    public Train(List<Vehicle> wagons, Vehicle engine) {
        trafficType = TrafficType.RAIL;
        canTransportCargo = true;
        speed = engine.getSpeed();
        storage = new Storage(new HashMap<>());
        kind = "train";

        for(Vehicle wagon: wagons){
            addWagon(wagon);
        }
        setEngine(engine);
    }

    public List<VehicleMovement> getTrainMovementsForNextDay(){
        List<VehicleMovement> movements = new ArrayList<>();
        VehicleMovement engineMovement = getMovementForNextDay();
        engineMovement.setVehicleName(engine.getGraphic());
        movements.add(engineMovement);
        //Letzte Bewegung weglassen
        for(int i=1; i<=wagons.size(); i++){
            int[] direction = reverseDirection(engineMovement.getDirectionOfLastMove());
            VehicleMovement movement =
                    engineMovement.getNewShiftedMovement(wagonShift*i, direction, wagons.get(i-1).getGraphic());
            movements.add(movement);
        }
        return movements;
    }

    private int[] reverseDirection(int[] direction){
        int[] nDirection = new int[2];
        nDirection[0] = direction[0]*-1;
        nDirection[1] = direction[1]*-1;
        return nDirection;
    }

    public List<Vehicle> getWagons() {
        return wagons;
    }

    public Vehicle getEngine() {
        return engine;
    }

    public void addWagon(Vehicle wagon){

        if(! wagon.getKind().equals("wagon") ) throw new IllegalArgumentException("Attribute was not of kind wagon");

        if(wagon.getSpeed() < this.getSpeed()) setSpeed(wagon.getSpeed());

        Map<String, Integer> addedStorage = wagon.getStorage().getMaxima();
        for (Map.Entry<String, Integer> entry : addedStorage.entrySet()) {
            String commodity = entry.getKey();
            Integer amount = entry.getValue();
            Map<String, Integer> actualStorage = storage.getMaxima();

            if(actualStorage.containsKey(commodity)){
                actualStorage.replace(commodity, actualStorage.get(commodity) + amount);
            }
            else {
                actualStorage.put(commodity, amount);
            }
        }

        wagons.add(wagon);
    }

    public void setEngine(Vehicle engine){
        if(! engine.getKind().equals("engine") ) throw new IllegalArgumentException("Attribute was not of kind engine");

        if(engine.getSpeed() < this.getSpeed()) setSpeed(engine.getSpeed());

        this.engine = engine;
    }

    /**
     * Gibt eine neue Instanz des Fahrzeugs zurück
     * @return
     */
    @Override
    public Vehicle getNewInstance(){
        //Vorsicht, hier wird eagons und engine einfach weiter verwendet
        Train instance = new Train(wagons, engine);
        instance.setTrafficType(trafficType);
        instance.setCanTransportCargo(canTransportCargo);
        instance.setSpeed(speed);
        instance.setGraphic(graphic);
        instance.setKind(kind);
        instance.setStorage(storage.getNewInstance());
        return instance;
    }


}
