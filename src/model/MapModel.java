package model;

import java.util.ArrayList;
import java.util.List;

public class MapModel {
    private MapGenerator mapgen;
    private String gamemode;
    private int width;
    private int depth;
    private Field[][] fieldGrid;
    private List<Building> buildings;

    public MapModel(String gamemode, Field[][] fieldGrid, ArrayList<Building> buildings) {
        this.gamemode = gamemode;
        this.width = fieldGrid.length;
        this.depth = fieldGrid.length;
        this.fieldGrid = fieldGrid;
        this.buildings = buildings;
    }

    public MapGenerator getMapgen() {
        return mapgen;
    }

    public String getGamemode() {
        return gamemode;
    }

    public int getWidth() {
        return width;
    }

    public int getDepth() {
        return depth;
    }

    public Field[][] getFieldGrid() {
        return fieldGrid;
    }

    public List<Building> getBuildings() {
        return buildings;
    }
}
