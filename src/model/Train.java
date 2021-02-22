package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Train extends Vehicle {
    private List<Vehicle> wagons;
    private Vehicle engine;
    private List<VehicleMovement> movementsOfLastDay = new ArrayList<>();

    public Train(List<Vehicle> wagons, Vehicle engine) {
        this.wagons = wagons;
        this.engine = engine;
        kind = "train";
    }

    public List<VehicleMovement> getTrainMovementsForNextDay(){
        List<VehicleMovement> movements = new ArrayList<>();
        VehicleMovement engineMovement = getMovementForNextDay();
        engineMovement.setVehicleName(engine.getGraphic());
        movements.add(engineMovement);
        //Letzte Bewegung weglassen
        for(int i=0; i<movementsOfLastDay.size() && i<wagons.size(); i++){
            VehicleMovement movement = movementsOfLastDay.get(i);
            movement.setVehicleName(wagons.get(i).getGraphic());
            movements.add(movement);
        }
        movementsOfLastDay = movements;
        return movements;
    }

    /**
     * Gibt die Positionen des Fahrzeugs innerhalb des nächsten Tags zurück. Entfernt die abgefahrenen Knoten aus
     * pathToNextStation
     * @return Ein VehicleMovement Objekt
     */
    public VehicleMovement getMovementForNextDay(){
        pathToNextStationBeforeMovement = new ArrayList<>(pathToNextStation);
        // Pro Tag sollen so viele Tiles zurückgelegt werden, wie in speed steht
        double wayToGo = speed;
        // Die Bewegung startet an der aktuellen Position
        PositionOnTilemap currentPosition = position;
        VehicleMovement vehicleMovement = new VehicleMovement(currentPosition, graphic, false, trafficType);
        double distanceToNextVertex = 0;
        // Solange der zur Verfügung stehende Weg an dem tag noch nicht verbraucht ist und solange es noch Wegstrecke
        // in pathToNextStation gibt, soll dem vehicleMovement ein Paar aus der nächsten Position, also dem angefahrenen
        // Knoten, und der Länge des Wegs zu diesem Knoten mitgegeben werden
        while(wayToGo >= 0 && pathToNextStation.size() > 0){
            Vertex nextVertex = pathToNextStation.remove(0);
            //TODO Was wenn letzter Knoten aus pathToNextStation erreicht? Am Ziel?
            distanceToNextVertex = currentPosition.getDistanceToPosition(nextVertex);
            vehicleMovement.appendPairOfPositionAndDistance(nextVertex, distanceToNextVertex);
            currentPosition = nextVertex;
            wayToGo -= distanceToNextVertex;
        }
        if(pathToNextStation.size() == 0){
            // Station ist erreicht
            updateNextStation();
            savePathToNextStation((Vertex) currentPosition);
            return vehicleMovement;
        }
        // Ansonsten wurde die Zielstation nicht erreicht

        // Da wayToGo dann vermutlich negativ ist, also nicht genug Weg bis zum nächsten Knoten vorhanden war, muss der
        // letzte Knoten aus dem VehicleMovement wieder entfernt werden und stattdessen eine Position anteilig des übrigen
        // Weges in Richtung des nächsten Knotens hinzugefügt werden

        wayToGo+=distanceToNextVertex;
        pathToNextStation.add(0, (Vertex) currentPosition);
        vehicleMovement.removeLastPair();

        PositionOnTilemap previouslyLastPosition;
        if(vehicleMovement.getNumberOfPoints() == 0){
            System.out.println("way to go "+wayToGo);
            System.out.println("path to next station "+pathToNextStation);
            previouslyLastPosition = vehicleMovement.getStartPosition();
        }
        else previouslyLastPosition = vehicleMovement.getLastPair().getKey();
        VehiclePosition lastPosition = previouslyLastPosition.
                getnewPositionShiftedTowardsGivenPointByGivenDistance(
                        currentPosition.coordsRelativeToMapOrigin(), wayToGo);

        //TODO Was wenn es genau am Ziel landet?

        vehicleMovement.appendPairOfPositionAndDistance(lastPosition, wayToGo);
        return vehicleMovement;
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

}
