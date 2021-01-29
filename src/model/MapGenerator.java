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
                    if(mapFieldGrid[row-1][col].getHeight() < 0) probWater+=345;
                    if(mapFieldGrid[row][col-1].getHeight() < 0) probWater+=500;
                    if(mapFieldGrid[row-2][col].getHeight() < 0) probWater+=90;
                    if(mapFieldGrid[row][col-2].getHeight() < 0) probWater+=50;
                }



                Building building = null;
                int heightRandom =  new Random().nextInt(1000);
                int height = 0;
                if(heightRandom <= probWater) {
                    height = -1;
                    mapFieldGrid[row][col] = new Tile(height, null);
                }

                //Wenn kein Wasser gesetzt ist, andere Höhen setzen
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
    public int generateTileHeight(){
        int heightRandom =  0;
        int firstDigit = new Random().nextInt(10);

        int secondDigit = genrateNextNumber(firstDigit);
        int thirdDigit = genrateNextNumber(secondDigit);
        int fourthDigit = genrateNextNumber(thirdDigit);

        if(Math.abs(firstDigit - fourthDigit) > 2){
            int digit = new Random().nextInt(2) + 1;
            if(firstDigit > fourthDigit) {
                fourthDigit = fourthDigit + digit;
            } else {
                fourthDigit = fourthDigit - digit;
            }
        }

        System.out.println(firstDigit + "" + secondDigit + "" + thirdDigit + "" + fourthDigit);

        return heightRandom;
    }

    public int genrateNextNumber(int digitBefore){
        Random r = new Random();

        int low;
        int high;
        if(digitBefore != 0){
            low = digitBefore-1;
        } else {
            low = digitBefore;
        }

        if(digitBefore == 9){
            high = digitBefore;
        } else {
            high = digitBefore +1;
        }

        int digit = r.nextInt(high-low +1) + low;

        return digit;
    }
}
