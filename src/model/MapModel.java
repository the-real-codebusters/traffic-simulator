package model;

import javafx.geometry.Point2D;

import java.util.*;

public class MapModel {
    private String mapgen;

    private int width;
    private int depth;
    private Tile[][] tileGrid;

    private BasicModel model;
    private Long adjacentStationId;
    private List<Station> stations = new ArrayList<>();
    private TrafficGraph roadGraph = new TrafficGraph();
    private TrafficGraph railGraph = new TrafficGraph();
    private TrafficGraph airGraph = new TrafficGraph();
    private TrafficLineGraph trafficLineGraph = new TrafficLineGraph();

    public MapModel(int width, int depth, BasicModel model) {
        this.width = width;
        this.depth = depth;
        this.tileGrid = new Tile[depth][width];
        this.model = model;
    }

    /**
     * Platziert das Gebäude building an der angegebenen Stelle
     *
     * @param row
     * @param column
     * @param building
     * @return
     */
    public Building placeBuilding(int row, int column, Building building) {

        Building instance = building.getNewInstance();
        //Setzt Ursprungstile auf das tile ganz links
        Tile originTile = tileGrid[row][column];
        originTile.setBuildingOrigin(true);
        instance.setOriginColumn(column);
        instance.setOriginRow(row);
//        System.out.println("building name in placeBuilding: " + building.getBuildingName());
//        System.out.println("origin row: " + instance.getOriginRow());
//        System.out.println("origin column: " + instance.getOriginColumn());

        //Setzt für jedes zugrundeliegende Tile das neue Building
        for (int r = row; r < row + instance.getWidth(); r++) {
            for (int c = column; c < column + instance.getDepth(); c++) {
                if (tileGrid[r][c] == null)
                    tileGrid[r][c] = new Tile(instance, tileGrid[r][c].getCornerHeights(), false);
                else {
                    removePreviousBuildingOutOfVertices(r, c);
                    tileGrid[r][c].setBuilding(instance);
                }
            }
        }

        //Variable, die anzeigt ob eine neue Station kreirt wird / wurde
        boolean createdNewStation = false;
        Station station = null;
        boolean worked = true;

        if (instance instanceof Stop) {
            //Sucht, ob direkt daneben eine existierende Station steht und verbindet sich in dem Fall damit.
            //Ansonsten wird eine neue Station erzeugt
            Station nextStation = getStationNextToStop(row, column, (Stop) instance);
            if (nextStation != null) {
                station = nextStation;
//                System.out.println("Nachbar für Stop gefunden");
                //TODO Fall ignoriert, wenn Station durch immer neue Stops näher an Factory gebaut wird
//                TrafficType type = instance.getTrafficType();
//                station.setTrafficPartForTrafficType(nextStation.getTrafficPartForTrafficType(type), type);
            } else {

//                System.out.println("stop: "+instance.getBuildingName()+" found factory: "+getNearFactory((Stop) instance, row, column));
                station = new Station(model.getPathfinder(), model);
                stations.add(station);
//                System.out.println("Station neu erzeugt");
//                System.out.println("stations in placebuilding " + stations);
//                System.out.println("airstations in placebuilding " + getAllAirStations());

                createdNewStation = true;
            }
            Factory nearFactory = getNearFactory(row, column);
            if (nearFactory != null) {
                station.setNearFactory(nearFactory);
                nearFactory.getNearStations().add(station);
            }

            worked = station.addBuildingAndSetStationInBuilding((Stop) instance, nextStation == null);
//            System.out.println("StationID in placeBuilding " + ((Stop) instance).getStation().getId());
        }
        if (instance instanceof PartOfTrafficGraph) {
            List<Vertex> addedPoints = model.getMap().addPointsToGraph((PartOfTrafficGraph) instance, row, column);
            if (instance instanceof Road) {
                //checke, ob man zwei TrafficParts mergen sollte
                mergeTrafficPartsIfNecessary(addedPoints, TrafficType.ROAD);
            } else if (instance instanceof Rail) {

                if (((Rail) instance).isSignal()) {
                    splitRailblocks((Rail) instance, addedPoints);
                } else {
                    //checke, ob man zwei TrafficParts mergen sollte
                    mergeTrafficPartsIfNecessary(addedPoints, TrafficType.RAIL);
                }
            }
//            if (instance instanceof Runway) {
//                if (createdNewStation) {
//                    ConnectedTrafficPart connectedTrafficPart = addNewStationToTrafficPartOrCreateNewTrafficPart(station, instance.getTrafficType());
//                    instance.setTrafficPart(connectedTrafficPart);
//                }
            //checke, ob man zwei TrafficLines mergen sollte
//                mergeTrafficAirPartsIfNeccessary(addedPoints.get(0));
//                return instance;
//            }
        }

        if (createdNewStation) {
            ConnectedTrafficPart connectedTrafficPart = addNewStationToTrafficPartOrCreateNewTrafficPart(station, instance.getTrafficType());
            instance.setTrafficPart(connectedTrafficPart);
        }
        if(!worked){
            ConnectedTrafficPart newTrafficPart = new ConnectedTrafficPart(model, building.getTrafficType(), station);
            model.getNewCreatedOrIncompleteTrafficParts().add(newTrafficPart);
        }

        return instance;
    }

    private void splitRailblocks(Rail railsignal, List<Vertex> addedVertices) {
        if (addedVertices.size() != 3) {
            //Signals müssen immer 3 Punkte haben
            throw new RuntimeException("Signals müssen immer 3 Punkte haben");
        }

        Vertex middleVertex = null;
        for (Vertex v : addedVertices) {

            if (v.getxCoordinateRelativeToTileOrigin() > 0 && v.getxCoordinateRelativeToTileOrigin() < 1
                    && v.getyCoordinateRelativeToTileOrigin() > 0 && v.getyCoordinateRelativeToTileOrigin() < 1) {
                middleVertex = v;
            }
        }

        //Entferne middleVertex temporär aus Graph
        List<Vertex> edges = railGraph.getAdjacencyMap().get(middleVertex.getName());
        railGraph.removeVertex(middleVertex.getName());

        //Suche jeweils in verschiedene Richtungen
        Set<Vertex> verticesRailblock1 = model.getPathfinder().findAllConnectedVerticesUntilSignal(edges.get(0), railsignal);

        Set<Vertex> verticesRailblock2 = model.getPathfinder().findAllConnectedVerticesUntilSignal(edges.get(1), railsignal);

        Railblock block1 = new Railblock();
        block1.addVertices(verticesRailblock1);

        Railblock block2 = new Railblock();
        block2.addVertices(verticesRailblock2);
        block2.addVertices(new HashSet<>(Arrays.asList(middleVertex)));

        railGraph.addVertex(middleVertex);
        railGraph.addEdgeBidirectional(edges.get(0).getName(), middleVertex.getName());
        railGraph.addEdgeBidirectional(edges.get(1).getName(), middleVertex.getName());
//
//        System.out.println("block1 in splitRailblocks: ");
//        block1.getVertices().forEach((x) -> System.out.println(x.getName()));
//
//        System.out.println("block2 in splitRailblocks: ");
//        block2.getVertices().forEach((x) -> System.out.println(x.getName()));

    }


    /**
     * Gibt zurück, ob das Gebäude an der angegebenen Stelle platziert werden darf
     *
     * @param row
     * @param column
     * @param building
     * @return
     */
    public boolean canPlaceBuilding(int row, int column, Building building) {
        if ((row + building.getWidth()) >= depth) return false;
        if ((column + building.getDepth()) >= width) return false;

        if (building.getBuildingName().equals("remove")
                && !tileGrid[row][column].isWater()
                && !(tileGrid[row][column].getBuilding() instanceof Factory)) return true;

        if (building instanceof JustCombines) {
            boolean canCombine = model.checkCombines(row, column, building) != building;
            if (!canCombine) return false;
        }

        for (int r = row; r < row + building.getWidth(); r++) {
            for (int c = column; c < column + building.getDepth(); c++) {
                Tile tile = tileGrid[r][c];

                // Wenn der dz Wert des Gebäudes nicht zu dem Tile passt, auf dem platziert werden soll,
                // soll false zurückgegeben werden
//                Tile tilet = tileGrid[row][column];
                Map<String, Integer> cornerHeights = tile.getCornerHeights();
                int heightShift = Math.abs(tile.findMinCorner(cornerHeights) - tile.findMaxCorner(cornerHeights));
                if (building.getDz() < heightShift) return false;


                // Fabriken werden beim erzeugen der Map nur auf komplett ebene Flächen platziert
                if (building instanceof Factory && tile.getBuilding() != null) {
                    if (!((tile.getBuilding() instanceof Nature) ||
                            tile.getBuilding().getBuildingName().equals("grass") ||
                            tile.getBuilding().getBuildingName().equals("0000")
                                    && tile.getCornerHeights().get("cornerS") > 0
                    )) return false;
                }

                // TODO Wenn Höhe nicht passt, return false

                //Auf Wasserfeldern darf nicht gebaut werden
                if (tile.isWater()) return false;


                if (tile.getBuilding() instanceof Road || tile.getBuilding() instanceof Rail) {
                    boolean canCombine = model.checkCombines(row, column, building) != building;
                    if (!canCombine) return false;
                } else {
                    // Auf Graßfelder soll wieder gebaut werden dürfen
                    if (!((tile.getBuilding() instanceof Nature) ||
                            tile.getBuilding().getBuildingName().equals("grass") ||
                            tile.getBuilding().getBuildingName().equals("ground")
                    )) return false;
                }
            }
        }

        if (building instanceof Stop) {

            if (isTooCloseToFactory(building, row, column)) return false;


            adjacentStationId = -1L;
            for (int r = row; r < row + building.getWidth(); r++) {
                Building adjacentBuilding = tileGrid[r][column - 1].getBuilding();
                if (adjacentBuilding instanceof Stop) {
                    if (checkForSecondStation(adjacentBuilding)) return false;
                }
                adjacentBuilding = tileGrid[r][column + building.getDepth()].getBuilding();
                if (adjacentBuilding instanceof Stop) {
                    if (checkForSecondStation(adjacentBuilding)) return false;
                }
            }

            for (int c = column; c < column + building.getDepth(); c++) {
                Building adjacentBuilding = tileGrid[row - 1][c].getBuilding();
                if (adjacentBuilding instanceof Stop) {
                    if (checkForSecondStation(adjacentBuilding)) return false;
                }
                adjacentBuilding = tileGrid[row + building.getWidth()][c].getBuilding();
                if (adjacentBuilding instanceof Stop) {
                    if (checkForSecondStation(adjacentBuilding)) return false;
                }
            }
        }
        return true;
    }


    /**
     * Prüft, ob die zu bauende Station zu nah an einer Fabrik ist.
     * Alle Stationen müssen einen Mindestabstand von 1 Tile zur nächsten Fabrik einhalten. Der kleine Tower
     * muss einen Abstand von mindestens 2 Tiles einhalten und der große Tower von mindestens 3 Tiles
     *
     * @param building die zu platzierende Haltestelle
     * @param row
     * @param column
     * @return
     */
    private boolean isTooCloseToFactory(Building building, int row, int column) {

        // Wenn es sich um einen Busstop oder einen Bahnhof handelt, muss mindestens eine Zeile/Spalte
        // Abstand zu einer Fabrik eingehalten werden
        Point2D tileCoords = new Point2D(row, column);
        Map<Tile, Point2D> neighbors = getAllNeighbors(tileCoords);
        if(neighbors.size() == 0) return true;
        for (Tile neighbor : neighbors.keySet()) {
            if (neighbor.getBuilding() instanceof Factory) {
                return true;
            }
            // Wenn es sich um einen kleinen Tower handelt, müssen mindestens 2 Zeilen/Spalten
            // Abstand zu einer Fabrik eingehalten werden
            if (building.getBuildingName().contains("tower")) {
                Map<Tile, Point2D> neighborsOfNeighbor = getAllNeighbors(neighbors.get(neighbor));
                if(neighborsOfNeighbor.size() == 0) return true;
                for (Tile neighborOfNeighbor : neighborsOfNeighbor.keySet()) {
                    if (neighborOfNeighbor.getBuilding() instanceof Factory) {
                        return true;
                    }
                    // Wenn es sich um einen großen Tower handelt, müssen mindestens 3 Zeilen/Spalten
                    // Abstand zu einer Fabrik eingehalten werden
                    if (building.getBuildingName().equals("big tower")) {
                        Map<Tile, Point2D> thirdNeighbors = getAllNeighbors(neighborsOfNeighbor.get(neighborOfNeighbor));
                        if(thirdNeighbors.size() == 0) return true;
                        for (Tile thirdNeighbor : thirdNeighbors.keySet()) {
                            if (thirdNeighbor.getBuilding() instanceof Factory) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }


    private Factory getNearFactory(int row, int column) {

        // Wenn es sich um einen Busstop oder einen Bahnhof handelt, muss mindestens eine Zeile/Spalte
        // Abstand zu einer Fabrik eingehalten werden

        //Abstand von höchstens 4 Tiles zwischen factory und station

        Point2D tileCoords = new Point2D(row, column);
        Map<Tile, Point2D> neighbors = getAllNeighbors(tileCoords);
        if(neighbors.size() == 0) return null;
        for (Tile neighbor : neighbors.keySet()) {
            if (neighbor.getBuilding() instanceof Factory) {
                return (Factory) neighbor.getBuilding();
            }

            Map<Tile, Point2D> neighborsOfNeighbor = getAllNeighbors(neighbors.get(neighbor));
            if(neighborsOfNeighbor.size() == 0) return null;
            for (Tile neighborOfNeighbor : neighborsOfNeighbor.keySet()) {
                if (neighborOfNeighbor.getBuilding() instanceof Factory) {
                    return (Factory) neighborOfNeighbor.getBuilding();
                }

                Map<Tile, Point2D> thirdNeighbors = getAllNeighbors(neighborsOfNeighbor.get(neighborOfNeighbor));
                if(thirdNeighbors.size() == 0) return null;
                for (Tile thirdNeighbor : thirdNeighbors.keySet()) {
                    if (thirdNeighbor.getBuilding() instanceof Factory) {
                        return (Factory) thirdNeighbor.getBuilding();
                    }

                    Map<Tile, Point2D> fourthNeighbors = getAllNeighbors(thirdNeighbors.get(thirdNeighbor));
                    if(fourthNeighbors.size() == 0) return null;
                    for (Tile fourthNeighbor : fourthNeighbors.keySet()) {
                        if (fourthNeighbor.getBuilding() instanceof Factory) {
                            return (Factory) fourthNeighbor.getBuilding();
                        }
                    }
                }
            }
        }
        return null;
    }


    /**
     * Erzeugt eine Map, die alle Nachbar-Tiles mit entsprechenden Koordinaten enthält
     *
     * @param coords Koordinaten des Tiles, dessen Nachbarn gesucht werden
     * @return
     */
    private Map<Tile, Point2D> getAllNeighbors(Point2D coords) {
        int xCoord = (int) coords.getX();
        int yCoord = (int) coords.getY();

        Map<Tile, Point2D> neighbors = new LinkedHashMap<>();

        // Es muss ein Mindestabstand zum Rand der Map eingehalten werden
        boolean tooCloseToBorder = xCoord < 1 || yCoord < 1 || xCoord > depth-2 || yCoord > width-2;
        if(!tooCloseToBorder){
            Tile tileNW = tileGrid[xCoord - 1][yCoord];     // NW
            Tile tileNE = tileGrid[xCoord][yCoord + 1];     // NE
            Tile tileSE = tileGrid[xCoord + 1][yCoord];     // SE
            Tile tileSW = tileGrid[xCoord][yCoord - 1];     // SW

            Tile tileN = tileGrid[xCoord - 1][yCoord + 1];     // N
            Tile tileE = tileGrid[xCoord + 1][yCoord + 1];     // E
            Tile tileS = tileGrid[xCoord + 1][yCoord - 1];     // S
            Tile tileW = tileGrid[xCoord - 1][yCoord - 1];     // W

            Point2D NW = new Point2D(xCoord - 1, yCoord);     // NW
            Point2D NE = new Point2D(xCoord, yCoord + 1);     // NE
            Point2D SE = new Point2D(xCoord + 1, yCoord);     // SE
            Point2D SW = new Point2D(xCoord, yCoord - 1);     // SW

            Point2D N = new Point2D(xCoord - 1, yCoord + 1);     // N
            Point2D E = new Point2D(xCoord + 1, yCoord + 1);     // E
            Point2D S = new Point2D(xCoord + 1, yCoord - 1);     // S
            Point2D W = new Point2D(xCoord - 1, yCoord - 1);     // W

            neighbors.put(tileNW, NW);
            neighbors.put(tileNE, NE);
            neighbors.put(tileSE, SE);
            neighbors.put(tileSW, SW);
            neighbors.put(tileN, N);
            neighbors.put(tileE, E);
            neighbors.put(tileS, S);
            neighbors.put(tileW, W);
        }

        return neighbors;
    }

    /**
     * Prüft ausgehend von einem neu hinzugefügten Knoten, ob 2 Verkehrsteile verbunden wurden. Wenn das der Fall ist,
     * fügt es die beiden Verkehrsteile zu einer zusammen.
     *
     * @param newAddedVertices
     */
    private void mergeTrafficPartsIfNecessary(List<Vertex> newAddedVertices, TrafficType trafficType) {

        if (trafficType.equals(TrafficType.AIR)) {
//            mergeTrafficAirPartsIfNecessary(newAddedVertices);
        } else {
            List<Station> nearStations = model.getPathfinder().findAllDirectlyConnectedStations(newAddedVertices.get(0), trafficType);
            Set<ConnectedTrafficPart> differentParts = new HashSet<>();
            for (Station station : nearStations) {
                differentParts.add(station.getTrafficPartForTrafficType(trafficType));
            }
            int numberOfNearTrafficParts = differentParts.size();
//            System.out.println(numberOfNearTrafficParts);
            boolean mergeNecessary = numberOfNearTrafficParts > 1;
            if (mergeNecessary) {
//            System.out.println("newAddedVertices in mergeTrafficPartsIfNeccessary: ");
//            newAddedVertices.forEach((x) -> System.out.println(x.getName()));

                mergeTrafficParts(new ArrayList<>(differentParts));

                if (trafficType.equals(TrafficType.RAIL)) {

                    Set<Railblock> nearRailblocks = new HashSet<>();
                    for (Vertex vertex : newAddedVertices) {

                        Railblock nearRailblock = vertex.getRailblock();
                        if (nearRailblock != null) {
                            nearRailblocks.add(nearRailblock);
                        }
                    }
                    if (nearRailblocks.size() == 2) {
                        ArrayList<Railblock> railblockList = new ArrayList(nearRailblocks);

                        Railblock railblock1 = railblockList.get(0);
                        Railblock railblock2 = railblockList.get(1);

//                        System.out.println("Railblock 1 vertices: ");
//                        railblock1.getVertices().forEach((x) -> System.out.println(x.getName()));
//
//                        System.out.println("Railblock 2 vertices: ");
//                        railblock2.getVertices().forEach((x) -> System.out.println(x.getName()));

                        railblock1.mergeWithRailblock(railblock2);
                        railblock1.addVertices(new HashSet<>(newAddedVertices));

//                        System.out.println("Railblock 1 vertices: ");
//                        railblock1.getVertices().forEach((x) -> System.out.println(x.getName()));

                    } else if (nearRailblocks.size() > 2) {
                        throw new RuntimeException("Unfertiger Code");
                    } else {
                        throw new RuntimeException("nearRailblocks.size() in mergeTrafficPartsIfNeccessary was " + nearRailblocks.size());
                    }
                }
            } else {
                if (numberOfNearTrafficParts == 1 && trafficType.equals(TrafficType.RAIL)) {
//                ConnectedTrafficPart trafficPart = differentParts.iterator().next();
                    Railblock nearRailblock = null;
                    for (Vertex vertex : newAddedVertices) {
                        if (vertex.getRailblock() != null) {
                            nearRailblock = vertex.getRailblock();
                        }
                    }
                    if (nearRailblock != null) {
                        nearRailblock.addVertices(new HashSet<>(newAddedVertices));
                        Set<Vertex> vertices = model.getPathfinder().findAllConnectedVerticesUntilSignal(newAddedVertices.get(0));
//                        System.out.println("connected vertices if not merged: ");
//                        vertices.forEach((x) -> System.out.println(x.getName()));
                    } else throw new RuntimeException("railblock war null");
                }
            }
        }
    }

//    private void mergeTrafficAirPartsIfNecessary(List<Vertex> newAddedVertices) {
//        List<Station> airStations = getAllAirStations();
//
//        Set<ConnectedTrafficPart> differentLines = new HashSet<>();
//        for (Station station : nearStations) {
//            differentLines.add(station.getAirTrafficPart());
//            //TODO Wann wird TrafficPart in Station gesetzt?
//        }
//        int numberOfNearTrafficLines = differentLines.size();
//        System.out.println(numberOfNearTrafficLines);
//        if (numberOfNearTrafficLines > 1) {
//            System.out.println("tried to merge trafficLines");
//            System.out.println("found lines " + numberOfNearTrafficLines);
//
//            //mergeTrafficAirParts(new ArrayList<>(differentLines));
//            //TODO Einkommentieren bzw ändern
//        }
//    }

    public List<Station> getAllAirStations() {
        List<Station> airStations = new ArrayList<>();
        for (Station st : stations) {
            if (st.hasPartOfTrafficType(TrafficType.AIR)) {
                airStations.add(st);
            }
        }
//        System.out.println("AirStations in MapModel :" + airStations);
        return airStations;
    }

    /**
     * Fügt die angegebenen Verkehrsteile zu einem zusammen
     *
     * @param parts
     */
    private void mergeTrafficParts(List<ConnectedTrafficPart> parts) {
        ConnectedTrafficPart firstPart = parts.get(0);
//        System.out.println("firstPart " + firstPart.getStations().size());
        for (int i = 1; i < parts.size(); i++) {
            firstPart.mergeWithTrafficPart(parts.get(i));
            model.getActiveTrafficParts().remove(parts.get(i));
            model.getNewCreatedOrIncompleteTrafficParts().remove(parts.get(i));
        }
//        System.out.println("firstPart after merge " + firstPart.getStations().size());
    }

//    private void mergeTrafficAirParts(List<ConnectedTrafficPart> parts){
//        ConnectedTrafficPart firstPart = parts.get(0);
//        System.out.println("firstPart "+firstPart.getStations().size());
//        for(int i=1; i<parts.size(); i++){
//            firstPart.mergeWithAirTrafficPart(parts.get(i));
//            model.getActiveTrafficParts().remove(parts.get(i));
//            model.getNewCreatedOrIncompleteTrafficParts().remove(parts.get(i));
//        }
//        System.out.println("firstPart after merge "+firstPart.getStations().size());
//    }

    /**
     * Gibt die Station zurück, die direkt neben der Haltestelle in x- oder y-Richtung steht. Wenn keine Station angrenzt,
     * wird null zurückgegeben
     *
     * @param row
     * @param column
     * @param building
     * @return
     */
    private Station getStationNextToStop(int row, int column, Stop building) {
        Station station;
        for (int r = row; r < row + building.getWidth(); r++) {
            Building adjacentBuilding = tileGrid[r][column - 1].getBuilding();
            if (adjacentBuilding instanceof Stop) {
                return ((Stop) adjacentBuilding).getStation();
            }
            adjacentBuilding = tileGrid[r][column + building.getDepth()].getBuilding();
            if (adjacentBuilding instanceof Stop) {
                return ((Stop) adjacentBuilding).getStation();
            }
        }

        for (int c = column; c < column + building.getDepth(); c++) {
            Building adjacentBuilding = tileGrid[row - 1][c].getBuilding();
            if (adjacentBuilding instanceof Stop) {
                return ((Stop) adjacentBuilding).getStation();
            }
            adjacentBuilding = tileGrid[row + building.getWidth()][c].getBuilding();
            if (adjacentBuilding instanceof Stop) {
                return ((Stop) adjacentBuilding).getStation();
            }
        }
        return null;
    }

    //TODO refactoring
    private boolean checkForSecondStation(Building building) {
        Long currentId = ((Stop) building).getStation().getId();

        if (adjacentStationId == -1) {
            adjacentStationId = currentId;
        } else {
            if (adjacentStationId != currentId) return true;
        }
        return false;
    }

    private void removePreviousBuildingOutOfVertices(int xCoordOfTile, int yCoordOfTile) {

        Tile selectedTile = model.getMap().getTileGrid()[xCoordOfTile][yCoordOfTile];
        Building buildingOnSelectedTileBefore = selectedTile.getBuilding();

        //Entferne alle points des buildings, das vorher auf dem tile stand
        if (buildingOnSelectedTileBefore instanceof PartOfTrafficGraph) {
            Set<Vertex> verticesOfBuildingBefore = ((PartOfTrafficGraph) buildingOnSelectedTileBefore).getVertices();
            TrafficGraph graph = model.getMap().getGraphForTrafficType(buildingOnSelectedTileBefore.getTrafficType());
            for (Vertex v : verticesOfBuildingBefore) {
                v.getBuildings().remove(buildingOnSelectedTileBefore);
                if (v.getBuildings().size() == 0) {
                    graph.removeVertex(v.getName());
                } else {
                    for (Vertex v2 : verticesOfBuildingBefore) {
                        graph.removeEdgeBidirectional(v.getName(), v2.getName());
                    }
                }
            }
//            graph.printGraph();
        }
    }


    /**
     * Fügt die Points eines Felds zum Verkehrsgraph hinzu. Points innerhalb eines Tiles sind miteinander
     * durch eine ungerichtete Kante verbunden. Wenn sich Punkte "an derselben Stelle" befinden, werden diese
     * zusammengeführt.
     *
     * @param building     die Instanz des Gebäudes, wessen Punkte hinzugefügt werden sollen
     * @param xCoordOfTile x-Koordinate des Tiles, auf das die Straße platziert wurde
     * @param yCoordOfTile y-Koordinate des Tiles, auf das die Straße platziert wurde
     * @return Eine Liste der Vertices, die zum Graph hinzugefügt wurden
     */
    public List<Vertex> addPointsToGraph(PartOfTrafficGraph building, int xCoordOfTile, int yCoordOfTile) {
        TrafficGraph trafficGraph;
        if (building == null) throw new IllegalArgumentException("building was null");
        if (building.getTrafficType().equals(TrafficType.ROAD)) {
            trafficGraph = this.roadGraph;
        } else {
            if (building.getTrafficType().equals(TrafficType.AIR)) {
                trafficGraph = this.airGraph;
            }

            //Vielleicht sollte man für Flugzeuge eine eigene globale Variable von TrafficGraph erstellen, der die Punkte der
            // Flugverbindungen abspeichert. Dann müssten auch im Pathfinder unterschiedliche Graphen benutzt werden,
            // je nach TrafficType, und die Methode addNewStationToTrafficLineOrCreateNewTrafficLine() mit Sicherheit auch

            else if (building.getTrafficType().equals(TrafficType.RAIL)) {
                trafficGraph = this.railGraph;
            } else {
                throw new RuntimeException("TrafficType in addPointsToGraph() war weder ROAD, AIR noch RAIL");
            }
        }

        boolean isPointPartOfStation = false;

        //Stationen bestehen aus Haltestellen, deswegen ist dann der Punkt Teil einer Station
        if (building instanceof Stop) isPointPartOfStation = true;

        //Speichere die Knoten aus dem Graph vor den Änderungen
        List<Vertex> verticesBefore = new ArrayList<>(trafficGraph.getMapOfVertexes().values());

        Map<String, List<Double>> points = building.getPoints();
        Set<Vertex> joinedVertices = new HashSet<>();


        for (Map.Entry<String, List<Double>> pointNameAndCoords : points.entrySet()) {

            // identifier wird dem Name eines Knotens hinzugefügt, damit der Name unique bleibt,
            // sonst gäbe es Duplikate, da points aus verschiedenen Felder denselben Namen haben könnten
            String identifier = xCoordOfTile + "-" + yCoordOfTile + "-";
            String vertexName = identifier + pointNameAndCoords.getKey();

            double xCoordOfPoint = pointNameAndCoords.getValue().get(0);
            double yCoordOfPoint = pointNameAndCoords.getValue().get(1);


            Vertex vertexOfBuilding = new Vertex(vertexName, xCoordOfPoint, yCoordOfPoint, xCoordOfTile, yCoordOfTile);
            vertexOfBuilding.setPointOfStation(isPointPartOfStation);
            if (isPointPartOfStation) {
                vertexOfBuilding.setStation(((Stop) building).getStation());
            }
            boolean added = trafficGraph.addVertex(vertexOfBuilding);
            if (!added) {
                Vertex v2 = trafficGraph.getMapOfVertexes().get(vertexOfBuilding.getName());
                joinedVertices.add(v2);
            }
            //
            for (Vertex vertexOfGraph : trafficGraph.getMapOfVertexes().values()) {
                //edges gibt die Verbindungen zwischen den points eines buildings an
                List<List<String>> edges = building.getTransportations();
                for (int i = 0; i < edges.size(); i++) {
                    //Knoten, zwischen denen es eine Kante gibt
                    String from = identifier + edges.get(i).get(0);
                    String to = identifier + edges.get(i).get(1);

                    //Wenn es eine Verbindung von dem Knoten des buildings zu einem Knoten des Graphs gibt (oder umgekehrt)
                    //dann füge eine ungerichtete Kante hinzu
                    if ((vertexOfBuilding.getName().equals(from) && vertexOfGraph.getName().equals(to)) ||
                            (vertexOfBuilding.getName().equals(to) && vertexOfGraph.getName().equals(from)))

                        trafficGraph.addEdgeBidirectional(vertexOfGraph.getName(), vertexOfBuilding.getName());
                }
            }
        }

        //checkForDuplicatePoints entfernt Punkte, die durch das hinzufügen doppelt geworden sind.
        //Die joinedVertices sind die Punkte, die zusammengfügt wurden und noch im Graph sind
        joinedVertices.addAll(trafficGraph.checkForDuplicatePoints());
//        System.out.println("joinedVertices");
//        joinedVertices.forEach((x) -> System.out.println(x.getName()));

//        trafficGraph.printGraph();

        //Die Knoten im Graph nach den Änderungen
        List<Vertex> verticesAfter = new ArrayList<>(trafficGraph.getMapOfVertexes().values());

        verticesAfter.removeAll(verticesBefore);
        List<Vertex> verticesOfBuilding = verticesAfter;

        List<Vertex> addedVertices = verticesOfBuilding;

//        System.out.println("addedVertices");
//        addedVertices.forEach((x) -> System.out.println(x.getName()));

        //Es wurden Points zusammengeführt, die gehören aber trotzdem zum building
        for (Vertex j : joinedVertices) {
            if (!addedVertices.contains(j)) {
                addedVertices.add(j);
            }
        }
//        System.out.println("addedVertices after joined");
//        addedVertices.forEach((x) -> System.out.println(x.getName()));

        building.getVertices().addAll(addedVertices);
        for (Vertex addedV : addedVertices) {
            addedV.getBuildings().add(building);
        }
        return addedVertices;
    }


    /**
     * Gibt Vertexes zurück, die zu dem mitgegebenen Building gehören und bereits im Graph eingetragen sind
     *
     * @param building
     * @param xCoordOfTile
     * @param yCoordOfTile
     * @return
     */
    public List<Vertex> getVerticesOnTile(PartOfTrafficGraph building, int xCoordOfTile, int yCoordOfTile) {
        TrafficGraph trafficGraph;
        if (building.getTrafficType().equals(TrafficType.ROAD)) {
            trafficGraph = this.roadGraph;
        } else if (building.getTrafficType().equals(TrafficType.RAIL)) {
            trafficGraph = this.railGraph;
        } else {
            return new ArrayList<>();
            //TODO rails und air
        }
        List<Vertex> verticesOnTile = new ArrayList<>();

        Map<String, List<Double>> points = building.getPoints();
        for (Map.Entry<String, List<Double>> entry : points.entrySet()) {

            String identifier = xCoordOfTile + "-" + yCoordOfTile + "-";
            String vertexName = identifier + entry.getKey();

            for (Vertex v : trafficGraph.getMapOfVertexes().values()) {
                if (v.getName().equals(vertexName)) {
                    verticesOnTile.add(v);
//                    System.out.println("name of connected vertex: " + v.getName());
                }
            }
        }

        return verticesOnTile;
    }


    public void removePointsOnTile(Building buildingOnSelectedTile) {
        PartOfTrafficGraph partOfGraph = (PartOfTrafficGraph) buildingOnSelectedTile;
        TrafficGraph graph = model.getMap().getGraphForTrafficType(partOfGraph.getTrafficType());

        Set<Vertex> addedVertices = ((PartOfTrafficGraph) buildingOnSelectedTile).getVertices();

//        System.out.println("Vertices of building in removePointsOnTile");
//        addedVertices.forEach((x) -> System.out.println(x.getName()));

        for (Vertex vertex : addedVertices) {
            vertex.getBuildings().remove(buildingOnSelectedTile);

            if (vertex.getBuildings().size() == 0) {
                graph.removeVertex(vertex.getName());
            } else {
//                System.out.println("Buildings for Vertex");
//                vertex.getBuildings().forEach((x) -> System.out.println(x.getBuildingName()));
            }
        }
//
//        List<Vertex> addedVertices = model.getMap().getVerticesOnTile(partOfGraph, xCoord, yCoord);
//
//
//
//        for (Vertex v : addedVertices) {
//            boolean isOnBorder = true;
//            if(v.getxCoordinateRelativeToTileOrigin() > 0 && v.getxCoordinateRelativeToTileOrigin() < 1
//                    && v.getyCoordinateRelativeToTileOrigin() > 0 && v.getyCoordinateRelativeToTileOrigin() < 1){
//                isOnBorder = false;
//            }
//            if (isOnBorder == false) {
//                graph.removeVertex(v.getName());
//            }
//        }
//
//        Map<String, Vertex> vertexesInGraph = graph.getMapOfVertexes();
//        Iterator<Map.Entry<String, Vertex>> iterator = vertexesInGraph.entrySet().iterator();
//        while (iterator.hasNext()) {
//            Map.Entry<String, Vertex> vertex = iterator.next();
//            List<Vertex> connections = graph.getAdjacencyMap().get(vertex.getKey());
//            if (connections.size() == 0) {
//                iterator.remove();
//            }
//        }
    }


    /**
     * Wenn keine andere Station im Graphen findbar, fügt es dem Graphen einen neuen Verkehrsteil hinzu. Wenn eine
     * andere Station findbar, wird der Verkehrsteil der gefundenen Station der neuen Station hinzugefügt
     * Der Graph ist abhängig vom angegebenen trafficType.
     *
     * @param newStation
     * @param trafficType
     * @return
     */
    private ConnectedTrafficPart addNewStationToTrafficPartOrCreateNewTrafficPart(Station newStation, TrafficType trafficType) {
//        System.out.println("addNewStationToTrafficPartOrCreateNewTrafficPart called");
        List<Vertex> pathToStation = null;
        boolean anotherStationFindable = false;

        if (trafficType == TrafficType.AIR) {
            anotherStationFindable = getAllAirStations().size() > 0;
        } else {
            pathToStation = model.getPathfinder().findPathToNextStation(newStation, trafficType);
            if (pathToStation.size() > 0) anotherStationFindable = true;
        }


        if (anotherStationFindable) {

            if (trafficType.equals(TrafficType.ROAD)) {
                Vertex lastVertex = pathToStation.get(pathToStation.size() - 1);
                Station nextStation = lastVertex.getStation();
//                System.out.println("should call addStationAndUpdateConnectedStations with " + newStation.getId());
                nextStation.getRoadTrafficPart().addStationAndUpdateConnectedStations(newStation);
                newStation.setRoadTrafficPart(nextStation.getRoadTrafficPart());
                return nextStation.getRoadTrafficPart();
            } else if (trafficType.equals(TrafficType.AIR)) {

                ConnectedTrafficPart trafficPart = getAllAirStations().get(0).getAirTrafficPart();
//                System.out.println("should call addStationAndUpdateConnectedStations with " + newStation.getId());
                trafficPart.addStationAndUpdateConnectedStations(newStation);
                newStation.setAirTrafficPart(trafficPart);
                return trafficPart;

//                nextStation.setAirTrafficPart(trafficPart);
//                nextStation.getAirTrafficPart().addStationAndUpdateConnectedStations(newStation);
//                // TODO: hack:  erste Station löschen
//                model.getNewCreatedOrIncompleteTrafficParts().remove();
//                model.getNewCreatedOrIncompleteTrafficParts().add(nextStation.getAirTrafficPart());

//                return nextStation.getAirTrafficPart();
            } else if (trafficType.equals(TrafficType.RAIL)) {
                Vertex lastVertex = pathToStation.get(pathToStation.size() - 1);
                Station nextStation = lastVertex.getStation();
//                System.out.println("should call addStationAndUpdateConnectedStations with " + newStation.getId());
                nextStation.getRailTrafficPart().addStationAndUpdateConnectedStations(newStation);
                newStation.setRailTrafficPart(nextStation.getRailTrafficPart());

                Vertex oneVertexOfRailblock = newStation.getSomeVertexForTrafficType(TrafficType.RAIL);
                Set<Vertex> verticesOfRailblock = model.getPathfinder().findAllConnectedVerticesUntilSignal(oneVertexOfRailblock);
                Railblock railblock = new Railblock();
                railblock.addVertices(verticesOfRailblock);

//                System.out.println("Vertices of new Railblock when added Station to existing ConnectedTrafficPart");
//                railblock.getVertices().forEach((x) -> System.out.println(x.getName()));

                return nextStation.getRailTrafficPart();
            } else
                throw new IllegalArgumentException("traffictype in addNewStationToTrafficLineOrCreateNewTrafficLine was " + trafficType);
        } else {
            ConnectedTrafficPart connectedTrafficPart = null;
            switch (trafficType) {
                case AIR:
                    connectedTrafficPart = new ConnectedTrafficPart(model, TrafficType.AIR, newStation);
                    newStation.setAirTrafficPart(connectedTrafficPart);
                    break;
                case RAIL:
                    connectedTrafficPart = new ConnectedTrafficPart(model, TrafficType.RAIL, newStation);
                    newStation.setRailTrafficPart(connectedTrafficPart);
                    break;
                case ROAD:
                    connectedTrafficPart = new ConnectedTrafficPart(model, TrafficType.ROAD, newStation);
                    newStation.setRoadTrafficPart(connectedTrafficPart);
                    break;
                default:
                    break;
            }
            // TODO desiredNumber

            model.getNewCreatedOrIncompleteTrafficParts().add(connectedTrafficPart);
            return connectedTrafficPart;
        }
    }


    /**
     * Verändert die Höhe des Bodens ausgehend von den Koordinaten des angeklickten Tiles
     *
     * @param xCoord
     * @param yCoord
     * @param heightShift Wert, um den die Höhe verändert werden soll (kann +1 oder -1 sein)
     */
    public void changeGroundHeight(int xCoord, int yCoord, int heightShift) {

        // Tiles an der angeklickten Stelle
        Tile tileN = tileGrid[xCoord][yCoord];
        Tile tileW = tileGrid[xCoord][yCoord - 1];
        Tile tileS = tileGrid[xCoord + 1][yCoord - 1];
        Tile tileE = tileGrid[xCoord + 1][yCoord];

        List<Tile> tilesToBeUpdated = new ArrayList<>();
        tilesToBeUpdated.addAll(Arrays.asList(tileN, tileW, tileS, tileE));

        // Höhe an der angeklickten Stelle vor der Bearbeitung
        int startHeight = tileN.getCornerHeights().get("cornerS");

        if (canChangeHeight(xCoord, yCoord, startHeight, heightShift)) {

            // ändere die Höhen der Tiles, die sich direkt um die angeklickte stelle herum befinden
            updateFirstLevelHeights(xCoord, yCoord, heightShift);

            // prüfe, ob durch geänderte Höhen Anpassungen an den vier Start-Tiles nötig sind und passe ggf. an
            if (!heightConstraintsMetForAllTiles(tilesToBeUpdated)) {
                for (Tile tile : tilesToBeUpdated) {
                    updateHeightIfNecessary(tile);
                }
            }

            // Map, die ein Tile auf seine isometrischen Koordinaten im Spielfeld abbildet
            Map<Tile, Point2D> tileToPositionInGrid = new LinkedHashMap<>();
            tileToPositionInGrid.put(tileN, new Point2D(xCoord, yCoord));
            tileToPositionInGrid.put(tileW, new Point2D(xCoord, yCoord - 1));
            tileToPositionInGrid.put(tileS, new Point2D(xCoord + 1, yCoord - 1));
            tileToPositionInGrid.put(tileE, new Point2D(xCoord + 1, yCoord));

            if (heightShift > 0) {
                // solange der Wert von startHeight > 0 ist, müssen die Nachbarn der veränderten Tiles ebenfalls geprüft werden
                while (startHeight >= 0) {
                    checkNeighbors(tileToPositionInGrid);
                    startHeight--;
                }
            } else {
                while (startHeight > 0) {
                    checkNeighbors(tileToPositionInGrid);
                    startHeight--;
                }
            }
        }
    }


    /**
     * Prüft den Bereich der Map ab, der durch die Höhenverschiebung geändert werden würde und gibt false zurück,
     * falls eine Fabrik oder ein Element eines Verkehrsnetzes im Weg ist.
     *
     * @param xCoord
     * @param yCoord
     * @param startHeight
     * @return
     */
    private boolean canChangeHeight(int xCoord, int yCoord, int startHeight, int heightShift) {

        if (heightShift >= 0) {

            startHeight += 1;
            for (int row = xCoord - startHeight - 1; row <= xCoord + startHeight + 1; row++) {
                for (int col = yCoord - startHeight - 1; col <= yCoord + startHeight + 1; col++) {
                    if(row >= depth || col >= width || row < 0 || col < 0) return false;
                    if (!(tileGrid[row][col].isWater() || tileGrid[row][col].getBuilding() instanceof Nature
                            || tileGrid[row][col].getBuilding().getBuildingName().equals("ground")
                            || tileGrid[row][col].getBuilding().getBuildingName().equals("grass"))) {
                        return false;
                    }
                }
            }
        } else {

            startHeight -= 1;
            for (int row = xCoord + startHeight; row <= xCoord - startHeight; row++) {
                for (int col = yCoord + startHeight; col <= yCoord - startHeight; col++) {
                    if (!(tileGrid[row][col].isWater() || tileGrid[row][col].getBuilding() instanceof Nature
                            || tileGrid[row][col].getBuilding().getBuildingName().equals("ground")
                            || tileGrid[row][col].getBuilding().getBuildingName().equals("grass"))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    /**
     * Ändert die Höhen der Tiles, die sich um die angegebenen Koordinaten befinden um einen angegebenen Wert
     *
     * @param xCoord
     * @param yCoord
     * @param heightShift Höhe, um die die angegebene Stelle geändert werden soll
     */
    public void updateFirstLevelHeights(int xCoord, int yCoord, int heightShift) {

        //tiles um die angeklickte stelle
        Tile tileN = tileGrid[xCoord][yCoord];
        Tile tileW = tileGrid[xCoord][yCoord - 1];
        Tile tileS = tileGrid[xCoord + 1][yCoord - 1];
        Tile tileE = tileGrid[xCoord + 1][yCoord];


        Map<String, Integer> updatedHeights;
        Building ground = new Building(1, 1, "ground");
        String absoluteTileHeight;
        boolean isWater;


        updatedHeights = tileS.updateCornerHeight("cornerN", heightShift);
        absoluteTileHeight = tileS.absoluteHeigtToRelativeHeight(updatedHeights);
        isWater = absoluteTileHeight.equals("0000") && updatedHeights.get("cornerS") < 0;
        tileGrid[xCoord + 1][yCoord - 1] = new Tile(ground, updatedHeights, isWater);

        updatedHeights = tileW.updateCornerHeight("cornerE", heightShift);
        absoluteTileHeight = tileW.absoluteHeigtToRelativeHeight(updatedHeights);
        isWater = absoluteTileHeight.equals("0000") && updatedHeights.get("cornerS") < 0;
        tileGrid[xCoord][yCoord - 1] = new Tile(ground, updatedHeights, isWater);

        updatedHeights = tileN.updateCornerHeight("cornerS", heightShift);
        absoluteTileHeight = tileN.absoluteHeigtToRelativeHeight(updatedHeights);
        isWater = absoluteTileHeight.equals("0000") && updatedHeights.get("cornerS") < 0;
        tileGrid[xCoord][yCoord] = new Tile(ground, updatedHeights, isWater);

        updatedHeights = tileE.updateCornerHeight("cornerW", heightShift);
        absoluteTileHeight = tileE.absoluteHeigtToRelativeHeight(updatedHeights);
        isWater = absoluteTileHeight.equals("0000") && updatedHeights.get("cornerS") < 0;
        tileGrid[xCoord + 1][yCoord] = new Tile(ground, updatedHeights, isWater);
    }


    /**
     * Prüft für ein gegebenes Tile, ob die Höhen-Constraints eingehalten werden und ändert deren werte, falls sie
     * nicht eingehalten werden
     *
     * @param tile
     */
    private void updateHeightIfNecessary(Tile tile) {
        // Reihenfolge der corners in getCornerHeights: N-E-S-W

        List<Integer> corners = new ArrayList<>();
        for (Integer corner : tile.getCornerHeights().values()) {
            corners.add(corner);
        }

        if (!heightConstraintsMetInTile(tile)) {

            int maxCorner = tile.findMaxCorner(tile.getCornerHeights());
            int indexOfMaxCorner = corners.indexOf(maxCorner);

            // Wenn sich die höchste Ecke cornerN ist
            if (indexOfMaxCorner == 0) {
                updateHeightsForTile(tile, "cornerW", 3, "cornerE", 1, "cornerS", 2);

                // Wenn sich die höchste Ecke cornerE ist
            } else if (indexOfMaxCorner == 1) {
                updateHeightsForTile(tile, "cornerN", 0, "cornerS", 2, "cornerW", 3);

                // Wenn sich die höchste Ecke cornerS ist
            } else if (indexOfMaxCorner == 2) {
                updateHeightsForTile(tile, "cornerE", 1, "cornerW", 3, "cornerN", 0);

                // Wenn sich die höchste Ecke cornerW ist
            } else {
                updateHeightsForTile(tile, "cornerS", 2, "cornerN", 0, "cornerE", 1);
            }
        }
    }


    /**
     * Ändert die Höhen an den Ecken des Tiles, so dass die Höhen-Constraints wieder eingehalten werden.
     * Reihenfolge der Ecken: N-E-S-W
     *
     * @param tile          Tile, dessen Höhen angepasst werden müssen
     * @param prev          Name der Vorgänger-Ecke
     * @param indexPrev     Index der Vorgänger Ecke
     * @param next          Name der Nachfolger-Ecke
     * @param indexNext     Index der Nachfolger Ecke
     * @param opposite      Name der gegenüberliegenden Ecke
     * @param indexOpposite Index der gegenüberliegenden Ecke
     */
    public void updateHeightsForTile(Tile tile, String prev, int indexPrev, String next, int indexNext, String opposite, int indexOpposite) {

        List<Integer> corners = new ArrayList<>();
        for (Integer corner : tile.getCornerHeights().values()) {
            corners.add(corner);
        }

        int maxCorner = tile.findMaxCorner(tile.getCornerHeights());

        int heightDiffToPrevious = maxCorner - corners.get(indexPrev);
        if (Math.abs(heightDiffToPrevious) > 1) {
            int newHeight = (heightDiffToPrevious - 1);
            tile.updateCornerHeight(prev, newHeight);
        }
        int heightDiffToNext = maxCorner - corners.get(indexNext);
        if (Math.abs(heightDiffToNext) > 1) {
            int newHeight = (heightDiffToNext - 1);
            tile.updateCornerHeight(next, newHeight);
        }
        int heightDiffToOpposite = maxCorner - corners.get(indexOpposite);
        if (Math.abs(heightDiffToOpposite) > 2) {
            int newHeight = (heightDiffToOpposite - 2);
            tile.updateCornerHeight(opposite, newHeight);
        }
    }


    /**
     * Überprüft für die Nachbarn jedes Felds in der mitgegebenen Map, ob die Höhen-Constraints eingehalten werden
     * und ändert ggf. deren Werte
     *
     * @param tileToPositionInGrid
     */
    public void checkNeighbors(Map<Tile, Point2D> tileToPositionInGrid) {
        Map<Tile, Point2D> tileToPositionInGridNeighbors = new LinkedHashMap<>();

        // Prüfe benachbarte Felder für jedes der zuvor veränderten Felder
        for (Tile tile : tileToPositionInGrid.keySet()) {
            Map<String, Tile> neighbors = getNeighbors(tileToPositionInGrid.get(tile));
            for (Map.Entry<String, Tile> neighbor : neighbors.entrySet()) {

                Point2D coordsOfNeighbor = checkNeighborHeight(tile, neighbor, tileToPositionInGrid.get(tile));

                // Füge noch nicht geprüfte Nachbarn hinzu, damit diese im nächsten Schleifendurchlauf in
                // changeGroundHeight ebenfalls berücksichtigt werden
                if (!(tileToPositionInGrid.containsValue(coordsOfNeighbor)) &&
                        !(tileToPositionInGridNeighbors.containsValue(coordsOfNeighbor))) {
                    tileToPositionInGridNeighbors.put(neighbor.getValue(), coordsOfNeighbor);
                }
            }
        }
//        tileToPositionInGrid.clear();
        tileToPositionInGrid.putAll(tileToPositionInGridNeighbors);
    }


    /**
     * Prüft für Nachbarfeld eines Tiles ob Höhen zueinander passen
     *
     * @param tile     Tile dessen Nachbarn geprüft werden sollen
     * @param neighbor Eines der Nachbarfelder des Tiles
     * @param point    Koordinaten des Tiles dessen Nachbar geprüft wird
     * @return Koordinaten des geprüften Nachbarfelds
     */
    public Point2D checkNeighborHeight(Tile tile, Map.Entry<String, Tile> neighbor, Point2D point) {

        Tile[][] grid = model.getMap().getTileGrid();
        Building ground = new Building(1, 1, "ground");

        int xCoord = (int) point.getX();
        int yCoord = (int) point.getY();

        if (neighbor.getKey().equals("tileNW")) {
            if (!(neighbor.getValue().getCornerHeights().get("cornerS").equals(tile.getCornerHeights().get("cornerW")))) {
                neighbor.getValue().setHeightForCorner("cornerS", tile.getCornerHeights().get("cornerW"));
            }
            if (!(neighbor.getValue().getCornerHeights().get("cornerE").equals(tile.getCornerHeights().get("cornerN")))) {
                neighbor.getValue().setHeightForCorner("cornerE", tile.getCornerHeights().get("cornerN"));
            }
            xCoord = xCoord - 1;


        } else if (neighbor.getKey().equals("tileNE")) {
            if (!(neighbor.getValue().getCornerHeights().get("cornerS").equals(tile.getCornerHeights().get("cornerE")))) {
                neighbor.getValue().setHeightForCorner("cornerS", tile.getCornerHeights().get("cornerE"));
            }
            if (!(neighbor.getValue().getCornerHeights().get("cornerW").equals(tile.getCornerHeights().get("cornerN")))) {
                neighbor.getValue().setHeightForCorner("cornerW", tile.getCornerHeights().get("cornerN"));
            }
            yCoord = yCoord + 1;


        } else if (neighbor.getKey().equals("tileSE")) {
            if (!(neighbor.getValue().getCornerHeights().get("cornerN").equals(tile.getCornerHeights().get("cornerE")))) {
                neighbor.getValue().setHeightForCorner("cornerN", tile.getCornerHeights().get("cornerE"));
            }
            if (!(neighbor.getValue().getCornerHeights().get("cornerW").equals(tile.getCornerHeights().get("cornerS")))) {
                neighbor.getValue().setHeightForCorner("cornerW", tile.getCornerHeights().get("cornerS"));
            }
            xCoord = xCoord + 1;


        } else {
            if (!(neighbor.getValue().getCornerHeights().get("cornerN").equals(tile.getCornerHeights().get("cornerW")))) {
                neighbor.getValue().setHeightForCorner("cornerN", tile.getCornerHeights().get("cornerW"));
            }
            if (!(neighbor.getValue().getCornerHeights().get("cornerE").equals(tile.getCornerHeights().get("cornerS")))) {
                neighbor.getValue().setHeightForCorner("cornerE", tile.getCornerHeights().get("cornerS"));
            }
            yCoord = yCoord - 1;

        }

        if (heightConstraintsMetInTile(neighbor.getValue())) {
            grid[xCoord][yCoord] = new Tile(ground, neighbor.getValue().getCornerHeights(), false);
        } else {
            updateHeightIfNecessary(neighbor.getValue());
        }

        return new Point2D(xCoord, yCoord);
    }


    /**
     * Sucht die Nachbarn der Feldes mit den übergebenen Koordinaten und liefert diese als Map zurück
     *
     * @param coords Koorinaten des Feldes, dessen Nachbarn gesucht werden sollen
     * @return eine Map, die die Position des Nachbarn im Verhältnis zum Ursprungsteil auf das NachbarTile abbildet
     */
    private Map<String, Tile> getNeighbors(Point2D coords) {
        int xCoord = (int) coords.getX();
        int yCoord = (int) coords.getY();

        Tile[][] grid = model.getMap().getTileGrid();
        Tile tileNW = grid[xCoord - 1][yCoord];     // NW
        Tile tileNE = grid[xCoord][yCoord + 1];     // NE
        Tile tileSE = grid[xCoord + 1][yCoord];     // SE
        Tile tileSW = grid[xCoord][yCoord - 1];     // SW

        Map<String, Tile> neighbors = new LinkedHashMap<>();
        neighbors.put("tileNW", tileNW);
        neighbors.put("tileNE", tileNE);
        neighbors.put("tileSE", tileSE);
        neighbors.put("tileSW", tileSW);

        return neighbors;
    }


    /**
     * Prüft ob für alle in der mitgegebenen Liste von Tiles die Höhen-Constraints eingehalten werden
     *
     * @param tilesToBeUpdated
     * @return
     */
    private boolean heightConstraintsMetForAllTiles(List<Tile> tilesToBeUpdated) {
        List<String> validHeights = new ArrayList<>();
        validHeights.addAll(Arrays.asList("0100", "1101", "0101", "0000", "1000", "1100", "1010", "1001", "1011",
                "0010", "0110", "1110", "1210", "2101", "0121", "1012", "0011", "0001", "0111"));

        for (Tile tile : tilesToBeUpdated) {
            String nameOfAssociatedImage = tile.absoluteHeigtToRelativeHeight(tile.getCornerHeights());
            if (!validHeights.contains(nameOfAssociatedImage)) {
                return false;
            }
        }
        return true;
    }


    /**
     * Prüft ob die Höhen-Constraints im angegebenen Tile eingehalten werden
     *
     * @param tile
     * @return
     */
    private boolean heightConstraintsMetInTile(Tile tile) {
        List<String> validHeights = new ArrayList<>();
        validHeights.addAll(Arrays.asList("0100", "1101", "0101", "0000", "1000", "1100", "1010", "1001", "1011",
                "0010", "0110", "1110", "1210", "2101", "0121", "1012", "0011", "0001", "0111"));

        String nameOfAssociatedImage = tile.absoluteHeigtToRelativeHeight(tile.getCornerHeights());

        return validHeights.contains(nameOfAssociatedImage);

    }

    public TrafficGraph getGraphForTrafficType(TrafficType trafficType) {
        TrafficGraph graph;
        if (trafficType.equals(TrafficType.ROAD)) {
            graph = roadGraph;
        } else if (trafficType.equals(TrafficType.RAIL)) {
            graph = railGraph;
        } else if (trafficType.equals(TrafficType.AIR)) {
            graph = airGraph;
        } else {
            throw new IllegalArgumentException("Kein Graph vorhanden für Typ " + trafficType);
        }
        return graph;
    }


    public String getMapgen() {
        return mapgen;
    }

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

    public TrafficGraph getRoadGraph() {
        return roadGraph;
    }

    public TrafficGraph getAirGraph() {
        return airGraph;
    }

    public void setRoadGraph(TrafficGraph roadGraph) {
        this.roadGraph = roadGraph;
    }

    public TrafficGraph getRailGraph() {
        return railGraph;
    }

    public void setRailGraph(TrafficGraph railGraph) {
        this.railGraph = railGraph;
    }

    public TrafficLineGraph getTrafficLineGraph() {
        return trafficLineGraph;
    }
}

