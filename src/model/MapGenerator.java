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
                Building building = null;
                int i = 0;
                while(building == null){
                    if(new Random().nextInt(100) < 50) {
                        building = natureBuildings.get(i);
                        // TODO: Ist es nötig neue Instanzen für jedes neue Building zu kreiieren?
                        // 
//                         building = natureBuildings.get(i).getNewInstance();
                    }
                    i++;
                    if(i >= natureBuildings.size()) i = 0;
                }
                // Felder im mapModel werden mit Höhe 0 und zugewiesenem FieldType initialisiert
                mapModel.getFieldGrid()[row][col] = new Field(0, building);
            }
        }
        return mapModel.getFieldGrid();
    }
}
