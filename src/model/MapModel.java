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
                if(tileGrid[r][c] == null) tileGrid[r][c] = new Tile(instance, tileGrid[r][c].getCornerHeights());
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
                station = new Station(model, null, null, null, model.getPathfinder(), null);
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
                mergeTrafficPartsIfNeccessary(addedPoints.get(0));
            }
            if(instance instanceof Runway){
                if(createdNewStation){
                    ConnectedTrafficPart connectedTrafficPart = addNewStationToTrafficLineOrCreateNewTrafficLine(station, instance.getTrafficType());
                    instance.setTrafficLine(connectedTrafficPart);
                }
                //checke, ob man zwei TrafficLines mergen sollte
                mergeTrafficAirPartsIfNeccessary(addedPoints.get(0));
                return instance;
            }
        }


        if(createdNewStation){
            ConnectedTrafficPart connectedTrafficPart = addNewStationToTrafficLineOrCreateNewTrafficLine(station, instance.getTrafficType());
            instance.setTrafficLine(connectedTrafficPart);
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

        if(building.getBuildingName().equals("remove")
                && !tileGrid[row][column].isWater()
                && !(tileGrid[row][column].getBuilding() instanceof Factory)) return true;

        for(int r=row; r<row+building.getWidth(); r++){
            for(int c=column; c<column+building.getDepth(); c++){
                Tile tile = tileGrid[r][c];
//                if(tile.getHeight() < 0) return false;
                // TODO Wenn Höhe nicht passt, return false

                //Auf Wasserfeldern darf nicht gebaut werden
                if(tile.isWater()) return false;

                if(tile.getBuilding() instanceof Road) {
                    boolean canCombine = model.checkCombines(row, column, building) != building;
                    // Wenn eine strasse abgerissen werden soll, soll ebenfalls true zurückgegeben werden
                    if(! canCombine) return false;
                }
                else if (tile.getBuilding() instanceof Rail){
                    boolean canCombine = model.checkCombines(row, column, building) != building;
                    if(! canCombine) return false;
                }

                else {
                    // Auf Graßfelder soll wieder gebaut werden dürfen
                    if(! ((tile.getBuilding() instanceof Nature) ||
                            tile.getBuilding().getBuildingName().equals("grass"))) return false;
                }
            }
        }

        //TODO Runways werden beim platzieren abgeschnitten

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
     * Prüft ausgehend von einem neu hinzugefügten Knoten, ob 2 Verkehrsteile verbunden wurden. Wenn das der Fall ist,
     * fügt es die beiden Verkehrsteile zu einer zusammen.
     * @param newAddedVertex
     */
    private void mergeTrafficPartsIfNeccessary(Vertex newAddedVertex){
        List<Station> nearStations = model.getPathfinder().findAllDirectlyConnectedStations(newAddedVertex);
        Set<ConnectedTrafficPart> differentLines = new HashSet<>();
        for(Station station: nearStations){
            differentLines.add(station.getRoadTrafficPart());
            //TODO Wann wird TrafficPart in Station gesetzt?
        }
        int numberOfNearTrafficLines = differentLines.size();
        System.out.println(numberOfNearTrafficLines);
        if(numberOfNearTrafficLines > 1){
            System.out.println("tried to merge trafficLines");
            System.out.println("found lines "+numberOfNearTrafficLines);
            mergeTrafficParts(new ArrayList<>(differentLines));
        }
    }

    private void mergeTrafficAirPartsIfNeccessary(Vertex newAddedVertex){
        List<Station> nearStations = new ArrayList<>();
        for (Station s :stations) {
            nearStations.add(s);
        }

//        List<Station> nearStations = model.getPathfinder().findAllDirectlyConnectedStations(newAddedVertex);
        Set<ConnectedTrafficPart> differentLines = new HashSet<>();
        for(Station station: nearStations){
            differentLines.add(station.getAirTrafficPart());
            //TODO Wann wird TrafficPart in Station gesetzt?
        }
        int numberOfNearTrafficLines = differentLines.size();
        System.out.println(numberOfNearTrafficLines);
        if(numberOfNearTrafficLines > 1){
            System.out.println("tried to merge trafficLines");
            System.out.println("found lines "+numberOfNearTrafficLines);
            mergeTrafficAirParts(new ArrayList<>(differentLines));
        }
    }

    /**
     * Fügt die angegebenen Verkehrsteile zu einem zusammen
     * @param parts
     */
    private void mergeTrafficParts(List<ConnectedTrafficPart> parts){
        ConnectedTrafficPart firstPart = parts.get(0);
        System.out.println("firstPart "+firstPart.getStations().size());
        for(int i=1; i<parts.size(); i++){
            firstPart.mergeWithTrafficPart(parts.get(i));
            model.getActiveTrafficParts().remove(parts.get(i));
            model.getNewCreatedOrIncompleteTrafficParts().remove(parts.get(i));
        }
        System.out.println("firstPart after merge "+firstPart.getStations().size());
    }

    private void mergeTrafficAirParts(List<ConnectedTrafficPart> parts){
        ConnectedTrafficPart firstPart = parts.get(0);
        System.out.println("firstPart "+firstPart.getStations().size());
        for(int i=1; i<parts.size(); i++){
            firstPart.mergeWithAirTrafficPart(parts.get(i));
            model.getActiveTrafficParts().remove(parts.get(i));
            model.getNewCreatedOrIncompleteTrafficParts().remove(parts.get(i));
        }
        System.out.println("firstPart after merge "+firstPart.getStations().size());
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
            if(building.getTrafficType().equals(TrafficType.AIR)) {
                trafficGraph = this.rawRoadGraph;
            }
            //TODO rails

            //Vielleicht sollte man für Flugzeuge eine eigene globale Variable von TrafficGraph erstellen, der die Punkte der
            // Flugverbindungen abspeichert. Dann müssten auch im Pathfinder unterschiedliche Graphen benutzt werden,
            // je nach TrafficType, und die Methode addNewStationToTrafficLineOrCreateNewTrafficLine() mit Sicherheit auch

            else {
                throw new RuntimeException("Unfertiger Code");
            }
        }

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
        building.getVertices().addAll(addedVertices);
        for(Vertex addedV: addedVertices){
            addedV.setBuilding(building);
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
    private ConnectedTrafficPart addNewStationToTrafficLineOrCreateNewTrafficLine(Station newStation, TrafficType trafficType) {
        List<Vertex> pathToStation;
        if (trafficType == TrafficType.AIR && stations.size() > 1) {
            Vertex startVertex = stations.get(0).getComponents().get(0).getVertices().iterator().next();
            Vertex endVertex = newStation.getComponents().get(0).getVertices().iterator().next();
            pathToStation = model.getPathfinder().findPathForPlane(startVertex, endVertex);
        }
        else {
            pathToStation = model.getPathfinder().findPathToNextStation(newStation);
        }

        boolean anotherStationFindable = false;
        if (pathToStation.size() > 0) anotherStationFindable = true;

        if (anotherStationFindable) {
            Vertex lastVertex = pathToStation.get(pathToStation.size() - 1);
            Station nextStation = lastVertex.getStation();
            if (trafficType.equals(TrafficType.ROAD)) {
                nextStation.getRoadTrafficPart().addStationAndUpdateConnectedStations(newStation);
                newStation.setRoadTrafficPart(nextStation.getRoadTrafficPart());
                return nextStation.getRoadTrafficPart();
            }
            else if (trafficType.equals(TrafficType.AIR)) {
                ConnectedTrafficPart trafficPart = new ConnectedTrafficPart(model, TrafficType.AIR, stations.get(0));
                nextStation.setAirTrafficPart(trafficPart);
                nextStation.getAirTrafficPart().addStationAndUpdateConnectedStations(newStation);
                // TODO: hack:  erste Station löschen
                model.getNewCreatedOrIncompleteTrafficParts().remove();
                model.getNewCreatedOrIncompleteTrafficParts().add(nextStation.getAirTrafficPart());

                //stations.get(0).setAirTrafficLine(nextStation.getAirTrafficLine());
               // nextStation.getRoadTrafficLine().addStationAndUpdateConnectedStations(newStation);
               // newStation.setRoadTrafficLine(nextStation.getRoadTrafficLine());
                return nextStation.getAirTrafficPart();
            }
            else ; //TODO Andere Verkehrstypen
        } else {
            ConnectedTrafficPart connectedTrafficPart = null;
            switch (trafficType) {
                case AIR:
                    connectedTrafficPart = new ConnectedTrafficPart(model, TrafficType.AIR, newStation);
                    newStation.setAirTrafficPart(connectedTrafficPart);
                    break;
                case RAIL:
                    break;
                case ROAD:
                    connectedTrafficPart = new ConnectedTrafficPart(model, TrafficType.ROAD, newStation);
                    newStation.setRoadTrafficPart(connectedTrafficPart);
                    break;
                default:
                    break;
            }
            // TODO AIR, RAIL, desiredNumber
            // Es crasht hier manchmal, weil Rails noch nicht umgesetzt ist

            model.getNewCreatedOrIncompleteTrafficParts().add(connectedTrafficPart);
            return connectedTrafficPart;
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

