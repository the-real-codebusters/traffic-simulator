package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapModel {
    private String mapgen;

    private int width;
    private int depth;
    private Tile[][] fieldGrid;

    private BasicModel model;
    private Long adjacentStationId;
    private List<Station> stations = new ArrayList<>();
    private TrafficGraph rawRoadGraph = new TrafficGraph();


    public MapModel(int width, int depth, BasicModel model) {
        this.width = width;
        this.depth = depth;
        this.fieldGrid = new Tile[depth][width];
        this.model = model;
    }

    /**
     * Platziert das Gebäude building an der angegebenen Stelle
     * @param row
     * @param column
     * @param building
     * @return
     */
    public Building placeBuilding(int row, int column, Building building){

        Building instance = building.getNewInstance();
        if(instance instanceof PartOfTrafficGraph) System.out.println("points "+((PartOfTrafficGraph) instance).getPoints());
        for(int r=row; r<row+instance.getWidth(); r++){
            for(int c=column; c<column+instance.getDepth(); c++){
                if(fieldGrid[r][c] == null) fieldGrid[r][c] = new Tile(instance, fieldGrid[r][c].getCornerHeights(), false);
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
            } else {
                stations.add(station);
            }
            station.addBuilding((Stop) instance);
        }
        return instance;
    }


    /**
     * Gibt zurück, ob das Gebäude an der angegebenen Stelle platziert werden darf
     * @param row
     * @param column
     * @param building
     * @return
     */
    public boolean canPlaceBuilding(int row, int column, Building building){
        if((row+building.getWidth()) >= depth) return  false;
        if((column+building.getDepth()) >= width) return  false;

        for(int r=row; r<row+building.getWidth(); r++){
            for(int c=column; c<column+building.getDepth(); c++){
                Tile tile = fieldGrid[r][c];
                if(tile.getHeight() < 0) return false;
                if(tile.getBuilding() instanceof Road) {
                    // TODO Mache es allgemeiner, indem es auch für Rail implementiert wird
                    boolean canCombine = model.checkCombines(row, column, building) != building;
                    return canCombine;
                }
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


    /**
     * Fügt die Points eines Felds zum Verkehrsgraph hinzu. Points innerhalb eines Tiles sind miteinander
     * durch eine ungerichtete Kante verbunden. Wenn sich Punkte "an derselben Stelle" befinden, werden diese
     * zusammengeführt.
     *
     * @param building die Instanz des Gebäudes, wessen Punkte hinzugefügt werden sollen
     * @param xCoordOfTile     x-Koordinate des Tiles, auf das die Straße platziert wurde
     * @param yCoordOfTile     y-Koordinate des Tiles, auf das die Straße platziert wurde
     */
    public void addPointsToGraph(PartOfTrafficGraph building, int xCoordOfTile, int yCoordOfTile) {
        TrafficGraph trafficGraph;
        if(building instanceof Road) {
            trafficGraph = this.rawRoadGraph;
        }
        else {
            return;
            //TODO rails
        };
                Map<String, List<Double>> points = building.getPoints();
                for (Map.Entry<String, List<Double>> entry : points.entrySet()) {

                    // identifier wird dem Name eines Knotens hinzugefügt, damit der Name unique bleibt,
                    // sonst gäbe es Duplikate, da points aus verschiedenen Felder denselben Namen haben könnten
                    String identifier = xCoordOfTile + "-" + yCoordOfTile + "-";
                    String vertexName = identifier + entry.getKey();

                    double xCoordOfPoint = entry.getValue().get(0);
                    double yCoordOfPoint = entry.getValue().get(1);

                    Vertex v = new Vertex(vertexName, xCoordOfPoint, yCoordOfPoint, xCoordOfTile, yCoordOfTile);

                    trafficGraph.addVertex(v);

                    for (Vertex v1 : trafficGraph.getMapOfVertexes().values()) {
                        List<List<String>> edges = building.getTransportations();
                        System.out.println(building.getTransportations());
                        System.out.println(building.getBuildingName());
                        for (int i = 0; i < edges.size(); i++) {
                            String from = identifier + edges.get(i).get(0);
                            String to = identifier + edges.get(i).get(1);
//                            System.out.println("From: " + from);
//                            System.out.println("To: " + to);

                            if ((v.getName().equals(from) && v1.getName().equals(to)) ||
                                    (v.getName().equals(to) && v1.getName().equals(from)))
                                trafficGraph.addEdgeBidirectional(v1.getName(), v.getName());
                        }
                    }
                }
        trafficGraph.checkForDuplicatePoints();
        trafficGraph.printGraph();
        System.out.println();
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

    public Tile[][] getTileGrid() {
        return fieldGrid;
    }

    public void setFieldGrid(Tile[][] fieldGrid) {
        this.fieldGrid = fieldGrid;
    }


    // Methode für Testzwecke zum Überprüfen ob Indizes des fieldGrid im Model mit Indizes in der View übereinstimmen
    public void printFieldsArray() {
        for (int row = 0; row < depth; row++) {
            for (int column = 0; column < width; column++) {
//                if (fieldGrid[row][column].getHeight() < 0) {
//                    System.out.print("[" + row + ", " + column + "]water" + " ");
//                } else {
//                    System.out.print("[" + row + ", " + column + "]" + fieldGrid[row][column].getBuilding().getBuildingName() + " ");
//                }
            }
            System.out.println();
        }
    }


    public TrafficGraph getRawRoadGraph() {
        return rawRoadGraph;
    }

    public void setRawRoadGraph(TrafficGraph rawRoadGraph) {
        this.rawRoadGraph = rawRoadGraph;
    }

}

