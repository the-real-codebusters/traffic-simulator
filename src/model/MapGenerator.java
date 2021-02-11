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
        generateHeightMap();
        generateFactories(mapWidth, mapDepth, basicModel);
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
        System.out.println("generate nature called");
        List<Special> natureBuildings = basicModel.getBuildingsForSpecialUse("nature");

        Tile[][] mapFieldGrid = mapModel.getTileGrid();

        HashMap<Building, Integer> natureStartProbabilities = new HashMap<>();
        for(Building nature: natureBuildings){
            natureStartProbabilities.put(nature, 3/natureBuildings.size()>=1?3/natureBuildings.size():1);
        }

        int percentPointsIfEqualNatureBuildingNear = 25;
        for (int row = 0; row < mapDepth; row++) {
            for (int col = 0; col < mapWidth; col++) {
                Map<Building, Integer> natureProbabilities = new HashMap<>();
                natureProbabilities.putAll(natureStartProbabilities);
                if(row > 0){
                    Building rowNearBuilding = mapFieldGrid[row-1][col].getBuilding();
                    if(natureProbabilities.containsKey(rowNearBuilding)){
                        natureProbabilities.replace(rowNearBuilding, natureProbabilities.get(rowNearBuilding)+percentPointsIfEqualNatureBuildingNear);
                    }
                }
                if(col > 0){
                    Building colNearBuilding = mapFieldGrid[row][col-1].getBuilding();
                    if(natureProbabilities.containsKey(colNearBuilding)){
                        natureProbabilities.replace(colNearBuilding, natureProbabilities.get(colNearBuilding)+percentPointsIfEqualNatureBuildingNear);
                    }
                }
                if(col > 0 && row>0){
                    Building colNearBuilding = mapFieldGrid[row-1][col-1].getBuilding();
                    if(natureProbabilities.containsKey(colNearBuilding)){
                        natureProbabilities.replace(colNearBuilding, natureProbabilities.get(colNearBuilding)+percentPointsIfEqualNatureBuildingNear);
                    }
                }
                int probCounter = 0;
                int random = new Random().nextInt(99)+1;
                Building building = new Building(1, 1, "grass");
                for (Map.Entry<Building, Integer> entry : natureProbabilities.entrySet()) {
                    probCounter+=entry.getValue();
                    if(random <= probCounter){
                        building = entry.getKey();
                        break;
                    }
                }

                    Map <String, Integer> heightMap = new HashMap<>();
                    heightMap.put("cornerN", 0);
                    heightMap.put("cornerE", 0);
                    heightMap.put("cornerS", 0);
                    heightMap.put("cornerW", 0);
                    mapFieldGrid[row][col] = new Tile(building, heightMap);
            }
        }
    }


    /**
     * Iteriert durch Tiles des Spielfelds und erzeugt für jedes Tile eine Map, die die Ecken auf ihre Höhe abbildet,
     * wobei die Höhen der Ecken sowohl innerhalb eines Tiles als auch der Tiles zueinander stimmig sein müssen
     */
    public void generateHeightMap() {
        int mapWidth = mapModel.getWidth();
        int mapDepth = mapModel.getDepth();
        Tile[][] mapFieldGrid = mapModel.getTileGrid();

        System.out.println("generate height map called");

        for (int row = 0; row < mapDepth; row++) {
            for (int col = 0; col < mapWidth; col++) {

                // Erzeuge Höhe für das Feld mit Koordinaten [0,0] und setze die erzeugten Höhen für das Feld
                if(row == 0 && col == 0){
                    Map<String, Integer> map = generateTileHeight();
                    mapFieldGrid[0][0].setCornerHeightsAndUpdateIsWater(map);
//                    System.out.println("row: " + row + " col: " +  col + " " + map);

                // Erzeuge Höhen für die erste Zeile ausschließlich des Felds[0,0]
                } else if (row == 0 && col != 0){
                    // Holt sich die Werte der Ecken des "Vorgängerfeldes" und gibt diese als Parameter in der Methode
                    // zur Erzeugung der Höhen mit, damit diese Werte berücksichtigt werden können
                    int heightOfCornerN = mapFieldGrid[0][col-1].getCornerHeights().get("cornerN");
                    int heightOfCornerE = mapFieldGrid[0][col-1].getCornerHeights().get("cornerE");

                    Map<String, Integer> map = generateTileHeightFirstRow(heightOfCornerN, heightOfCornerE);

                    mapFieldGrid[0][col].setCornerHeightsAndUpdateIsWater(map);
//                    System.out.println("row: " + row + " col: " +  col + " " + map);

                // Erzeuge Höhen für die erste Spalte ausschließlich des Felds[0,0]
                } else if (row != 0 && col == 0){
                    int heightOfCornerS = mapFieldGrid[row-1][0].getCornerHeights().get("cornerS");
                    int heightOfCornerE = mapFieldGrid[row-1][0].getCornerHeights().get("cornerE");

                    Map<String, Integer> map = generateTileHeightFirstColumn(heightOfCornerS, heightOfCornerE);

                    mapFieldGrid[row][0].setCornerHeightsAndUpdateIsWater(map);
                    System.out.println("row: " + row + " col: " +  col + " " + map);

                // Erzeuge Höhen für alle Felder, die nicht in der ersten Zeile oder in der ersten Spalte sind
                } else if (row != 0 && col != 0){
                    int heightOfCornerN = mapFieldGrid[row][col-1].getCornerHeights().get("cornerN");
                    int heightOfCornerERow = mapFieldGrid[row][col-1].getCornerHeights().get("cornerE");
                    int heightOfCornerECol = mapFieldGrid[row-1][col].getCornerHeights().get("cornerE");

                    Map<String, Integer> map = generateTileHeightMiddle(heightOfCornerN, heightOfCornerECol, heightOfCornerERow);
                    mapFieldGrid[row][col].setCornerHeightsAndUpdateIsWater(map);
//                    System.out.println("row: " + row + " col: " +  col + " " + map);
                }
            }
        }

        System.out.println("links: " + mapFieldGrid[16][21].getCornerHeights());
        System.out.println("unten: " + mapFieldGrid[17][20].getCornerHeights());
        System.out.println("zu prüfen: " + mapFieldGrid[17][21].getCornerHeights());
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

        // Generiere Höhe für die fehlende Ecke
        int cornerE = generateHeightForCorner(cornerN);

        // Stelle sicher, dass Höhenunterschied zwischen Süd und Ost immer noch innerhalb der erlaubten Toleranz liegt
        if (Math.abs(cornerS - cornerE) > 1) {
            int digit = new Random().nextInt(2);
            if (Math.abs(cornerS - cornerE) > 2) {
                digit = 2;
            }
            if (cornerS > cornerE) cornerE = cornerE + digit;
            else cornerE = cornerE - digit;
        }

        // Füge Höhen der Map hinzu
        cornerHeights.put("cornerN", cornerN);
        cornerHeights.put("cornerE", cornerE);
        cornerHeights.put("cornerS", cornerS);
        cornerHeights.put("cornerW", cornerW);

        return cornerHeights;
    }

    /**
     * Generiert Höhen für die Tiles in der ersten Spalte
     * Die Werte der übergebenen Parameter sind die Höhen des "Vorgängerfelds", die bei der Erzeugung der Höhen des
     * aktuellen Feldes berücksichtigt und beibehalten werden müssen.
     * @param cornerW
     * @param cornerN
     * @return eine Map, die die Ecken eines Tiles auf die zugehörige Höhe abbildet
     */
    public Map<String, Integer> generateTileHeightFirstColumn(int cornerW , int cornerN) {
        Map<String, Integer> cornerHeights = new LinkedHashMap<>();

        int cornerE = generateHeightForCorner(cornerN);
        int cornerS = generateHeightForCorner(cornerE);


        if (Math.abs(cornerS - cornerW) > 1) {
            int digit = new Random().nextInt(1) + 1;
            if (Math.abs(cornerS - cornerW) > 2) {
                digit = 2;
            }
            if (cornerS > cornerW) cornerS = cornerS - digit;
            else cornerS = cornerS + digit;
        }

        cornerHeights.put("cornerW", cornerW);
        cornerHeights.put("cornerN", cornerN);
        cornerHeights.put("cornerE", cornerE);
        cornerHeights.put("cornerS", cornerS);

        return cornerHeights;
    }

    /**
     * Generiert Höhen für die Tiles in der ersten Zeile
     * Die Werte der übergebenen Parameter sind die Höhen des "Vorgängerfelds", die bei der Erzeugung der Höhen des
     * aktuellen Feldes berücksichtigt und beibehalten werden müssen.
     * @param cornerW
     * @param cornerS
     * @return eine Map, die die Ecken eines Tiles auf die zugehörige Höhe abbildet
     */
    public Map<String, Integer> generateTileHeightFirstRow(int cornerW , int cornerS) {
        Map<String, Integer> cornerHeights = new LinkedHashMap<>();

        int cornerN = generateHeightForCorner(cornerW);
        int cornerE = generateHeightForCorner(cornerN);

        // prüfe ob max. Höhenunterschied eingehalten wird
        // falls nicht, ändere den Wert
        if (Math.abs(cornerN - cornerW) > 1) {
            int digit = new Random().nextInt(1) + 1;
            if (Math.abs(cornerN - cornerW) > 2) {
                digit = 2;
            }
            if (cornerN > cornerW) cornerN = cornerN - digit;
            else cornerN = cornerN + digit;
        }

        if (Math.abs(cornerS - cornerE) > 1) {
            int digit = new Random().nextInt(1) + 1;
            if (Math.abs(cornerS - cornerE) > 2) {
                digit = 2;
            }
            if (cornerS > cornerE) cornerE = cornerE + digit;
            else cornerE = cornerE - digit;
        }

        cornerHeights.put("cornerN", cornerN);
        cornerHeights.put("cornerE", cornerE);
        cornerHeights.put("cornerS", cornerS);
        cornerHeights.put("cornerW", cornerW);

        return cornerHeights;
    }

    /**
     * Generiert Höhen für die vier Ecken eines Tiles unter Berücksichtigung der Einschränkungen (benachbarte Ecken
     * dürfen einen Höhenunterschied von max. 1 haben und diagonal gegenüberliegende Ecken max. 2
     *
     * @return eine Map, die die Ecken eines Tiles auf die zugehörige Höhe abbildet
     */
    public Map<String, Integer> generateTileHeight() {
        Map<String, Integer> cornerHeights = new LinkedHashMap<>();

        int cornerN = new Random().nextInt(5);
        int cornerE = generateHeightForCorner(cornerN);
        int cornerS = generateHeightForCorner(cornerE);
        int cornerW = generateHeightForCorner(cornerS);

        // prüfe ob max. Höhenunterschied zwischen angegebenen Ecken eingehalten wird
        // falls nicht, ändere den Wert
        if (Math.abs(cornerN - cornerW) > 1) {
            int digit = new Random().nextInt(1) + 1;
            if (Math.abs(cornerN - cornerW) > 2) {
                digit = 2;
            }
            if (cornerN > cornerW) cornerW = cornerW + digit;
            else cornerW = cornerW - digit;
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
                heightOfNextCorner = r.nextInt(maxHeight - minHeight + 2) + minHeight;
            }
        }
        else if (heightOfEdgeBefore < 0) {
            int probabilityMaxHeight = 40;
            int randomNumber = r.nextInt(100)+1;
            if(randomNumber < probabilityMaxHeight){
                heightOfNextCorner = maxHeight;
            }
            else {
                heightOfNextCorner = r.nextInt(maxHeight - minHeight) + minHeight;
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
