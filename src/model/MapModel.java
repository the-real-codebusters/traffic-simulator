package model;

import java.util.ArrayList;
import java.util.List;

public class MapModel {
    private MapGenerator mapgen;

    private int width;
    private int depth;
    private Field[][] fieldGrid;

    public MapModel(int width, int depth) {
        this.width = width;
        this.depth = depth;
        this.fieldGrid = new Field[width][depth];
    }

//    public placeBuilding(int row, int column, Building building){
//        Field originTile = fieldGrid[row][column];
//        if(originTile == null) originTile = new Field(0, building);
//
//    }
    public MapGenerator getMapgen() { return mapgen; }

    public int getWidth() {
        return width;
    }

    public int getDepth() {
        return depth;
    }

    public Field[][] getFieldGrid() {
        return fieldGrid;
    }

    public void setFieldGrid(Field[][] fieldGrid) {
        this.fieldGrid = fieldGrid;
    }

}

