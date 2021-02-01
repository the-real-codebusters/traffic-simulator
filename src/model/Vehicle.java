package model;

import javafx.util.Pair;

import java.util.Map;

public class Vehicle {
    private String kind; // TODO: sollten wir eventuell einen Enum statt einen String verwenden?
    private boolean canTransportCargo;
    private double speed;
    private String graphic;
    private Storage storage;

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

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
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
}
