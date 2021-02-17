package model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Tile {

    private Map<String, Integer> cornerHeights;
    private boolean isWater;
    private Building building;
    boolean isBuildingOrigin;


    public Tile(Building building, Map<String, Integer> cornerHeights, boolean isWater) {
        this.building = building;
        this.cornerHeights = cornerHeights;
        this.building = building;
        this.isWater = isWater;
    }

    /**
     * Die Höhen der Ecken werden in relative Höhen umgerechnet, so dass jedes Tile einen zugeordneten String
     * erhalten kann, der aus einer 4-stelligen Zahlenkombination aus den Ziffern 0, 1 und 2 besteht.
     * Diese Zahlenkombination wird später für die Zuordnung der korrekten Grafik entsprechend den Höhenverhältnissen
     * benötigt.
     * Beispiel: Absolute Höhe = 3454 -> Relative Höhe = 0121.
     * @param cornerHeights
     * @return
     */
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
        return absoluteHeight;
    }

    /**
     * Der Value wird um den durch heightShift angegebenen Wert verändert, sofern der Value dadurch nicht einen Wert
     * annimt, der < -1 ist
     * @param key Ecke dessen Höhe verändert werden soll
     * @param heightShift Wert, um den die Höhe der Ecke verändert werden soll
     * @return Map mit verändertem Wert
     */
    public Map <String, Integer> updateCornerHeight(String key, int heightShift){
        Map <String, Integer> heights = this.getCornerHeights();

        // Wenn durch die Änderung, der Höhenwert nicht unter -1 senken würde
        if(heights.get(key)+heightShift >= -1){
            heights.put(key, heights.get(key)+heightShift);
        }
        return heights;
    }


    /**
     * Die Höhe einer Ecke wird auf die angegebene Höhe gesetzt
     * @param key Ecke dessen Höhe verändert werden soll
     * @param height Neuer Höhenwert für den gegebenen Key
     * @return Map mit verändertem Wert
     */
    public Map <String, Integer> setHeightForCorner(String key, int height){
        Map <String, Integer> heights = this.getCornerHeights();

        heights.replace(key, height);

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

    public void setCornerHeightsAndUpdateIsWater(Map<String, Integer> cornerHeights) {
        this.cornerHeights = cornerHeights;
        int numberOfCornersUnder0 = 0;
        for (Map.Entry<String, Integer> entry : cornerHeights.entrySet()) {
            if(entry.getValue() < 0) numberOfCornersUnder0++;
        }
        if(numberOfCornersUnder0 >= 2) {
            isWater = true;
            building = null;
        }
        else isWater = false;
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
