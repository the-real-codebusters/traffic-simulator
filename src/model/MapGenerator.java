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
        Tile[][] mapFieldGrid = mapModel.getTileGrid();

        generateNature(mapWidth, mapDepth, basicModel);
        generateFactories(mapWidth, mapDepth, basicModel);
        generateHeightMap();

        return mapFieldGrid;
    }

    /**
     * Generiert einige Fabriken auf die Map. Mindestens eine Fabrik jeden Typs
     * @param mapWidth
     * @param mapDepth
     * @param basicModel
     */
    private void generateFactories(int mapWidth, int mapDepth, BasicModel basicModel){
        List<Special> factoryBuildings = basicModel.getBuildingsForSpecialUse("factory");
        Random randomGenerator = new Random();
        for (Building factory : factoryBuildings) {
            int maxNumberOfPlacements = ((mapDepth * mapWidth) / (factory.getDepth() * factory.getWidth())) / 100;
            int numberOfPlacements;
            if (maxNumberOfPlacements == 0) numberOfPlacements = 1;
            else numberOfPlacements = randomGenerator.nextInt(maxNumberOfPlacements) + 1;
            int maxColumn = mapWidth - factory.getDepth() - 1;
            int maxRow = mapDepth - factory.getWidth() - 1;
            while (numberOfPlacements > 0) {
                int row = randomGenerator.nextInt(maxRow);
                int column = randomGenerator.nextInt(maxColumn);
                if (mapModel.canPlaceBuilding(row, column, factory)) {
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
     *
     * @param mapWidth
     * @param mapDepth
     * @param basicModel
     */
    private void generateNature(int mapWidth, int mapDepth, BasicModel basicModel) {
        List<Special> natureBuildings = basicModel.getBuildingsForSpecialUse("nature");

        Tile[][] mapFieldGrid = mapModel.getTileGrid();

        //TODO Hier steht teilweise veralteter Code

        for (int row = 0; row < mapDepth; row++) {
            for (int col = 0; col < mapWidth; col++) {
//                //propability für Wasser
//                int probWater = 1;
//                if (row > 1 && col > 1) {
//                    //Wahrscheinlichkeit für das Erscheinen von Wasser neben Wasser erhöhen
//                    if (mapFieldGrid[row - 1][col].isWater()) probWater += 345;
//                    if (mapFieldGrid[row][col - 1].isWater()) probWater += 500;
//                    if (mapFieldGrid[row - 2][col].isWater()) probWater += 90;
//                    if (mapFieldGrid[row][col - 2].isWater()) probWater += 50;
//                }
//
//
                Building building = null;
//                int heightRandom = new Random().nextInt(1000);
//                if (heightRandom <= probWater ) {
//                    Map<String, Integer> cornerHeights = new LinkedHashMap<>();
//                    cornerHeights.put("cornerN", -1);
//                    cornerHeights.put("cornerE", -1);
//                    cornerHeights.put("cornerS", -1);
//                    cornerHeights.put("cornerW", -1);
//                    //TODO && false löschen
//
//                    mapFieldGrid[row][col] = new Tile(null, cornerHeights, true);
//                }
//
//                else {

                    //Wenn kein Wasser gesetzt ist, andere Höhen setzen
                    int buildingRandom = new Random().nextInt(natureBuildings.size());
                    building = natureBuildings.get(buildingRandom).getNewInstance();
                    Map<String, Integer> heightMap = new HashMap<>();
                    heightMap.put("cornerN", 0);
                    heightMap.put("cornerE", 0);
                    heightMap.put("cornerS", 0);
                    heightMap.put("cornerW", 0);
                    mapFieldGrid[row][col] = new Tile(building, heightMap, false);

                }
            }
//        }
    }


    /**
     * Iteriert durch Tiles des Spielfelds und erzeugt für jedes Tile eine Map, die die Ecken auf ihre Höhe abbildet,
     * wobei die Höhen der Ecken sowohl innerhalb eines Tiles als auch der Tiles zueinander stimmig sein müssen
     */
    public void generateHeightMap() {
        int mapWidth = mapModel.getWidth();
        int mapDepth = mapModel.getDepth();
        Tile[][] mapFieldGrid = mapModel.getTileGrid();

        for (int row = 0; row < mapDepth; row++) {
            for (int col = 0; col < mapWidth; col++) {

//                if (!mapFieldGrid[row][col].isWater() && mapFieldGrid[row][col].getBuilding() instanceof Nature) {

                // Setze höhen der ecken für erste zeile und erste spalte auf 0
                    if (row == 0 || col == 0) {
                        Map<String, Integer> map = new LinkedHashMap<>();
                        map.put("cornerN", 0);
                        map.put("cornerE", 0);
                        map.put("cornerS", 0);
                        map.put("cornerW", 0);
                        mapFieldGrid[row][col].setCornerHeights(map);
//                        System.out.println("row: " + row + " col: " + col + " " + map);

                        // Erzeuge Höhen für alle Felder, die nicht in der ersten Zeile oder in der ersten Spalte sind
                    } else if (row != 0 && col != 0) {
                        int heightOfCornerN = mapFieldGrid[row][col - 1].getCornerHeights().get("cornerN");
                        int heightOfCornerERow = mapFieldGrid[row][col - 1].getCornerHeights().get("cornerE");
                        int heightOfCornerECol = mapFieldGrid[row - 1][col].getCornerHeights().get("cornerE");

                        Map<String, Integer> map = generateTileHeightMiddle(heightOfCornerN, heightOfCornerECol, heightOfCornerERow);
                        mapFieldGrid[row][col].setCornerHeights(map);
                    System.out.println("row: " + row + " col: " +  col + " " + map);
                    }
//                    Map<String, Integer> cornerHeights = mapFieldGrid[row][col].getCornerHeights();
//                    System.out.println(row + " " + col + " " + cornerHeights);
//                    String absoluteTileHeight = mapFieldGrid[row][col].absoluteHeigtToRelativeHeight(cornerHeights);
//                    System.out.println(row + " " + col + " " + absoluteTileHeight);
//
//                    Map<String, Integer> heightMap = new HashMap<>();
//                    heightMap.put("cornerN", 0);
//                    heightMap.put("cornerE", 0);
//                    heightMap.put("cornerS", 0);
//                    heightMap.put("cornerW", 0);
//
//                    Building ground = new Building();
//                    mapFieldGrid[row][col].setCornerHeights(cornerHeights);
//                    ground.setBuildingName("ground");
//                    ground.setWidth(1);
//                    ground.setDepth(1);
//                    mapFieldGrid[row][col].setBuilding(ground);
//                }
            }
        }
    }


    /**
     * Generiert Höhen für die Tiles, die sich weder in der ersten Zeile noch in der ersten Spalte befinden.
     * Die Werte der übergebenen Parameter sind die Höhen der "Vorgängerfelder", die bei der Erzeugung der Höhen des
     * aktuellen Feldes berücksichtigt und beibehalten werden müssen.
     * @param cornerW
     * @param cornerN
     * @param cornerS
     * @return eine Map, die die Ecken eines Tiles auf die zugehörige Höhe abbildet
     */
    public Map<String, Integer> generateTileHeightMiddle(int cornerW , int cornerN, int cornerS) {
        Map<String, Integer> cornerHeights = new LinkedHashMap<>();

        int cornerE = generateHeightForCorner(cornerN);

        if (Math.abs(cornerS - cornerE) > 1) {
//            int digit = new Random().nextInt(1) + 1;
            int digit = 1;
            if (Math.abs(cornerS - cornerE) > 2) {
//                System.out.println("diff > 2 " + cornerS + " " + cornerE);
                digit = 2;
            }
            if (cornerS > cornerE) cornerE = cornerE + digit;
            else cornerE = cornerE - digit;
        }

        // Füge gefundene Höhen der Map hinzu
        cornerHeights.put("cornerN", cornerN);
        cornerHeights.put("cornerE", cornerE);
        cornerHeights.put("cornerS", cornerS);
        cornerHeights.put("cornerW", cornerW);

        return cornerHeights;
    }

    /**
     * Generiert eine Zahl für die Ecke eines Tiles unter berücksichtigung der Höhe der vorherigen benachbarten Ecke
     *
     * @param heightOfEdgeBefore von dieser Ecke ausgehend wird die Höhe der nächsten benachbarten Kante generiert
     * @return Höhe einer Ecke
     */
    public int generateHeightForCorner(int heightOfEdgeBefore) {
        Random r = new Random();

        int minHeight;
        int maxHeight;
        if (heightOfEdgeBefore != -3) {
            minHeight = heightOfEdgeBefore - 1;
        } else {
            minHeight = heightOfEdgeBefore;
        }

        if (heightOfEdgeBefore == 5) {
            maxHeight = heightOfEdgeBefore;
        } else {
            maxHeight = heightOfEdgeBefore + 1;
        }

//        int heightOfNextCorner = r.nextInt(maxHeight - minHeight + 1) + minHeight;

        //TODO Wenn man die Wahrscheinlichkeiten probabilityMinHeight und probabilityMaxHeight verkleinert, treten sehr
        //komische Probleme auf. Was ist da los?

        int heightOfNextCorner;
        if(heightOfEdgeBefore > 0){
            int probabilityMinHeight = 40;
            int randomNumber = r.nextInt(100)+1;
            if(randomNumber < probabilityMinHeight){
                heightOfNextCorner = minHeight;
            }
            else {
                heightOfNextCorner = r.nextInt(maxHeight - minHeight + 1) + minHeight;
            }
        }
        else if (heightOfEdgeBefore < 0) {
            int probabilityMaxHeight = 40;
            int randomNumber = r.nextInt(100)+1;
            if(randomNumber < probabilityMaxHeight){
                heightOfNextCorner = maxHeight;
            }
            else {
                heightOfNextCorner = r.nextInt(maxHeight - minHeight + 1) + minHeight;
            }
        }
        else {
            int probabilityNullHeight = 90;
            int randomNumber = r.nextInt(100)+1;
            if(randomNumber < probabilityNullHeight){
                heightOfNextCorner = 0;
            }
            else {
                heightOfNextCorner = r.nextInt(maxHeight - minHeight + 1) + minHeight;
            }
        }
        return heightOfNextCorner;
    }
}
