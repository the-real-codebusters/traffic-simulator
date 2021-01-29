package model;

import java.util.List;
import java.util.Random;

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

        Tile[][] mapFieldGrid = mapModel.getTileGrid();

        for (int row = 0; row < mapDepth; row++) {
            for (int col = 0; col < mapWidth; col++) {
                int probWater = 1                                                 ;
                if(row > 1 && col > 1) {
                    if(mapFieldGrid[row-1][col].getHeight() < 0) probWater+=345;
                    if(mapFieldGrid[row][col-1].getHeight() < 0) probWater+=500;
                    if(mapFieldGrid[row-2][col].getHeight() < 0) probWater+=50;
                    if(mapFieldGrid[row][col-2].getHeight() < 0) probWater+=50;
                }
                Building building = null;
                int heightRandom =  new Random().nextInt(1000);
                int height = 0;
                if(heightRandom <= probWater) {
                    height = -1;
                    mapFieldGrid[row][col] = new Tile(height, null);
                }
                else {
                    if(heightRandom > 850 && heightRandom < 950) height = 1;
                    else if(heightRandom > 950 && heightRandom <= 970) height = 2;
                    else if(heightRandom > 970 && heightRandom <= 985) height = 3;
                    else if(heightRandom > 985) height = 4;

                    int buildingRandom = new Random().nextInt(natureBuildings.size());
                    building = natureBuildings.get(buildingRandom).getNewInstance();
                    mapFieldGrid[row][col] = new Tile(height, building);
                }

            }
        }
    }
}
