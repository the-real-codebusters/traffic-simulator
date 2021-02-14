package model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        // Eine Kopie der Map wird erzeugt, damit die ursprüngliche Map nicht verändert wird
        Map<String, Integer> absoluteCornerHeights = new LinkedHashMap<>();
        absoluteCornerHeights.putAll(cornerHeights);

        String absoluteHeight = "";
        int minHeight = Integer.MAX_VALUE;
        for (Integer corner : cornerHeights.values()){
            if (corner < minHeight){
                minHeight = corner;
            }
        }
        for (Map.Entry<String, Integer> entry : absoluteCornerHeights.entrySet()){
            entry.setValue(Math.abs(entry.getValue() - minHeight));
            absoluteHeight += entry.getValue();
        }
//        System.out.println( absoluteHeight + " absoluteHeigtToRelativeHeight");
        return absoluteHeight;
    }

    public Map <String, Integer> updateCornerHeight(String key, int heightShift){
        Map <String, Integer> heights = this.getCornerHeights();

        // Werte der angegebenen Ecke um 1 erhöhen
        heights.put(key, heights.get(key)+heightShift);
        return heights;
    }


    public int findMaxCorner(Map<String, Integer> cornerHeights){

        int maxHeight = Integer.MIN_VALUE;
        for (Integer corner : cornerHeights.values()){
            if (corner > maxHeight){
                maxHeight = corner;
            }
        }
        return maxHeight;
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
