package model;

import java.util.ArrayList;
import java.util.List;

public class MapModel {
    private String mapgen;

    private int width;
    private int depth;
    private Field[][] fieldGrid;

    public MapModel(int width, int depth) {
        this.width = width;
        this.depth = depth;
        this.fieldGrid = new Field[depth][width];
    }

    public void placeBuilding(int row, int column, Building building){

        Building instance = building.getNewInstance();
        for(int r=row; r<row+instance.getWidth(); r++){
            for(int c=column; c<column+instance.getDepth(); c++){
                if(fieldGrid[r][c] == null) fieldGrid[r][c] = new Field(0, instance);
                else fieldGrid[r][c].setBuilding(instance);
            }
        }
        Field originTile = fieldGrid[row][column];
        originTile.setBuildingOrigin(true);
        instance.setOriginColumn(column);
        instance.setOriginRow(row);
    }


    public boolean canPlaceBuilding(int row, int column, Building building){
        if((row+building.getWidth()) >= depth) return  false;
        if((column+building.getDepth()) >= width) return  false;

        for(int r=row; r<row+building.getWidth(); r++){
            for(int c=column; c<column+building.getDepth(); c++){
                Field tile = fieldGrid[r][c];
                if(tile.getHeight() < 0) return false;
                if(tile.getBuilding() instanceof Road) return true;
                if(! (tile.getBuilding() instanceof Nature)) return false;
            }
        }
        return true;
    }

    public String getMapgen() { return mapgen; }

    public void setMapgen(String mapgen) {
        this.mapgen = mapgen;
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

    public void setFieldGrid(Field[][] fieldGrid) {
        this.fieldGrid = fieldGrid;
    }


    // Methode für Testzwecke zum Überprüfen ob Indizes des fieldGrid im Model mit Indizes in der View übereinstimmen
    public void printFieldsArray() {
        for (int row = 0; row < depth; row++) {
            for (int column = 0; column < width; column++) {
                if (fieldGrid[row][column].getHeight() < 0) {
                    System.out.print("[" + row + ", " + column + "]water" + " ");
                } else {
                    System.out.print("[" + row + ", " + column + "]" + fieldGrid[row][column].getBuilding().getBuildingName() + " ");
                }
            }
            System.out.println();
        }
    }



}

