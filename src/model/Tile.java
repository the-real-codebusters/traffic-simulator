package model;

import java.util.Map;

public class Tile {

    private Map<String, Integer> cornerHeights;
    private boolean isWater;
    private Building building;
    boolean isBuildingOrigin;


    public Tile(Building building, Map<String, Integer> cornerHeights) {
        this.building = building;
        this.cornerHeights = cornerHeights;
        this.building = building;
    }


    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }

    public Map<String, Integer> getCornerHeights() {
        return cornerHeights;
    }

    public void setCornerHeights(Map<String, Integer> cornerHeights) {
        this.cornerHeights = cornerHeights;
    }

    public boolean isBuildingOrigin() {
        return isBuildingOrigin;
    }

    public void setBuildingOrigin(boolean buildingOrigin) {
        isBuildingOrigin = buildingOrigin;
    }

    public boolean isWater() {
        return isWater;
    }
}
