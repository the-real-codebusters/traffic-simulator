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
    public Field[][] generateMap(MapModel mapModel) {

        int mapWidth = mapModel.getWidth();
        int mapDepth = mapModel.getDepth();
        String fieldType;

        //Momentan noch hardcoded, da nur 2 Graphiken verfügbar
        List<String> fieldTypes = new ArrayList<>();
        fieldTypes.add("grass");
        fieldTypes.add("green_grass");
        //TODO: geeignete Datenstruktur zur Speicherung der Typen überlegen. Eventuell benötigen wir
        // Unterkategorien, z.B: Kategorie Nature -> Unterkategorien Baum, Gras, Wasser usw.


        // FieldType wird für jedes Feld entsprechend einer Wahrscheinlichkeit bestimmt
        // Hier haben die "grauen" Grassfelder eine viel höhere Wahrscheinlichkeit als die grünen
        // Später sollen Grassfelder allgemein eine hohe Wahrscheinlichkeit bekommen, während Wasserfelder und
        // Felder mit Produktionsanlagen eine niedrigere prozentuale Wahrscheinlichkeit bekommen
        for (int row = 0; row < mapWidth; row++) {
            for (int col = 0; col < mapDepth; col++) {
                if (new Random().nextInt(100) < 90) {
                    fieldType = "grey";
                } else {
                    fieldType = "green";
                }
                // Felder im mapModel werden mit Höhe 0 und zugewiesenem FieldType initialisiert
                mapModel.getFieldGrid()[row][col] = new Field(0, fieldType);
            }
        }
        mapModel.printFieldsArray();
        return mapModel.getFieldGrid();
    }
}
