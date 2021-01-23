package model;

public class Vehicle {
    private String kind; // TODO: sollten wir eventuell einen Enum statt einen String verwenden?
    private boolean canTransportCargo;
    private double speed;
    private String graphic;
//    private Storage cargo;

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

    //    public Storage getCargo() {
//        return cargo;
//    }
//
//    public void setCargo(Storage cargo) {
//        this.cargo = cargo;
//    }
}
