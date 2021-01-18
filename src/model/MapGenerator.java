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

        generateNature(mapWidth, mapDepth, mapFieldGrid, basicModel);
        generateFactories(mapWidth, mapDepth, mapFieldGrid, basicModel);

        System.out.println("finished");

        return mapFieldGrid;
    }

    private void generateFactories(int mapWidth, int mapDepth, Field[][] mapFieldGrid, BasicModel basicModel){
        List<Building> factoryBuildings = basicModel.getBuildingsForSpecialUse("factory");
        Random randomGenerator = new Random();
        for(Building factory: factoryBuildings){
            int maxNumberOfPlacements = ((mapDepth*mapWidth)/(factory.getDepth()*factory.getWidth()))/10;
            int numberOfPlacements = randomGenerator.nextInt(maxNumberOfPlacements)+1;
            int maxColumn = mapWidth - factory.getWidth() -1;
            int maxRow = mapDepth - factory.getDepth() -1;
            while(numberOfPlacements > 0){
                mapFieldGrid[randomGenerator.nextInt(maxRow)][randomGenerator.nextInt(maxColumn)].setBuilding(factory);
                numberOfPlacements--;
            }
        }

    }

    private void generateNature(int mapWidth, int mapDepth, Field[][] mapFieldGrid, BasicModel basicModel) {
        List<Building> natureBuildings = basicModel.getBuildingsForSpecialUse("nature");

        //TODO: geeignete Datenstruktur zur Speicherung der Typen überlegen. Eventuell benötigen wir
        // Unterkategorien, z.B: Kategorie Nature -> Unterkategorien Baum, Gras, Wasser usw.


        //TODO Es gibt gar kein gras im Planverkehr JSON -> Stone statt Gras zeichnen und einbauen


        for (int row = 0; row < mapWidth; row++) {
            for (int col = 0; col < mapDepth; col++) {
                int probWater = 5;
                if(row > 0 && col > 0) {
                    if(mapFieldGrid[row-1][col].getHeight() < 0) probWater+=100;
                    if(mapFieldGrid[row][col-1].getHeight() < 0) probWater+=700;
                }
                Building building = null;
                int heightRandom =  new Random().nextInt(1000);
                int height = 0;
                if(heightRandom <= probWater) height = -1;
                else if(heightRandom > 850 && heightRandom < 950) height = 1;
                else if(heightRandom > 810 && heightRandom <= 850) height = 2;
                else if(heightRandom > 950 && heightRandom <= 980) height = 3;
                else if(heightRandom > 980) height = 4;

                int buildingRandom = new Random().nextInt(natureBuildings.size());
                building = natureBuildings.get(buildingRandom);
                // TODO: Ist es nötig neue Instanzen für jedes neue Building zu kreiieren?
                //
//                         building = natureBuildings.get(i).getNewInstance();

                mapFieldGrid[row][col] = new Field(height, building);
            }
        }
    }
}
