package model;

public class VehiclePosition {

    private Tile tilePosition;

    // Wert zwischen 0 und 1 ?
    private double shiftToWidth;

    // Wert zwischen 0 und 1 ?
    private double shiftToDepth;

    public Tile getTilePosition() {
        return tilePosition;
    }

    public void setTilePosition(Tile tilePosition) {
        this.tilePosition = tilePosition;
    }

    public double getShiftToWidth() {
        return shiftToWidth;
    }

    public void setShiftToWidth(double shiftToWidth) {
        this.shiftToWidth = shiftToWidth;
    }

    public double getShiftToDepth() {
        return shiftToDepth;
    }

    public void setShiftToDepth(double shiftToDepth) {
        this.shiftToDepth = shiftToDepth;
    }
}
