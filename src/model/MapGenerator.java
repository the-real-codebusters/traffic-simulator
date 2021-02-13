package model;

import java.util.*;

public class MapGenerator {
    private String generatorName;
    private MapModel mapModel;
    private BasicModel basicModel;

    public MapGenerator(String generatorName, MapModel mapModel, BasicModel basicModel) {
        this.generatorName = generatorName;
        this.mapModel = mapModel;
        this.basicModel = basicModel;
    }

    public Tile[][] generateMap(BasicModel basicModel) {

        int mapWidth = mapModel.getWidth();
        int mapDepth = mapModel.getDepth();
        Tile[][] mapFieldGrid = mapModel.getTileGrid();

        generateHeightMap();
        generateFactories(mapWidth, mapDepth, basicModel);

        return mapFieldGrid;
    }

    /**
     * Generiert einige Fabriken auf die Map. Mindestens eine Fabrik jeden Typs
     *
     * @param mapWidth
     * @param mapDepth
     * @param basicModel
     */
    private void generateFactories(int mapWidth, int mapDepth, BasicModel basicModel) {
        List<Special> factoryBuildings = basicModel.getBuildingsForSpecialUse("factory");
        Random randomGenerator = new Random();
        for (Building factory : factoryBuildings) {
            int maxNumberOfPlacements = ((mapDepth * mapWidth) / (factory.getDepth() * factory.getWidth())) / 250;
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
     * Iteriert durch Tiles des Spielfelds und erzeugt für jedes Tile eine Map, die die Ecken auf ihre Höhe abbildet,
     * wobei die Höhen der Ecken sowohl innerhalb eines Tiles als auch der Tiles zueinander stimmig sein müssen
     */
    public void generateHeightMap() {
        int mapWidth = mapModel.getWidth();
        int mapDepth = mapModel.getDepth();
        Tile[][] mapFieldGrid = mapModel.getTileGrid();

        for (int row = 0; row < mapDepth; row++) {
            for (int col = 0; col < mapWidth; col++) {

                // Setze höhen der ecken für erste zeile und erste spalte auf 0
                if (row == 0 || col == 0) {
                    Map<String, Integer> map = new LinkedHashMap<>();
                    map.put("cornerN", 0);
                    map.put("cornerE", 0);
                    map.put("cornerS", 0);
                    map.put("cornerW", 0);
                    mapFieldGrid[row][col] = new Tile(null, map, false);
                    mapFieldGrid[row][col].setCornerHeights(map);


                // Erzeuge Höhen für alle Felder, die nicht in der ersten Zeile oder in der ersten Spalte sind
                } else if (row != 0 && col != 0) {
                    int heightOfCornerN = mapFieldGrid[row][col - 1].getCornerHeights().get("cornerN");
                    int heightOfCornerERow = mapFieldGrid[row][col - 1].getCornerHeights().get("cornerE");
                    int heightOfCornerECol = mapFieldGrid[row - 1][col].getCornerHeights().get("cornerE");

                    Map<String, Integer> map = generateTileHeightMiddle(heightOfCornerN, heightOfCornerECol, heightOfCornerERow);
                    mapFieldGrid[row][col] = new Tile(null, map, false);
                    mapFieldGrid[row][col].setCornerHeights(map);

                }

                Map<String, Integer> cornerHeights = mapFieldGrid[row][col].getCornerHeights();
                String absoluteTileHeight = mapFieldGrid[row][col].absoluteHeigtToRelativeHeight(cornerHeights);

                // Wenn es sich um ein flaches Feld handelt, das kein Wasserfeld ist
                if (absoluteTileHeight.equals("0000") && cornerHeights.get("cornerS") >= 0) {
                    generateNature(row, col);

                // In diesem Fall wird Wasser erzeugt
                } else if( cornerHeights.get("cornerS") < 0 &&  cornerHeights.get("cornerN") < 0
                &&  cornerHeights.get("cornerW") < 0 &&  cornerHeights.get("cornerE") < 0){
                    mapFieldGrid[row][col] = new Tile(null, cornerHeights, true);

                // Ansonsten wird ein Feld mit Höhenunterschied erzeugt
                } else {
                    Building building = new Building(1, 1, "ground");
                    mapFieldGrid[row][col].setCornerHeights(cornerHeights);
                    mapFieldGrid[row][col].setBuilding(building);
                }
            }
        }
    }


    public void generateNature(int row, int col) {

        List<Special> natureBuildings = basicModel.getBuildingsForSpecialUse("nature");

        Tile[][] mapFieldGrid = mapModel.getTileGrid();

        Map<String, Integer> cornerHeights = mapModel.getTileGrid()[row][col].getCornerHeights();

        HashMap<Building, Integer> natureStartProbabilities = new HashMap<>();
        for (Building nature : natureBuildings) {
            natureStartProbabilities.put(nature, 3 / natureBuildings.size() >= 1 ? 3 / natureBuildings.size() : 1);
        }

        int percentPointsIfEqualNatureBuildingNear = 25;
        Map<Building, Integer> natureProbabilities = new HashMap<>();
        natureProbabilities.putAll(natureStartProbabilities);
        if (row > 0) {
            Building rowNearBuilding = mapFieldGrid[row - 1][col].getBuilding();
            if (natureProbabilities.containsKey(rowNearBuilding)) {
                natureProbabilities.replace(rowNearBuilding, natureProbabilities.get(rowNearBuilding) + percentPointsIfEqualNatureBuildingNear);
            }
        }
        if (col > 0) {
            Building colNearBuilding = mapFieldGrid[row][col - 1].getBuilding();
            if (natureProbabilities.containsKey(colNearBuilding)) {
                natureProbabilities.replace(colNearBuilding, natureProbabilities.get(colNearBuilding) + percentPointsIfEqualNatureBuildingNear);
            }
        }
        if (col > 0 && row > 0) {
            Building colNearBuilding = mapFieldGrid[row - 1][col - 1].getBuilding();
            if (natureProbabilities.containsKey(colNearBuilding)) {
                natureProbabilities.replace(colNearBuilding, natureProbabilities.get(colNearBuilding) + percentPointsIfEqualNatureBuildingNear);
            }
        }
        int probCounter = 0;
        int random = new Random().nextInt(99) + 1;
        Building building = new Building(1, 1, "grass");
//        Building building = new Building(1, 1, "flat");
        mapModel.getTileGrid()[row][col].setCornerHeights(cornerHeights);
        mapModel.getTileGrid()[row][col].setBuilding(building);
        for (Map.Entry<Building, Integer> entry : natureProbabilities.entrySet()) {
            probCounter += entry.getValue();
            if (random <= probCounter) {
                building = entry.getKey();
                break;
            }
        }

        mapFieldGrid[row][col] = new Tile(building, cornerHeights, false);

    }


    /**
     * Generiert Höhen für die Tiles, die sich weder in der ersten Zeile noch in der ersten Spalte befinden.
     * Die Werte der übergebenen Parameter sind die Höhen der "Vorgängerfelder", die bei der Erzeugung der Höhen des
     * aktuellen Feldes berücksichtigt und beibehalten werden müssen.
     *
     * @param cornerW
     * @param cornerN
     * @param cornerS
     * @return eine Map, die die Ecken eines Tiles auf die zugehörige Höhe abbildet
     */
    public Map<String, Integer> generateTileHeightMiddle(int cornerW, int cornerN, int cornerS) {
        Map<String, Integer> cornerHeights = new LinkedHashMap<>();

        int cornerE;

        if (cornerW == -1 && cornerN == -1 && cornerS == -1){
            cornerE = -1;
        } else {

            cornerE = generateHeightForCorner(cornerN);

            if (Math.abs(cornerS - cornerE) > 1) {
                int digit = 1;
                if (Math.abs(cornerS - cornerE) > 2) {
                    digit = 2;
                }
                if (cornerS > cornerE) cornerE = cornerE + digit;
                else cornerE = cornerE - digit;
            }
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
        if (heightOfEdgeBefore != -1) {
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
        if (heightOfEdgeBefore > 0) {
            int probabilityMinHeight = 80;
            int randomNumber = r.nextInt(100) + 1;
            if (randomNumber < probabilityMinHeight) {
                heightOfNextCorner = minHeight;
            } else {
                heightOfNextCorner = r.nextInt(maxHeight - minHeight + 1) + minHeight;
            }
            // Wenn die vorherige Ecke < 0 war, ist die Wahrscheinlichkeit höher, dass auch die nächste Ecke diesen
            // Wert bekommt, damit mehr Wasserfelder entstehen können
        } else if (heightOfEdgeBefore < 0) {
            int probabilityMaxHeight = 75;
            int randomNumber = r.nextInt(100) + 1;
            if (randomNumber < probabilityMaxHeight) {
                heightOfNextCorner = heightOfEdgeBefore;
            } else {
                heightOfNextCorner = r.nextInt(maxHeight - minHeight + 1) + minHeight;
            }
        } else {
            int probabilityNullHeight = 95;
            int randomNumber = r.nextInt(100) + 1;
            if (randomNumber < probabilityNullHeight) {
                heightOfNextCorner = 0;
            } else {
                heightOfNextCorner = r.nextInt(maxHeight - minHeight + 1) + minHeight;
            }
        }
        return heightOfNextCorner;
    }
}
