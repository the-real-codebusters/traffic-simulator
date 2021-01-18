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

    public void placeBuilding(int row, int column, Building building){

        for(int r=row; r<row+building.getDepth(); r++){
            for(int c=column; c<column+building.getWidth(); c++){
                if(fieldGrid[r][c] == null) fieldGrid[r][c] = new Field(0, building);
                else fieldGrid[r][c].setBuilding(building);
            }
        }
        Field originTile = fieldGrid[row][column];
        originTile.setBuildingOrigin(true);
    }

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

