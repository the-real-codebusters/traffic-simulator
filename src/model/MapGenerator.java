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

    /**
     * Generiert eine Map mit prozentualer Verteilung der Felder anhand des FieldType
     *
     * @param mapModel wird benutzt, um größe der Map zu bestimmen und Array der Felder zu befüllen
     * @return eine 2D-Liste von Feldern mit zufälligen Feldtypen
     */
    public Field[][] generateMap(MapModel mapModel, BasicModel basicModel) {

        int mapWidth = mapModel.getWidth();
        int mapDepth = mapModel.getDepth();

        List<Building> natureBuildings = basicModel.getBuildingsForSpecialUse("nature");
        System.out.println("Size nature buildings "+natureBuildings.size());

        //TODO: geeignete Datenstruktur zur Speicherung der Typen überlegen. Eventuell benötigen wir
        // Unterkategorien, z.B: Kategorie Nature -> Unterkategorien Baum, Gras, Wasser usw.


        //TODO Es gibt gar kein gras im Planverkehr JSON -> Stone statt Gras zeichnen und einbauen


        for (int row = 0; row < mapWidth; row++) {
            for (int col = 0; col < mapDepth; col++) {
                int probWater = 5;
                if(row > 0 && col > 0) {
                    if(mapModel.getFieldGrid()[row-1][col].getHeight() < 0) probWater+=100;
                    if(mapModel.getFieldGrid()[row][col-1].getHeight() < 0) probWater+=700;
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

                mapModel.getFieldGrid()[row][col] = new Field(height, building);
            }
        }
        return mapModel.getFieldGrid();
    }
}
