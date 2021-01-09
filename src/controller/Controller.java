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

        Field[][] fields = new Field[8][8];
        List<Building> buildings = new ArrayList<>();
        MapModel map = new MapModel(new MapGenerator("planverkehr"), "planverkehr",
                8, 8, fields, new ArrayList<Building>());

        for (int row = 0; row < fields.length; row++) {
            for (int col = 0; col < fields.length; col++) {
                fields[row][col] = new Field(0, "gras");
            }
        }
        fields[4][4].setFieldType("green");

        view.drawMap(fields);
        this.view.getStage().show();
    }
}
