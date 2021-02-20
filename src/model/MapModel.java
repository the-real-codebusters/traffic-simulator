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
//        System.out.println("place building "+instance.getBuildingName()+" with TrafficType "+instance.getTrafficType());
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

                // Fabriken werden beim erzeugen der Map nur auf komplett ebene Flächen platziert
                if (building instanceof Factory && tile.getBuilding() != null){
                    if(! ((tile.getBuilding() instanceof Nature) ||
                            tile.getBuilding().getBuildingName().equals("grass") ||
                            tile.getBuilding().getBuildingName().equals("0000") && tile.getCornerHeights().get("cornerS") > 0
                    )) return false;
                }


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
                            tile.getBuilding().getBuildingName().equals("grass") ||
                            tile.getBuilding().getBuildingName().equals("ground")
                    )) return false;
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

            //mergeTrafficAirParts(new ArrayList<>(differentLines));
            //TODO Einkommentieren bzw ändern
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

                    Vertex v = new Vertex(vertexName, xCoordOfPoint, yCoordOfPoint, xCoordOfTile, yCoordOfTile, entry.getKey());
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



    /**
     * Verändert die Höhe des Bodens ausgehend von den Koordinaten des angeklickten Tiles
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

            if(heightShift > 0){
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
     * @param xCoord
     * @param yCoord
     * @param startHeight
     * @return
     */
    private boolean canChangeHeight(int xCoord, int yCoord, int startHeight, int heightShift){

        if(heightShift >= 0) {

            startHeight += 1;
            for (int row = xCoord - startHeight-1; row <= xCoord + startHeight+1; row++) {
                for (int col = yCoord - startHeight-1; col <= yCoord + startHeight+1; col++) {
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
     * @param xCoord
     * @param yCoord
     * @param heightShift Höhe, um die die angegebene Stelle geändert werden soll
     */
    public void updateFirstLevelHeights(int xCoord, int yCoord, int heightShift){

        //tiles um die angeklickte stelle
        Tile tileN = tileGrid[xCoord][yCoord];
        Tile tileW = tileGrid[xCoord][yCoord-1];
        Tile tileS = tileGrid[xCoord+1][yCoord-1];
        Tile tileE = tileGrid[xCoord+1][yCoord];


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
     * @param tile
     */
    private void updateHeightIfNecessary(Tile tile){
        // Reihenfolge der corners in getCornerHeights: N-E-S-W

        List<Integer> corners = new ArrayList<>();
        for (Integer corner : tile.getCornerHeights().values()){
            corners.add(corner);
        }

        if (!heightConstraintsMetInTile(tile)) {

            int maxCorner = tile.findMaxCorner(tile.getCornerHeights());
            int indexOfMaxCorner = corners.indexOf(maxCorner);

            // Wenn sich die höchste Ecke cornerN ist
            if (indexOfMaxCorner == 0){
                updateHeightsForTile(tile, "cornerW", 3, "cornerE", 1, "cornerS", 2);

                // Wenn sich die höchste Ecke cornerE ist
            } else if (indexOfMaxCorner == 1) {
                updateHeightsForTile(tile, "cornerN", 0, "cornerS", 2, "cornerW", 3);

                // Wenn sich die höchste Ecke cornerS ist
            } else if (indexOfMaxCorner == 2){
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
     * @param tile Tile, dessen Höhen angepasst werden müssen
     * @param prev Name der Vorgänger-Ecke
     * @param indexPrev Index der Vorgänger Ecke
     * @param next Name der Nachfolger-Ecke
     * @param indexNext Index der Nachfolger Ecke
     * @param opposite Name der gegenüberliegenden Ecke
     * @param indexOpposite Index der gegenüberliegenden Ecke
     */
    public void updateHeightsForTile(Tile tile, String prev, int indexPrev, String next, int indexNext, String opposite, int indexOpposite){

        List<Integer> corners = new ArrayList<>();
        for (Integer corner : tile.getCornerHeights().values()){
            corners.add(corner);
        }

        int maxCorner = tile.findMaxCorner(tile.getCornerHeights());

        int heightDiffToPrevious = maxCorner - corners.get(indexPrev);
        if (Math.abs(heightDiffToPrevious) > 1) {
            int newHeight =  (heightDiffToPrevious -1);
            tile.updateCornerHeight(prev, newHeight);
        }
        int heightDiffToNext = maxCorner - corners.get(indexNext);
        if (Math.abs(heightDiffToNext) > 1) {
            int newHeight =  (heightDiffToNext -1);
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
     * @param tileToPositionInGrid
     */
    public void checkNeighbors(Map<Tile, Point2D> tileToPositionInGrid){
        Map <Tile, Point2D> tileToPositionInGridNeighbors = new LinkedHashMap<>();

        // Prüfe benachbarte Felder für jedes der zuvor veränderten Felder
        for(Tile tile : tileToPositionInGrid.keySet()) {
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
     * @param tile Tile dessen Nachbarn geprüft werden sollen
     * @param neighbor Eines der Nachbarfelder des Tiles
     * @param point Koordinaten des Tiles dessen Nachbar geprüft wird
     * @return Koordinaten des geprüften Nachbarfelds
     */
    public Point2D checkNeighborHeight(Tile tile, Map.Entry<String, Tile> neighbor, Point2D point) {

        Tile[][] grid = model.getMap().getTileGrid();
        Building ground = new Building(1, 1, "ground");

        int xCoord = (int) point.getX();
        int yCoord = (int) point.getY();

        if (neighbor.getKey().equals("tileNW")) {
            if (!(neighbor.getValue().getCornerHeights().get("cornerS").equals(tile.getCornerHeights().get("cornerW")))) {
                neighbor.getValue().setHeightForCorner("cornerS",tile.getCornerHeights().get("cornerW"));
            }
            if (!(neighbor.getValue().getCornerHeights().get("cornerE").equals(tile.getCornerHeights().get("cornerN")))) {
                neighbor.getValue().setHeightForCorner("cornerE",tile.getCornerHeights().get("cornerN"));
            }
            xCoord = xCoord -1;


        } else if (neighbor.getKey().equals("tileNE")) {
            if (!(neighbor.getValue().getCornerHeights().get("cornerS").equals(tile.getCornerHeights().get("cornerE")))) {
                neighbor.getValue().setHeightForCorner("cornerS",tile.getCornerHeights().get("cornerE"));
            }
            if (!(neighbor.getValue().getCornerHeights().get("cornerW").equals(tile.getCornerHeights().get("cornerN")))) {
                neighbor.getValue().setHeightForCorner("cornerW",tile.getCornerHeights().get("cornerN"));
            }
            yCoord = yCoord+1;


        } else if (neighbor.getKey().equals("tileSE")) {
            if (!(neighbor.getValue().getCornerHeights().get("cornerN").equals(tile.getCornerHeights().get("cornerE")))) {
                neighbor.getValue().setHeightForCorner("cornerN",tile.getCornerHeights().get("cornerE"));
            }
            if (!(neighbor.getValue().getCornerHeights().get("cornerW").equals(tile.getCornerHeights().get("cornerS")))) {
                neighbor.getValue().setHeightForCorner("cornerW",tile.getCornerHeights().get("cornerS"));
            }
            xCoord = xCoord+1;


        } else {
            if (!(neighbor.getValue().getCornerHeights().get("cornerN").equals(tile.getCornerHeights().get("cornerW")))) {
                neighbor.getValue().setHeightForCorner("cornerN",tile.getCornerHeights().get("cornerW"));
            }
            if (!(neighbor.getValue().getCornerHeights().get("cornerE").equals(tile.getCornerHeights().get("cornerS")))) {
                neighbor.getValue().setHeightForCorner("cornerE",tile.getCornerHeights().get("cornerS"));
            }
            yCoord = yCoord-1;

        }

        if (heightConstraintsMetInTile(neighbor.getValue())){
            grid[xCoord][yCoord] = new Tile(ground, neighbor.getValue().getCornerHeights(), false);
        }
        else {
            updateHeightIfNecessary(neighbor.getValue());
        }

        return new Point2D(xCoord, yCoord);
    }


    /**
     * Sucht die Nachbarn der Feldes mit den übergebenen Koordinaten und liefert diese als Map zurück
     * @param coords Koorinaten des Feldes, dessen Nachbarn gesucht werden sollen
     * @return eine Map, die die Position des Nachbarn im Verhältnis zum Ursprungsteil auf das NachbarTile abbildet
     */
    private Map <String, Tile> getNeighbors(Point2D coords){
        int xCoord = (int) coords.getX();
        int yCoord = (int) coords.getY();

        Tile[][] grid = model.getMap().getTileGrid();
        Tile tileNW = grid[xCoord-1][yCoord];     // NW
        Tile tileNE = grid[xCoord][yCoord+1];     // NE
        Tile tileSE = grid[xCoord+1][yCoord];     // SE
        Tile tileSW = grid[xCoord][yCoord-1];     // SW

        Map <String, Tile> neighbors = new LinkedHashMap<>();
        neighbors.put("tileNW", tileNW);
        neighbors.put("tileNE", tileNE);
        neighbors.put("tileSE", tileSE);
        neighbors.put("tileSW", tileSW);

        return neighbors;
    }


    /**
     * Prüft ob für alle in der mitgegebenen Liste von Tiles die Höhen-Constraints eingehalten werden
     * @param tilesToBeUpdated
     * @return
     */
    private boolean heightConstraintsMetForAllTiles(List<Tile> tilesToBeUpdated){
        List<String> validHeights = new ArrayList<>();
        validHeights.addAll(Arrays.asList("0100", "1101", "0101", "0000", "1000", "1100", "1010", "1001", "1011",
                "0010", "0110", "1110", "1210", "2101", "0121", "1012", "0011", "0001", "0111"));

        for (Tile tile : tilesToBeUpdated){
            String nameOfAssociatedImage = tile.absoluteHeigtToRelativeHeight(tile.getCornerHeights());
            if (!validHeights.contains(nameOfAssociatedImage)){
                return false;
            }
        }
        return true;
    }


    /**
     * Prüft ob die Höhen-Constraints im angegebenen Tile eingehalten werden
     * @param tile
     * @return
     */
    private boolean heightConstraintsMetInTile(Tile tile){
        List<String> validHeights = new ArrayList<>();
        validHeights.addAll(Arrays.asList("0100", "1101", "0101", "0000", "1000", "1100", "1010", "1001", "1011",
                "0010", "0110", "1110", "1210", "2101", "0121", "1012", "0011", "0001", "0111"));

        String nameOfAssociatedImage = tile.absoluteHeigtToRelativeHeight(tile.getCornerHeights());

        return validHeights.contains(nameOfAssociatedImage);

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

