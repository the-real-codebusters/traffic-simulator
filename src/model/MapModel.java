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
        this.fieldGrid = new Field[depth][width];
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
        building.setOriginColumn(column);
        building.setOriginRow(row);
    }

    public boolean canPlaceBuilding(int row, int column, Building building){
        for(int r=row; r<row+building.getDepth(); r++){
            for(int c=column; c<column+building.getWidth(); c++){
                Field tile = fieldGrid[r][c];
                if(tile.getHeight() < 0) return false;
                if(! (tile.getBuilding() instanceof Nature)) return false;
            }
        }
        return true;
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

