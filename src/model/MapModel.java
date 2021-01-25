package model;

public class MapModel {
    private String mapgen;

    private int width;
    private int depth;
    private Tile[][] fieldGrid;

    private BasicModel model;
    private Long adjacentStationId;

    public MapModel(int width, int depth, BasicModel model) {
        this.width = width;
        this.depth = depth;
        this.fieldGrid = new Tile[depth][width];
        this.model = model;
    }

    public void placeBuilding(int row, int column, Building building){

        Building instance = building.getNewInstance();
        for(int r=row; r<row+instance.getWidth(); r++){
            for(int c=column; c<column+instance.getDepth(); c++){
                if(fieldGrid[r][c] == null) fieldGrid[r][c] = new Tile(0, instance);
                else fieldGrid[r][c].setBuilding(instance);
            }
        }
        Tile originTile = fieldGrid[row][column];
        originTile.setBuildingOrigin(true);
        instance.setOriginColumn(column);
        instance.setOriginRow(row);

        if(instance instanceof Stop) {
            Station nextStation = getStationNextToStop(row, column, (Stop) instance);
            Station station = new Station(model);
            if(nextStation != null) {
                station = nextStation;
            }
            station.addBuilding((Stop) instance);
        }
    }


    public boolean canPlaceBuilding(int row, int column, Building building){
        if((row+building.getWidth()) >= depth) return  false;
        if((column+building.getDepth()) >= width) return  false;

        for(int r=row; r<row+building.getWidth(); r++){
            for(int c=column; c<column+building.getDepth(); c++){
                Tile tile = fieldGrid[r][c];
                if(tile.getHeight() < 0) return false;
                if(tile.getBuilding() instanceof Road) return true;
                if(! (tile.getBuilding() instanceof Nature)) return false;
            }
        }

        if(building instanceof Stop){
            adjacentStationId = -1L;
            for(int r=row; r<row+building.getWidth(); r++){
                Building adjacentBuilding = fieldGrid[r][column -1].getBuilding();
                if(adjacentBuilding instanceof Stop) {
                    if(checkForSecondStation(adjacentBuilding)) return false;
                }
                adjacentBuilding = fieldGrid[r][column+ building.getDepth()].getBuilding();
                if(adjacentBuilding instanceof Stop) {
                    if(checkForSecondStation(adjacentBuilding)) return false;
                }
            }

            for(int c=column; c<column+building.getDepth(); c++){
                Building adjacentBuilding = fieldGrid[row-1][c].getBuilding();
                if(adjacentBuilding instanceof Stop) {
                    if(checkForSecondStation(adjacentBuilding)) return false;
                }
                adjacentBuilding = fieldGrid[row+building.getWidth()][c].getBuilding();
                if(adjacentBuilding instanceof Stop) {
                    if(checkForSecondStation(adjacentBuilding)) return false;
                }
            }
        }
        return true;
    }

    private Station getStationNextToStop(int row, int column, Stop building){
        Station station;
        for(int r=row; r<row+building.getWidth(); r++){
            Building adjacentBuilding = fieldGrid[r][column -1].getBuilding();
            if(adjacentBuilding instanceof Stop) {
                return ((Stop) adjacentBuilding).getStation();
            }
            adjacentBuilding = fieldGrid[r][column+ building.getDepth()].getBuilding();
            if(adjacentBuilding instanceof Stop) {
                return ((Stop) adjacentBuilding).getStation();
            }
        }

        for(int c=column; c<column+building.getDepth(); c++){
            Building adjacentBuilding = fieldGrid[row-1][c].getBuilding();
            if(adjacentBuilding instanceof Stop) {
                return ((Stop) adjacentBuilding).getStation();
            }
            adjacentBuilding = fieldGrid[row+building.getWidth()][c].getBuilding();
            if(adjacentBuilding instanceof Stop) {
                return ((Stop) adjacentBuilding).getStation();
            }
        }
        return null;
    }

    private boolean checkForSecondStation(Building building) {
        Long currentId = ((Stop) building).getStation().getId();

        if(adjacentStationId== -1) {
            adjacentStationId = currentId;
        }
        else {
            if (adjacentStationId != currentId) return true;
        }
        return false;
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

    public Tile[][] getFieldGrid() {
        return fieldGrid;
    }

    public void setFieldGrid(Tile[][] fieldGrid) {
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

