package model;

import javafx.geometry.Point2D;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Vehicle {
    private TrafficType kind;
    private boolean canTransportCargo;
    private double speed;
    private String graphic;
    private Storage storage;

    private VehiclePosition position;
    private List<Vertex> pathToNextStation = new ArrayList<>();

    // Wenn das false ist, fährt das Fahrzeug zurück, also die Liste der Stationen der Verkehrslinie rückwärts ab
    boolean movementInTrafficLineGoesForward = true;

    private Station nextStation;
    private Pathfinder pathfinder;

    /**
     * Gibt eine neue Instanz des Fahrzeugs zurück
     * @return
     */
    public Vehicle getNewInstance(){
        Vehicle instance = new Vehicle();
        instance.setKind(kind);
        instance.setCanTransportCargo(canTransportCargo);
        instance.setSpeed(speed);
        instance.setGraphic(graphic);
        instance.setStorage(storage.getNewInstance());
        return instance;
    }

    // Wo startet das Fahrzeug?
    // Wo will es hin?
    public void savePathToNextStation(Vertex startVertex){
        pathToNextStation = pathfinder.findPathToDesiredStation(nextStation, startVertex);
    }

    public void updateNextStation() {
        TrafficLine line = nextStation.getTrafficLineForTrafficType(kind);
        nextStation = line.getNextStation(nextStation, movementInTrafficLineGoesForward, this);
    }

    public List<PositionOnTilemap> getPositionsForNextDay(){
        double wayToGo = speed;
        List<PositionOnTilemap> positionsAtOneDay = new ArrayList<>();
        PositionOnTilemap currentPosition = position;
        positionsAtOneDay.add(position);
        double distanceToNextVertex = 0;
        while(wayToGo >= 0){
            Vertex nextVertex = pathToNextStation.remove(0);
            distanceToNextVertex = currentPosition.getDistanceToPosition(nextVertex);
            positionsAtOneDay.add(nextVertex);
            currentPosition = nextVertex;
            wayToGo -= distanceToNextVertex;
        }
        wayToGo+=distanceToNextVertex;
        pathToNextStation.add(0, (Vertex) positionsAtOneDay.remove(positionsAtOneDay.size()-1));


        VehiclePosition lastPosition = positionsAtOneDay.get(positionsAtOneDay.size()-1).
                getnewPositionShiftedTowardsGivenPointByGivenDistance(
                        currentPosition.coordsRelativeToMapOrigin(), wayToGo);
        //TODO Was wenn es genau am Ziel landet?

        positionsAtOneDay.add(lastPosition);
        return positionsAtOneDay;
    }

    /**
     * Lädt eine Ware commodity mit der Menge amount ein. Wenn mehr Waren eingeladen werden sollen als Platz vorhanden
     * ist, wird der Rest der Ware vernichtet.
     * @param commodity
     * @param amount
     * @return
     */
    public void loadCommodity(String commodity, int amount){
        if(amount == 0) return;
        else if(amount < 0) throw new IllegalArgumentException("Argument amount in loadCommodity() was "+amount);

        for(Integer currentAmount: storage.getCargo().values()){
            if(currentAmount != 0) throw new RuntimeException("storage of a vehicle should be empty when loading, but" +
                    "was not");
        }
        storage.changeCargo(commodity, amount);
    }

    /**
     * Lädt die Ware aus und gibt sie zurück.
     * @return Null, wenn keine ladung an Bord war. Ansonsten ein Pair von ausgeladener Ware und Menge der Ware
     */
    public Pair<String, Integer> unloadCommodity(){
        int numberOfLoadedCommodities = 0;
        for(Integer currentAmount: storage.getCargo().values()){
            if(currentAmount > 0) numberOfLoadedCommodities++;
        }

        if(numberOfLoadedCommodities == 0){
            return null;
        }
        else if(numberOfLoadedCommodities > 1) throw new IllegalStateException("In a vehicle was more than one different " +
                "commodity. That should not be the case");
        else {
            String unloadedCommodity = "";
            Integer unloadedAmount = 0;
            Map<String, Integer> cargo = storage.getCargo();
            for(String commodity: cargo.keySet()){
                Integer amount = cargo.get(commodity);
                if(amount > 0) {
                    unloadedAmount = amount;
                    unloadedCommodity = commodity;
                    break;
                }
            }
            return new Pair(unloadedCommodity, unloadedAmount);
        }
    }

    public TrafficType getKind() {
        return kind;
    }

    public void setKind(TrafficType kind) {
        this.kind = kind;
    }

    public boolean isCanTransportCargo() {
        return canTransportCargo;
    }

    public void setCanTransportCargo(boolean canTransportCargo) {
        this.canTransportCargo = canTransportCargo;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public String getGraphic() {
        return graphic;
    }

    public void setGraphic(String graphic) {
        this.graphic = graphic;
    }

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public VehiclePosition getPosition() {
        return position;
    }

    public void setPosition(VehiclePosition position) {
        this.position = position;
    }

    public boolean isMovementInTrafficLineGoesForward() {
        return movementInTrafficLineGoesForward;
    }

    public void setMovementInTrafficLineGoesForward(boolean movementInTrafficLineGoesForward) {
        this.movementInTrafficLineGoesForward = movementInTrafficLineGoesForward;
    }

    public Station getNextStation() {
        return nextStation;
    }

    public void setNextStation(Station nextStation) {
        this.nextStation = nextStation;
    }
}
