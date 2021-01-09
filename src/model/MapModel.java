package model;

import java.util.ArrayList;
import java.util.List;

public class MapModel {
    private MapGenerator mapgen;
    private String gamemode;
    private int width;
    private int depth;
    private Field[][] basicFieldGrid;
    private List<Building> buildings;

    public MapModel(MapGenerator mapgen, String gamemode, int width, int depth, Field[][] basicFieldGrid,
                    ArrayList<Building> buildings) {
        this.mapgen = mapgen;
        this.gamemode = gamemode;
        this.width = width;
        this.depth = depth;
        this.basicFieldGrid = basicFieldGrid;
        this.buildings = buildings;
    }
}
