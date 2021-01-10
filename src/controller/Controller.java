package controller;

import model.Building;
import model.Field;
import model.MapGenerator;
import model.MapModel;
import view.View;

import java.util.ArrayList;
import java.util.List;

public class Controller {
    private View view;

    public Controller(View view) {
        this.view = view;

        // buildings und map sind hier lediglich Testobjekte und sollen später anhand der Daten in der
        // JSON-Datei erzeugt werden
        List<Building> buildings = new ArrayList<>();
        MapModel map = new MapModel( "planverkehr", 10, 10, new ArrayList<>());

        // Ein generator wird erzeugt, der eine Map generiert (im Model)
        MapGenerator generator = new MapGenerator("planverkehr", map);
        Field[][] generatedMap = generator.generateMap(map);

        // Breite und Tiefe der Map aus dem Model werden in der View übernommen
        view.setMapWidth(map.getWidth());
        view.setMapDepth(map.getDepth());

        // Map wird durch Methode der View gezeichnet
        view.drawMap(generatedMap);

    }
}
