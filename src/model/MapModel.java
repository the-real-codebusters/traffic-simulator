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

    public MapModel(String gamemode, int width, int depth, ArrayList<Building> buildings) {
        this.gamemode = gamemode;
        this.width = width;
        this.depth = depth;
        this.fieldGrid = new Field[width][depth];
        this.buildings = buildings;
    }

    public MapGenerator getMapgen() { return mapgen; }

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


    // Methode für Testzwecke zum Überprüfen ob Indizes des fieldGrid im Model mit Indizes in der View übereinstimmen
    public void printFieldsArray() {
        String greenFont = "\u001B[32m";
        String resetFont = "\u001B[0m";
        for (int row = 0; row < width; row++) {
            for (int column = 0; column < depth; column++) {
                if(fieldGrid[row][column].getFieldType().equals("green")){
                    System.out.print("["+ row + ", " + column + "]" + greenFont + fieldGrid[row][column].getFieldType()
                            + " " + resetFont);
                } else {
                    System.out.print("[" + row + ", " + column + "]" + fieldGrid[row][column].getFieldType() + " ");
                }
            }
            System.out.println();
        }
    }
}

