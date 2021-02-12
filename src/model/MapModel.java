package model;

import java.util.*;

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
        System.out.println("place building "+instance.getBuildingName()+" with TrafficType "+instance.getTrafficType());
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
                station = new Station(model, null, null, null, model.getPathfinder());
                stations.add(station);
                System.out.println("Station neu erzeugt");
                createdNewStation = true;
            }
            station.addBuildingAndSetStationInBuilding((Stop) instance);
            System.out.println("StationID in placeBuilding "+((Stop) instance).getStation().getId());

        }
        if(instance instanceof PartOfTrafficGraph){
            List<Vertex> addedPoints = model.getMap().addPointsToGraph((PartOfTrafficGraph) instance, row, column);
            if(instance instanceof Road){
                //checke, ob man zwei TrafficLines mergen sollte
                mergeTrafficLinesIfNeccessary(addedPoints.get(0));
            }
        }


        if(createdNewStation){
            TrafficLine trafficLine = addNewStationToTrafficLineOrCreateNewTrafficLine(station, instance.getTrafficType());
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
                if(tile.getBuilding() != null && tile.getBuilding().getBuildingName().equals("grass")
                    || tile.getBuilding() != null && tile.getBuilding().getBuildingName().equals("flat")){
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

    //TODO Funktioniert momentan nur für ROAD

    /**
     * Prüft ausgehend von einem neu hinzugefügten Knoten, ob 2 Verkehrslinien verbunden wurden. Wenn das der Fall ist,
     * fügt es die beiden Verkehrlinien zu einer zusammen.
     * @param newAddedVertex
     */
    private void mergeTrafficLinesIfNeccessary(Vertex newAddedVertex){
        System.out.println("mergeTrafficLinesIfNeccessary called");

        List<Station> nearStations = model.getPathfinder().findAllDirectlyConnectedStations(newAddedVertex);
        Set<TrafficLine> differentLines = new HashSet<>();
        for(Station station: nearStations){
            differentLines.add(station.getRoadTrafficLine());
        }
        int numberOfNearTrafficLines = differentLines.size();
        System.out.println(numberOfNearTrafficLines);
        if(numberOfNearTrafficLines > 1){
            System.out.println("tried to merge trafficLines");
            System.out.println("found lines "+numberOfNearTrafficLines);
            mergeTrafficLines(new ArrayList<>(differentLines));
        }
    }

    /**
     * Fügt die angegebenen Verkehrslinien zu einer zusammen
     * @param lines
     */
    private void mergeTrafficLines(List<TrafficLine> lines){
        TrafficLine firstLine = lines.get(0);
        System.out.println("firstLine "+firstLine.getStations().size());
        for(int i=1; i<lines.size(); i++){
            firstLine.mergeWithTrafficLine(lines.get(i));
            model.getActiveTrafficLines().remove(lines.get(i));
            model.getNewCreatedOrIncompleteTrafficLines().remove(lines.get(i));
        }
        System.out.println("firstLine after merge "+firstLine.getStations().size());
    }

    /**
     * Gibt die Station zurück, die direkt neben der Haltestelle in x- oder y-Richtung steht. Wenn keine Station angrenzt,
     * wird null zurückgegeben
     * @param row
     * @param column
     * @param building
     * @return
     */
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

    //TODO refactoring
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
        if(building == null) throw new IllegalArgumentException("building was null");
        if(building.getTrafficType().equals(TrafficType.ROAD)) {
            trafficGraph = this.rawRoadGraph;
        }
        else {
            //TODO rails
            //TODO Air ?

            //Vielleicht sollte man für Flugzeuge eine eigene globale Variable von TrafficGraph erstellen, der die Punkte der
            // Flugverbindungen abspeichert. Dann müssten auch im Pathfinder unterschiedliche Graphen benutzt werden,
            // je nach TrafficType, und die Methode addNewStationToTrafficLineOrCreateNewTrafficLine() mit Sicherheit auch

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
        List<Vertex> joinedVertices = trafficGraph.checkForDuplicatePoints();
        trafficGraph.printGraph();
        System.out.println();
        List<Vertex> verticesAfter = new ArrayList<>(trafficGraph.getMapOfVertexes().values());
        System.out.println(verticesAfter);
        verticesAfter.removeAll(verticesBefore);
        System.out.println(verticesAfter);

        List<Vertex> addedVertices = verticesAfter;
        for(Vertex j:joinedVertices){
            if(!addedVertices.contains(j)){
                addedVertices.add(j);
            }
        }
        if(isPointPartOfStation){
            ((Stop) building).getVertices().addAll(addedVertices);
        }
        return addedVertices;
    }

    /**
     * Wenn keine andere Station im Straßengraphen findbar, fügt es dem Straßengraph eine neue Verkehrslinie hinzu. Wenn eine andere Station
     * findbar, wird der verkehrslinie der gefundenen Station die angegebene Station hinzugefügt
     * @param newStation
     * @param trafficType
     * @return
     */
    private TrafficLine addNewStationToTrafficLineOrCreateNewTrafficLine(Station newStation, TrafficType trafficType) {
        List<Vertex> pathToStation = model.getPathfinder().findPathToNextStation(newStation);

        boolean anotherStationFindable = false;
        if (pathToStation.size() > 0) anotherStationFindable = true;

        if (anotherStationFindable) {
            Vertex lastVertex = pathToStation.get(pathToStation.size() - 1);
            Station nextStation = lastVertex.getStation();
            if (trafficType.equals(TrafficType.ROAD)) {
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
                    trafficLine = new TrafficLine(2, model, TrafficType.ROAD, newStation);
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

