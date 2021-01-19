package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapGenerator {
    private String generatorName;
    private MapModel mapModel;

    public MapGenerator(String generatorName, MapModel mapModel) {
        this.generatorName = generatorName;
        this.mapModel = mapModel;
    }

    public Field[][] generateMap(BasicModel basicModel) {

        int mapWidth = mapModel.getWidth();
        int mapDepth = mapModel.getDepth();
        Field[][] mapFieldGrid = mapModel.getFieldGrid();

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
            Building factoryInstance = factory.getNewInstance();
            while(numberOfPlacements > 0){
                int row = randomGenerator.nextInt(maxRow);
                int column = randomGenerator.nextInt(maxColumn);
                if(mapModel.canPlaceBuilding(row, column, factoryInstance)) {
                    mapModel.placeBuilding(row, column, factoryInstance);
                    numberOfPlacements--;
                }
            }
        }

    }

    private void generateNature(int mapWidth, int mapDepth, BasicModel basicModel) {
        List<Special> natureBuildings = basicModel.getBuildingsForSpecialUse("nature");

        //TODO: geeignete Datenstruktur zur Speicherung der Typen überlegen. Eventuell benötigen wir
        // Unterkategorien, z.B: Kategorie Nature -> Unterkategorien Baum, Gras, Wasser usw.


        //TODO Es gibt gar kein gras im Planverkehr JSON -> Stone statt Gras zeichnen und einbauen

        Field[][] mapFieldGrid = mapModel.getFieldGrid();

        for (int row = 0; row < mapDepth; row++) {
            for (int col = 0; col < mapWidth; col++) {
                int probWater = 5;
                if(row > 0 && col > 0) {
                    if(mapFieldGrid[row-1][col].getHeight() < 0) probWater+=100;
                    if(mapFieldGrid[row][col-1].getHeight() < 0) probWater+=700;
                }
                Building building = null;
                int heightRandom =  new Random().nextInt(1000);
                int height = 0;
                if(heightRandom <= probWater) {
                    height = -1;
                    mapFieldGrid[row][col] = new Field(height, null);
                }
                else {
                    if(heightRandom > 850 && heightRandom < 950) height = 1;
                    else if(heightRandom > 810 && heightRandom <= 850) height = 2;
                    else if(heightRandom > 950 && heightRandom <= 980) height = 3;
                    else if(heightRandom > 980) height = 4;

                    int buildingRandom = new Random().nextInt(natureBuildings.size());
                    building = natureBuildings.get(buildingRandom).getNewInstance();
                    mapFieldGrid[row][col] = new Field(height, building);
                }

            }
        }
    }
}
