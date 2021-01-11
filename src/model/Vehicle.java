package model;

public abstract class Vehicle {
    private String kind; // TODO: sollten wir eventuell einen Enum statt einen String verwenden?
    //    graphic
    private boolean canTransportCargo;
    private double speed;
//    private Storage cargo;

    // TODO: Graph oder Stations zu Fahrzeug hinzufügen

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

//    public Storage getCargo() {
//        return cargo;
//    }
//
//    public void setCargo(Storage cargo) {
//        this.cargo = cargo;
//    }
}
