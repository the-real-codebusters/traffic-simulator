package model;

public class VehiclePosition {

    private Tile tilePosition;

    // Wert zwischen 0 und 1, Änderung innerhalb des Tiles, ausgehend von der "linken" Ecke auf der Graphik
    private double shiftToWidthInTheTile;

    // Wert zwischen 0 und 1, Änderung innerhalb des Tiles, ausgehend von der "linken" Ecke auf der Graphik
    private double shiftToDepthInTheTile;

    public VehiclePosition(Tile tilePosition, double shiftToWidthInTheTile, double shiftToDepthInTheTile) {
        this.tilePosition = tilePosition;
        this.shiftToWidthInTheTile = shiftToWidthInTheTile;
        this.shiftToDepthInTheTile = shiftToDepthInTheTile;
    }

    public Tile getTilePosition() {
        return tilePosition;
    }

    public void setTilePosition(Tile tilePosition) {
        this.tilePosition = tilePosition;
    }

    public double getShiftToWidthInTheTile() {
        return shiftToWidthInTheTile;
    }

    public void setShiftToWidthInTheTile(double shiftToWidthInTheTile) {
        this.shiftToWidthInTheTile = shiftToWidthInTheTile;
    }

    public double getShiftToDepthInTheTile() {
        return shiftToDepthInTheTile;
    }

    public void setShiftToDepthInTheTile(double shiftToDepthInTheTile) {
        this.shiftToDepthInTheTile = shiftToDepthInTheTile;
    }
}
