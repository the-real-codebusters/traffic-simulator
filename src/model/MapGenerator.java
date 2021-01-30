package model;

import java.util.*;

public class MapGenerator {
    private String generatorName;
    private MapModel mapModel;

    public MapGenerator(String generatorName, MapModel mapModel) {
        this.generatorName = generatorName;
        this.mapModel = mapModel;
    }

    public Tile[][] generateMap(BasicModel basicModel) {

        int mapWidth = mapModel.getWidth();
        int mapDepth = mapModel.getDepth();
        Tile[][] mapFieldGrid = mapModel.getFieldGrid();

        generateNature(mapWidth, mapDepth, basicModel);
        generateFactories(mapWidth, mapDepth, basicModel);

        return mapFieldGrid;
    }

    private void generateFactories(int mapWidth, int mapDepth, BasicModel basicModel){
        List<Special> factoryBuildings = basicModel.getBuildingsForSpecialUse("factory");
        Random randomGenerator = new Random();
        for(Building factory: factoryBuildings){
            int maxNumberOfPlacements = ((mapDepth*mapWidth)/(factory.getDepth()*factory.getWidth()))/100;
            int numberOfPlacements;
            if(maxNumberOfPlacements==0) numberOfPlacements = 1;
            else numberOfPlacements = randomGenerator.nextInt(maxNumberOfPlacements)+1;
            int maxColumn = mapWidth - factory.getDepth()-1;
            int maxRow = mapDepth - factory.getWidth()-1;
            while(numberOfPlacements > 0){
                int row = randomGenerator.nextInt(maxRow);
                int column = randomGenerator.nextInt(maxColumn);
                if(mapModel.canPlaceBuilding(row, column, factory)) {
                    mapModel.placeBuilding(row, column, factory);
                    numberOfPlacements--;
                }
            }
        }

    }

    /**
     * Generiert nature-buildings und setzt sie an zufälliger Position auf die Karte.
     * Generiert außerdem zufällig Höhen zwischen -1 und 4 für jedes Tile.
     * Tiles mit Höhe -1, also Wasserfelder, haben eine deutlich höhere Wahrscheinlichkeit wenn ein Wasserfeld
     * benachbart ist.
     * @param mapWidth
     * @param mapDepth
     * @param basicModel
     */
    private void generateNature(int mapWidth, int mapDepth, BasicModel basicModel) {
        List<Special> natureBuildings = basicModel.getBuildingsForSpecialUse("nature");

        Tile[][] mapFieldGrid = mapModel.getFieldGrid();

        for (int row = 0; row < mapDepth; row++) {
            for (int col = 0; col < mapWidth; col++) {
                //propability für Wasser
                int probWater = 1;
                if(row > 1 && col > 1) {
                    //Wahrscheinlichkeit für das Erscheinen von Wasser neben Wasser erhöhen
                    if(mapFieldGrid[row-1][col].isWater()) probWater+=345;
                    if(mapFieldGrid[row][col-1].isWater()) probWater+=500;
                    if(mapFieldGrid[row-2][col].isWater()) probWater+=90;
                    if(mapFieldGrid[row][col-2].isWater()) probWater+=50;
                }


                Building building = null;
                int heightRandom =  new Random().nextInt(1000);
                if(heightRandom <= probWater) {
                    Map<String, Integer> cornerHeights = new LinkedHashMap<>();
                    cornerHeights.put("cornerN", -1);
                    cornerHeights.put("cornerE", -1);
                    cornerHeights.put("cornerS", -1);
                    cornerHeights.put("cornerW", -1);

                    mapFieldGrid[row][col] = new Tile(null, cornerHeights, true);
                }

                //Wenn kein Wasser gesetzt ist, andere Höhen setzen
                else {
                    int buildingRandom = new Random().nextInt(natureBuildings.size());
                    building = natureBuildings.get(buildingRandom).getNewInstance();
                    mapFieldGrid[row][col] = new Tile(building, generateTileHeight(), false);
                }

            }
        }
    }

    /**
     * Generiert Höhen für die vier Ecken eines Tiles unter Berücksichtigung der Einschränkungen (benachbarte Ecken
     * dürfen einen Höhenunterschied von max. 1 haben und diagonal gegenüberliegende Ecken max. 2
     * @return eine Map, die die Ecken eines Tiles auf die zugehörige Höhe abbildet
     */
    public Map<String, Integer> generateTileHeight(){
        Map<String, Integer> cornerHeights = new LinkedHashMap<>();

        int cornerN = new Random().nextInt(5);
        int cornerE = generateHeightForCorner(cornerN);
        int cornerS = generateHeightForCorner(cornerE);
        int cornerW = generateHeightForCorner(cornerS);

        // prüfe ob max. Höhenunterschied zwischen gegenüberliegenden Ecken eingehalten wird
        // falls nicht, ändere den Wert
        if(Math.abs(cornerN - cornerW) > 1){
            int digit = new Random().nextInt(1) + 1;
            if (Math.abs(cornerN - cornerW) > 2) {
                digit = 2;
            }
            if(cornerN > cornerW) cornerW = cornerW + digit;
            else cornerW = cornerW - digit;
        }

        // Füge gefundene Höhen der Map hinzu
        cornerHeights.put("cornerN", cornerN);
        cornerHeights.put("cornerE", cornerE);
        cornerHeights.put("cornerS", cornerS);
        cornerHeights.put("cornerW", cornerW);

//        System.out.println(cornerN + "" + cornerE + "" + cornerS + "" + cornerW);

//        for (Map.Entry<String, Integer> entry : cornerHeights.entrySet()) {
//            System.out.print(entry.getKey() + ": " + entry.getValue() + "  ");
//        }
//        System.out.println();

        return cornerHeights;
    }


    /**
     * Generiert eine Zahl für die Ecke eines Tiles unter berücksichtigung der Höhe der vorherigen Ecke
     * @param digitBefore von dieser Ecke ausgehend wird die Höhe der nächsten benachbarten Kante generiert
     * @return Höhe einer Ecke
     */
    public int generateHeightForCorner(int digitBefore){
        Random r = new Random();

        int minHeight;
        int maxHeight;
        if(digitBefore != 0){
            minHeight = digitBefore-1;
        } else {
            minHeight = digitBefore;
        }

        if(digitBefore == 9){
            maxHeight = digitBefore;
        } else {
            maxHeight = digitBefore +1;
        }

        int heightOfNextCorner = r.nextInt(maxHeight-minHeight +1) + minHeight;

        return heightOfNextCorner;
    }
}
