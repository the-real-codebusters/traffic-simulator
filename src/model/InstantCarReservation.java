package model;

public class InstantCarReservation {
    private int tileX;
    private int tileY;
    private VehicleMovement movement;

    public InstantCarReservation(int tileX, int tileY, VehicleMovement movement) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.movement = movement;
    }

    public VehicleMovement getMovement() {
        return movement;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InstantCarReservation that = (InstantCarReservation) o;

        if (tileX != that.tileX) return false;
        return tileY == that.tileY;
    }

    @Override
    public int hashCode() {
        int result = tileX;
        result = 31 * result + tileY;
        return result;
    }
}
