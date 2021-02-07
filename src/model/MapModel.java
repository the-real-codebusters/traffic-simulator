package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapModel {
    private String mapgen;

    private int width;
    private int depth;
    private Tile[][] tileGrid;

    private BasicModel model;
    private Long adjacentStationId;
    private List<Station> stations = new ArrayList<>();
    private TrafficGraph rawRoadGraph = new TrafficGraph();


    public MapModel(int width, int depth, BasicModel model) {
        this.width = width;
        this.depth = depth;
        this.tileGrid = new Tile[depth][width];
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
//        if(instance instanceof PartOfTrafficGraph) System.out.println("points "+((PartOfTrafficGraph) instance).getPoints());
        for(int r=row; r<row+instance.getWidth(); r++){
            for(int c=column; c<column+instance.getDepth(); c++){
                if(tileGrid[r][c] == null) tileGrid[r][c] = new Tile(instance, tileGrid[r][c].getCornerHeights(), false);
                else tileGrid[r][c].setBuilding(instance);
            }
        }
        Tile originTile = tileGrid[row][column];
        originTile.setBuildingOrigin(true);
        instance.setOriginColumn(column);
        instance.setOriginRow(row);

        boolean createdNewStation = false;
        Station station = null;

        if(instance instanceof Stop) {
            Station nextStation = getStationNextToStop(row, column, (Stop) instance);
//            ((Stop) instance).getSpecial().equals("busstop");
            if(nextStation != null) {
                station = nextStation;
                System.out.println("Nachbar für Stop gefunden");
            } else {
                station = new Station(model, null, null, null);
                stations.add(station);
                System.out.println("Station neu erzeugt");
                createdNewStation = true;
            }
            station.addBuildingAndSetStationInBuilding((Stop) instance);
            System.out.println("StationID in placeBuilding "+((Stop) instance).getStation().getId());

        }
        if(instance instanceof PartOfTrafficGraph){
            model.getMap().addPointsToGraph((PartOfTrafficGraph) instance, row, column);
        }

        if(createdNewStation){
            TrafficLine trafficLine = addNewStationToTrafficLine(station, instance.getTrafficType());
            instance.setTrafficLine(trafficLine);
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
                Tile tile = tileGrid[r][c];
//                if(tile.getHeight() < 0) return false;
                // TODO Wenn Höhe nicht passt, return false

                if(tile.getBuilding() instanceof Road) {
                    // TODO Mache es allgemeiner, indem es auch für Rail implementiert wird
                    boolean canCombine = model.checkCombines(row, column, building) != building;
                    // Wenn eine strasse abgerissen werden soll, soll ebenfalls true zurückgegeben werden
                    if(canCombine || building.getBuildingName().equals("remove")){
                        return true;
                    }
                }

                if(tile.getBuilding() instanceof Rail) {
                    boolean canCombine = model.checkCombines(row, column, building) != building;
                    if(canCombine || building.getBuildingName().equals("remove")){
                        return true;
                    }
                }

                // Auf Graßfelder soll wieder gebaut werden dürfen
                if(tile.getBuilding() != null && tile.getBuilding().getBuildingName().equals("grass")){
                    return true;
                }

                if(tile.getBuilding() instanceof Stop && building.getBuildingName().equals("remove")) return true;

                if(! (tile.getBuilding() instanceof Nature)) return false;
            }
        }

        if(building instanceof Stop){
            adjacentStationId = -1L;
            for(int r=row; r<row+building.getWidth(); r++){
                Building adjacentBuilding = tileGrid[r][column -1].getBuilding();
                if(adjacentBuilding instanceof Stop) {
                    if(checkForSecondStation(adjacentBuilding)) return false;
                }
                adjacentBuilding = tileGrid[r][column+ building.getDepth()].getBuilding();
                if(adjacentBuilding instanceof Stop) {
                    if(checkForSecondStation(adjacentBuilding)) return false;
                }
            }

            for(int c=column; c<column+building.getDepth(); c++){
                Building adjacentBuilding = tileGrid[row-1][c].getBuilding();
                if(adjacentBuilding instanceof Stop) {
                    if(checkForSecondStation(adjacentBuilding)) return false;
                }
                adjacentBuilding = tileGrid[row+building.getWidth()][c].getBuilding();
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
            Building adjacentBuilding = tileGrid[r][column -1].getBuilding();
            if(adjacentBuilding instanceof Stop) {
                return ((Stop) adjacentBuilding).getStation();
            }
            adjacentBuilding = tileGrid[r][column+ building.getDepth()].getBuilding();
            if(adjacentBuilding instanceof Stop) {
                return ((Stop) adjacentBuilding).getStation();
            }
        }

        for(int c=column; c<column+building.getDepth(); c++){
            Building adjacentBuilding = tileGrid[row-1][c].getBuilding();
            if(adjacentBuilding instanceof Stop) {
                return ((Stop) adjacentBuilding).getStation();
            }
            adjacentBuilding = tileGrid[row+building.getWidth()][c].getBuilding();
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
     * @return Eine Liste der Vertices, die zum Graph hinzugefügt wurden
     */
    public List<Vertex> addPointsToGraph(PartOfTrafficGraph building, int xCoordOfTile, int yCoordOfTile) {
        TrafficGraph trafficGraph;
        if(building instanceof PartOfTrafficGraph) {
            trafficGraph = this.rawRoadGraph;
        }
        else {
            //TODO rails
            throw new RuntimeException("Unfertiger Code");
        };

        // TODO Vertex zusammenführen überprüfen

        boolean isPointPartOfStation = false;
        if(building instanceof Stop) isPointPartOfStation = true;
        List<Vertex> verticesBefore = new ArrayList<>(trafficGraph.getMapOfVertexes().values());

                Map<String, List<Double>> points = building.getPoints();
                for (Map.Entry<String, List<Double>> entry : points.entrySet()) {

                    // identifier wird dem Name eines Knotens hinzugefügt, damit der Name unique bleibt,
                    // sonst gäbe es Duplikate, da points aus verschiedenen Felder denselben Namen haben könnten
                    String identifier = xCoordOfTile + "-" + yCoordOfTile + "-";
                    String vertexName = identifier + entry.getKey();

                    double xCoordOfPoint = entry.getValue().get(0);
                    double yCoordOfPoint = entry.getValue().get(1);

                    Vertex v = new Vertex(vertexName, xCoordOfPoint, yCoordOfPoint, xCoordOfTile, yCoordOfTile);
                    v.setPointOfStation(isPointPartOfStation);
                    if(isPointPartOfStation) {
                        v.setStation(((Stop) building).getStation());
                    }
                    trafficGraph.addVertex(v);

                    for (Vertex v1 : trafficGraph.getMapOfVertexes().values()) {
                        List<List<String>> edges = building.getTransportations();
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
        List<Vertex> verticesAfter = new ArrayList<>(trafficGraph.getMapOfVertexes().values());
        System.out.println(verticesAfter);
        verticesAfter.removeAll(verticesBefore);
        System.out.println(verticesAfter);

        List<Vertex> addedVertices = verticesAfter;
        if(isPointPartOfStation){
            ((Stop) building).getVertices().addAll(addedVertices);
        }
        return addedVertices;
    }

    private TrafficLine addNewStationToTrafficLine(Station newStation, TrafficType trafficType) {
        List<Vertex> pathToStation = model.getPathfinder().findPathToNextStation(newStation);

        boolean anotherStationFindable = false;
        if (pathToStation.size() > 0) anotherStationFindable = true;

        if (anotherStationFindable) {
            Vertex lastVertex = pathToStation.get(pathToStation.size() - 1);
            Station nextStation = lastVertex.getStation();
            if (trafficType.equals(TrafficType.ROAD)) {
                System.out.println("nextStation " + nextStation.getId());
                nextStation.getRoadTrafficLine().addStationAndUpdateConnectedStations(newStation);
                newStation.setRoadTrafficLine(nextStation.getRoadTrafficLine());
                return nextStation.getRoadTrafficLine();
            } else ; //TODO Andere Verkehrstypen
        } else {
            TrafficLine trafficLine = null;
            switch (trafficType) {
                case AIR:
                    break;
                case RAIL:
                    break;
                case ROAD:
                    trafficLine = new TrafficLine(3, model, TrafficType.ROAD, newStation);
                    newStation.setRoadTrafficLine(trafficLine);
                    break;
                default:
                    break;
            }
            // TODO AIR, RAIL, desiredNumber
            // Es crasht hier manchmal, weil Rails noch nicht umgesetzt ist

            model.getNewCreatedOrIncompleteTrafficLines().add(trafficLine);
            return trafficLine;
        }
        throw new RuntimeException("Unfertiger Code");
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
        return tileGrid;
    }

    public void setTileGrid(Tile[][] tileGrid) {
        this.tileGrid = tileGrid;
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
//            System.out.println();
        }
    }


    public TrafficGraph getRawRoadGraph() {
        return rawRoadGraph;
    }

    public void setRawRoadGraph(TrafficGraph rawRoadGraph) {
        this.rawRoadGraph = rawRoadGraph;
    }

}

