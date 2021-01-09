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

        // fields, buildings und map wurden hier nur zu Testzwecken erzeugt und sollen später anhand der Daten in
        // JSON erzeugt werden
        Field[][] fields = new Field[6][6];
        List<Building> buildings = new ArrayList<>();
        MapModel map = new MapModel( "planverkehr", fields, new ArrayList<Building>());

        MapGenerator generator = new MapGenerator("planverkehr", map);

        int mapSize = generator.generateMap(map).length;
        view.setMapSize(mapSize);

        // Map wird durch Methode der View gezeichnet
        view.drawMap(generator.generateMap(map));

    }
}
