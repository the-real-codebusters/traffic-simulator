package model;

import java.util.Map;

public class Tile {

    private Map<String, Integer> cornerHeights;
    private boolean isWater;
    private Building building;
    boolean isBuildingOrigin;


    public Tile(Building building, Map<String, Integer> cornerHeights, boolean isWater) {
        this.building = building;
        this.cornerHeights = cornerHeights;
        this.isWater = isWater;
        if(isWater) this.building = null;
    }


    public String absoluteHeigtToRelativeHeight(Map<String, Integer> cornerHeights){
        String absoluteHeight = "";
        int minHeight = Integer.MAX_VALUE;
        for (Integer corner : cornerHeights.values()){
            if (corner < minHeight){
                minHeight = corner;
            }
        }
        for (Map.Entry<String, Integer> entry : cornerHeights.entrySet()){
            entry.setValue(Math.abs(entry.getValue() - minHeight));
            absoluteHeight += entry.getValue();
        }
//        System.out.println( absoluteHeight + " absoluteHeigtToRelativeHeight");
        return absoluteHeight;
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
