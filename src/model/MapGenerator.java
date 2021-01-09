package model;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

public class MapGenerator {
    private String generatorName;
    private MapModel mapModel;

    public MapGenerator(String generatorName, MapModel mapModel) {
        this.generatorName = generatorName;
        this.mapModel = mapModel;
    }

    /**
     * Generiert eine Map mit zufälliger Verteilung anhand des FieldType
     * @param mapModel wird benutzt, um größe der Map zu bestimmen und Array der Felder zu befüllen
     * @return eine 2D-Liste von Feldern mit zufälligen Feldtypen
     */
    public Field[][] generateMap(MapModel mapModel) {

        int mapSize = mapModel.getFieldGrid().length;

        for (int row = 0; row < mapSize; row++) {
            for (int col = 0; col < mapSize; col++) {
                //Momentan noch hardcoded, da nur 2 Graphiken verfügbar
                List<String> fieldTypes = new ArrayList<>();
                fieldTypes.add("grass");
                fieldTypes.add("green_grass");
                //TODO: geeignete Datenstruktur finden, zur Speicherung der Typen. Eventuell benötigen wir
                // Unterkategorien, z.B: Kategorie Nature -> Unterkategorien Baum, Gras, Wasser usw.

                // Es wird ein zufälliger Wert aus den möglichen Feldtypen ermittelt
                SplittableRandom random = new SplittableRandom();
                String fieldType  = fieldTypes.get(random.nextInt(0, fieldTypes.size()));

                // Felder im mapModel werden mit Höhe 0 und zufälligem FieldType initialisiert
                mapModel.getFieldGrid()[row][col] = new Field(0, fieldType);
            }
        }
        return  mapModel.getFieldGrid();
    }
}
