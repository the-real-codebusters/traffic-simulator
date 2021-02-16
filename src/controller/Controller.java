package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import model.*;
import view.MenuPane;
import view.View;


import java.util.*;


public class Controller {
    private View view;
    private BasicModel model;
    private Pathfinder pathfinder;
    private Map <Tile, Point2D> tileToPositionInGridNeighbors = new LinkedHashMap<>();

    public Controller(View view, BasicModel model) {
        this.view = view;
        this.model = model;

        MapModel map = model.getMap();
//        model.printModelAttributes();

        // Ein generator wird erzeugt, der eine Map generiert (im Model)
        MapGenerator generator = new MapGenerator(map.getMapgen(), map, model);
        Tile[][] generatedMap = generator.generateMap(model);
        map.setTileGrid(generatedMap);

        view.setController(this);
        view.storeImageRatios();
        view.generateMenuPane(this);


        // Breite und Tiefe der Map aus dem Model werden in der View übernommen
        view.setMapWidth(map.getWidth());
        view.setMapDepth(map.getDepth());

        // Map wird durch Methode der View gezeichnet
        view.drawMap();

        TrafficGraph graph = model.getMap().getRawRoadGraph();
        pathfinder = new Pathfinder(graph);
        model.setPathfinder(pathfinder);

        simulateOneDay();
    }

    //TODO Was wenn zwei TrafficLines zu einer verbunden werden?

    public void simulateOneDay(){
        List<VehicleMovement> movements = model.simulateOneDay();
        if(movements.size() > 0){
            view.translateVehicles(movements);
        }
        else {
            waitForOneDay();
        }

    }

    public void waitForOneDay(){
        Timeline timeline = new Timeline(new KeyFrame(
                Duration.seconds(view.getTickDuration()),
                ae -> simulateOneDay()));
        timeline.play();
    }

    public List<Vertex> getVertexesOfGraph(){
        List<Vertex> vertexes = new ArrayList<>();
        vertexes.addAll(model.getMap().getRawRoadGraph().getMapOfVertexes().values());
        return vertexes;
    }

    /**
     * Zeichnet die Knoten des Straßengraphen als gelbe Punkte in der View ein. Dies soll vor allem zum Testen der
     * Animationen dienen.
     */
    public void drawVertexesOfGraph(){
        Canvas canvas = view.getCanvas();
        List<Vertex> vertexes = getVertexesOfGraph();
        for(Vertex vertex: vertexes){
            Point2D pointOnCanvas = view.translateTileCoordsToCanvasCoords(vertex.getxCoordinateInGameMap(), vertex.getyCoordinateInGameMap());
            // pointOnCanvas ist an der Stelle der linken Ecke des Tiles

            pointOnCanvas = view.changePointByTiles(pointOnCanvas,
                    vertex.getxCoordinateRelativeToTileOrigin(),
                    vertex.getyCoordinateRelativeToTileOrigin());

            canvas.getGraphicsContext2D().setFill(Color.YELLOW);
            canvas.getGraphicsContext2D().fillOval(pointOnCanvas.getX()-2.5, pointOnCanvas.getY()-2.5, 5, 5);
            canvas.getGraphicsContext2D().setFill(Color.BLACK);
//            System.out.println("drawVertexesOfGraph: " + pointOnCanvas);
        }
    }


    /**
     * Die Methode bekommt ein event übergeben und prüft, ob ein Gebäude platziert werden darf. Ist dies der Fall, so
     * wird außerdem geprüft, ob es sich beim zu platzierenden Gebäude um eine Straße oder ien Gleis handelt und ob
     * diese mit dem ausgewählten Feld kombiniert werden kann. Anschließend wird das Gebäude auf der Karte platziert
     * und die entsprechenden Points dem Verkehrsgraph hinzugefügt. Wenn es sich um eine Haltestelle handelt, wird
     * außerdem entweder eine neue Station erstellt oder die Haltestelle der passenden Station hinzugefügt. Außerdem wird
     * entweder eine neue Verkehrslinie erstellt oder die Station der vorhandenen Verkehrslinie hinzugefügt.
     * @param event MouseEvent, wodurch die Methode ausgelöst wurde
     */
    public void managePlacement(MouseEvent event) {

        double mouseX = event.getX();
        double mouseY = event.getY();
        Point2D isoCoord = view.findTileCoord(mouseX, mouseY);
        if(isoCoord != null){
        int xCoord = (int) isoCoord.getX();
        int yCoord = (int) isoCoord.getY();
        List<Vertex> addedVertices;


        MenuPane menuPane = view.getMenuPane();
        Building selectedBuilding = menuPane.getSelectedBuilding();

        if (model.getMap().canPlaceBuilding(xCoord, yCoord, selectedBuilding)) {

            // Wenn ein Gebäude entfernt werden soll
            if(selectedBuilding.getBuildingName().equals("remove")){
                selectedBuilding = new Building();
                selectedBuilding.setBuildingName("grass");
                selectedBuilding.setWidth(1);
                selectedBuilding.setDepth(1);

                Tile selectedTile = model.getMap().getTileGrid()[xCoord][yCoord];
                Building buildingOnSelectedTile = selectedTile.getBuilding();

                // Wenn eine Straße/Rail abgerissen wird, sollen die zugehörigen Points aus Graph entfernt werden
                if(buildingOnSelectedTile instanceof PartOfTrafficGraph){

                    PartOfTrafficGraph partOfGraph = (PartOfTrafficGraph) buildingOnSelectedTile;
                    addedVertices = model.getMap().addPointsToGraph(partOfGraph, xCoord, yCoord);

                    for(Vertex v : addedVertices){
                        if(v.getName().contains("c")) {
                            model.getMap().getRawRoadGraph().removeVertex(v.getName());
                        }
                    }

                    // TODO so anpassen, dass es auch für rails funktioniert

                    Map<String, Vertex> vertexesInGraph = model.getMap().getRawRoadGraph().getMapOfVertexes();
                    Iterator<Map.Entry<String, Vertex>> iterator = vertexesInGraph.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, Vertex> vertex = iterator.next();
                        List<Vertex> connections = model.getMap().getRawRoadGraph().getAdjacencyMap().get(vertex.getKey());
                        if(connections.size()== 0) {
                            iterator.remove();
                            continue;
                        }
                    }
                    model.getMap().getRawRoadGraph().printGraph();
                }
            }

            if (selectedBuilding != null && selectedBuilding.getBuildingName().equals("height_up")){
                selectedBuilding = null;
                changeGroundHeight(xCoord, yCoord, 1);
            }

            if (selectedBuilding != null && selectedBuilding.getBuildingName().equals("height_down")){
                selectedBuilding = null;
                changeGroundHeight(xCoord, yCoord, -1);
            }



            if (selectedBuilding instanceof Road || selectedBuilding instanceof Rail) {
                selectedBuilding = model.checkCombines(xCoord, yCoord, selectedBuilding);
            }

            if(selectedBuilding != null) {
                Building placedBuilding = model.getMap().placeBuilding(xCoord, yCoord, selectedBuilding);
            }

            // Suchen, ob andere Station durch Graph findbar. Wenn ja, dann hinzufügen zu existierender Verkehrslinie
            // Wenn nein, dann neu erstellen

            view.drawMap();
            if(model.getNewCreatedOrIncompleteTrafficParts().size() > 0) {
                System.out.println("Size of incompleteConnectedTrafficParts "+model.getNewCreatedOrIncompleteTrafficParts().size());
            }
        }
        }
    }



    /**
     * Verändert die Höhe des Bodens an den angeklickten Koordinaten um einen gegebenen Wert
     * @param xCoord
     * @param yCoord
     * @param heightShift
     */
    public void changeGroundHeight(int xCoord, int yCoord, int heightShift) {

        Tile[][] grid = model.getMap().getTileGrid();

        // Tiles an der angeklickten Stelle
        Tile tileN = grid[xCoord][yCoord];
        Tile tileW = grid[xCoord][yCoord - 1];
        Tile tileS = grid[xCoord + 1][yCoord - 1];
        Tile tileE = grid[xCoord + 1][yCoord];

        List<Tile> tilesToBeUpdated = new ArrayList<>();
        tilesToBeUpdated.addAll(Arrays.asList(tileN, tileW, tileS, tileE));

        // Map, die ein Tile auf seine isometrischen Koordinaten im Spielfeld abbildet
        Map<Tile, Point2D> tileToPositionInGrid = new LinkedHashMap<>();
        tileToPositionInGrid.put(tileN, new Point2D(xCoord, yCoord));
        tileToPositionInGrid.put(tileW, new Point2D(xCoord, yCoord - 1));
        tileToPositionInGrid.put(tileS, new Point2D(xCoord + 1, yCoord - 1));
        tileToPositionInGrid.put(tileE, new Point2D(xCoord + 1, yCoord));


        // Höhe an der angeklickten Stelle vor der Bearbeitung
        int startHeight = tileN.getCornerHeights().get("cornerS");


        // ändere die Höhen der Tiles, die sich direkt um die angeklickte stelle herum befinden
        updateFirstLevelHeights(xCoord, yCoord, heightShift);

        // prüfe, ob durch geänderte Höhen Anpassungen an den vier Start-Tiles nötig sind und passe ggf. an
        if (!heightConstraintsMetForAllTiles(tilesToBeUpdated)) {
            for (Tile tile : tilesToBeUpdated) {
                updateHeightIfNecessary(tile);
            }
        }

        // solange der Wert von startHeight > 0 ist, müssen die Nachbarn der veränderten Tiles ebenfalls geprüft werden
        while (startHeight > 0) {
            checkNeighbors(tileToPositionInGrid);
            startHeight--;
        }
    }


    /**
     *
     * @param tileToPositionInGrid
     */
    public void checkNeighbors(Map<Tile, Point2D> tileToPositionInGrid){
        Map <Tile, Point2D> tileToPositionInGridNeighbors = new LinkedHashMap<>();

        // Prüfe benachbarte Felder für jedes der zuvor veränderten Felder
        for(Tile tile : tileToPositionInGrid.keySet()){
            Map<String, Tile> neighbors = getNeighbors(tileToPositionInGrid.get(tile));
            for (Map.Entry<String, Tile> neighbor : neighbors.entrySet()){

                Point2D coordsOfNeighbor = checkNeighborHeight(tile, neighbor, tileToPositionInGrid.get(tile));

                if(!(tileToPositionInGrid.values().contains(coordsOfNeighbor))) {
                    tileToPositionInGridNeighbors.put(neighbor.getValue(), coordsOfNeighbor);
                }
            }
        }
        tileToPositionInGrid.clear();
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


        List<String> validHeights = new ArrayList<>();
        validHeights.addAll(Arrays.asList("0100", "1101", "0101", "0000", "1000", "1100", "1010", "1001", "1011",
                "0010", "0110", "1110", "1210", "2101", "0121", "1012", "0011", "0001", "0111"));

        if (validHeights.contains(neighbor.getValue().absoluteHeigtToRelativeHeight(neighbor.getValue().getCornerHeights()))){
            grid[xCoord][yCoord] = new Tile(ground, neighbor.getValue().getCornerHeights(), false);
        }
        else {
            updateHeightIfNecessary(neighbor.getValue());
            System.out.println(xCoord + " " + yCoord + " " + neighbor.getValue().getCornerHeights() + " " + neighbor.getKey());
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
     * Prüft für ein gegebenes Tile, ob die Höhen-Constraints eingehalten werden und ändert deren werte, falls sie
     * nicht eingehalten werden
     * @param tile
     */
    private void updateHeightIfNecessary(Tile tile){
        // reihenfolge der corners in getCornerHeights: NESW

        // Werte aus cornerHeights als String aus 4 Zeichen
        String heights = "";
        for (Integer corner : tile.getCornerHeights().values()){
            heights += corner;
        }

        if (!heightConstraintsMetInTile(tile)) {

            int maxCorner = tile.findMaxCorner(tile.getCornerHeights());
            String maxCornerString = String.valueOf(maxCorner);
            int indexOfMaxCorner = heights.indexOf(maxCornerString);


            int heightDiffToPrevious;
            int heightDiffToNext;
            int heightDiffToOpposite;
            int newHeight;

            // Wenn sich die Ecke mit dem höchsten Wert an der ersten Stelle im String befindet
            if (indexOfMaxCorner == 0){
                heightDiffToPrevious = maxCorner - Character.getNumericValue(heights.charAt(3));
                if (Math.abs(heightDiffToPrevious) > 1) {
                    newHeight =  (heightDiffToPrevious -1 );
                    tile.updateCornerHeight("cornerW", newHeight);
                }
                heightDiffToNext = maxCorner - Character.getNumericValue(heights.charAt(1));
                if (Math.abs(heightDiffToNext) > 1) {
                    newHeight =  (heightDiffToNext -1);
                    tile.updateCornerHeight("cornerE", newHeight);
                }
                heightDiffToOpposite = maxCorner - Character.getNumericValue(heights.charAt(2));
                if (Math.abs(heightDiffToOpposite) > 2) {
                    newHeight = (heightDiffToOpposite - 2);
                    tile.updateCornerHeight("cornerS", newHeight);
                }

            // Wenn die Ecke mit dem höchsten Wert an der zweiten Stelle im String befindet
            } else if (indexOfMaxCorner == 1) {
                heightDiffToPrevious = maxCorner - Character.getNumericValue(heights.charAt(0));
                if (Math.abs(heightDiffToPrevious) > 1) {
                    newHeight =  (heightDiffToPrevious -1 );
                    tile.updateCornerHeight("cornerN", newHeight);
                }
                heightDiffToNext = maxCorner - Character.getNumericValue(heights.charAt(2));
                if (Math.abs(heightDiffToNext) > 1) {
                    newHeight =  (heightDiffToNext -1 );
                    tile.updateCornerHeight("cornerS", newHeight);
                }
                heightDiffToOpposite = maxCorner - Character.getNumericValue(heights.charAt(3));
                if (Math.abs(heightDiffToOpposite) > 2) {
                    newHeight =  (heightDiffToOpposite -2 );
                    tile.updateCornerHeight("cornerW", newHeight);
                }

            // Wenn die Ecke mit dem höchsten Wert an der dritten Stelle im String befindet
            } else if (indexOfMaxCorner == 2){
                heightDiffToPrevious = maxCorner -  Character.getNumericValue(heights.charAt(1));
                if (Math.abs(heightDiffToPrevious) > 1) {
                    newHeight =  (heightDiffToPrevious -1 );
                    tile.updateCornerHeight("cornerE", newHeight);
                }
                heightDiffToNext = maxCorner - Character.getNumericValue(heights.charAt(3));
                if (Math.abs(heightDiffToNext) > 1) {
                    newHeight =  (heightDiffToNext -1 );
                    tile.updateCornerHeight("cornerW", newHeight);
                }
                heightDiffToOpposite = maxCorner - Character.getNumericValue(heights.charAt(0));
                if (Math.abs(heightDiffToOpposite) > 2) {
                    newHeight =  (heightDiffToOpposite -2 );
                    tile.updateCornerHeight("cornerN", newHeight);
                }

                // Wenn die Ecke mit dem höchsten Wert an der letzten Stelle im String befindet
            } else {
                heightDiffToPrevious = maxCorner - Character.getNumericValue(heights.charAt(2));
                if (Math.abs(heightDiffToPrevious) > 1) {
                    newHeight =  (heightDiffToPrevious -1);
                    tile.updateCornerHeight("cornerS", newHeight);
                }
                heightDiffToNext = maxCorner - Character.getNumericValue(heights.charAt(0));
                if (Math.abs(heightDiffToNext) > 1) {
                    newHeight =  (heightDiffToNext -1);
                    tile.updateCornerHeight("cornerN", newHeight);
                }
                heightDiffToOpposite = maxCorner - Character.getNumericValue(heights.charAt(1));
                if (Math.abs(heightDiffToOpposite) > 2) {
                    newHeight =  (heightDiffToOpposite -2);
                    tile.updateCornerHeight("cornerE", newHeight);
                }
            }
        }
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


    /**
     * Ändert die Höhen der Tiles, die sich um die angegebenen Koordinaten befinden um einen angegebenen Wert
     * @param xCoord
     * @param yCoord
     * @param heightShift Höhe, um die die angegebene Stelle geändert werden soll
     */
    public void updateFirstLevelHeights(int xCoord, int yCoord, int heightShift){
        Tile[][] grid = model.getMap().getTileGrid();

        //tiles um die angeklickte stelle
        Tile tileN = grid[xCoord][yCoord];
        Tile tileW = grid[xCoord][yCoord-1];
        Tile tileS = grid[xCoord+1][yCoord-1];
        Tile tileE = grid[xCoord+1][yCoord];


        Map<String, Integer> updatedHeights;
        Building ground = new Building(1, 1, "ground");
        String absoluteTileHeight;
        boolean isWater;


        updatedHeights = tileS.updateCornerHeight("cornerN", heightShift);
        absoluteTileHeight = tileS.absoluteHeigtToRelativeHeight(updatedHeights);
        isWater = absoluteTileHeight.equals("0000") && updatedHeights.get("cornerS") < 0;
        grid[xCoord + 1][yCoord - 1] = new Tile(ground, updatedHeights, isWater);

        updatedHeights = tileW.updateCornerHeight("cornerE", heightShift);
        absoluteTileHeight = tileW.absoluteHeigtToRelativeHeight(updatedHeights);
        isWater = absoluteTileHeight.equals("0000") && updatedHeights.get("cornerS") < 0;
        grid[xCoord][yCoord - 1] = new Tile(ground, updatedHeights, isWater);

        updatedHeights = tileN.updateCornerHeight("cornerS", heightShift);
        absoluteTileHeight = tileN.absoluteHeigtToRelativeHeight(updatedHeights);
        isWater = absoluteTileHeight.equals("0000") && updatedHeights.get("cornerS") < 0;
        grid[xCoord][yCoord] = new Tile(ground, updatedHeights, isWater);

        updatedHeights = tileE.updateCornerHeight("cornerW", heightShift);
        absoluteTileHeight = tileE.absoluteHeigtToRelativeHeight(updatedHeights);
        isWater = absoluteTileHeight.equals("0000") && updatedHeights.get("cornerS") < 0;
        grid[xCoord + 1][yCoord] = new Tile(ground, updatedHeights, isWater);
    }





    // Diese globalen Variablen dienen einer experimentellen Anzeige der Animationen.
    // TODO In einem fertigen Programm sollten die Variablen nicht mehr in dieser Form vorhanden sein
//    public int indexOfStart = 0;
//    public int indexOfNext = indexOfStart + 1;
//    public List<Vertex> path;
//    public boolean notDone = true;

//    /**
//     * Bewegt ein Bild des Autos von Knoten v1 zu Knoten v2
//     */
//    public void moveCarFromPointToPoint(Vertex v1, Vertex v2){
//
//        Point2D startPointOnCanvas = view.translateTileCoordsToCanvasCoords(v1.getxCoordinateInGameMap(), v1.getyCoordinateInGameMap());
//        startPointOnCanvas = view.changePointByTiles(startPointOnCanvas,
//                v1.getxCoordinateRelativeToTileOrigin(),
//                v1.getyCoordinateRelativeToTileOrigin());
//
//        Point2D nextPointOnCanvas = view.translateTileCoordsToCanvasCoords(v2.getxCoordinateInGameMap(), v2.getyCoordinateInGameMap());
//        nextPointOnCanvas = view.changePointByTiles(nextPointOnCanvas,
//                v2.getxCoordinateRelativeToTileOrigin(),
//                v2.getyCoordinateRelativeToTileOrigin());
//
//        view.translateVehicle(startPointOnCanvas, nextPointOnCanvas);
//    }


//    /**
//     * Soll momentan dafür sorgen, dass sich ein Auto entlang mehrerer Points bewegt.
//     * Es wird eine Liste von Knoten anhand einer Breitensuche ermittelt und durch diese Liste wird iteriert, so dass
//     * bei jeder Iteration die nächsten zwei Knoten der Liste der Methode translateCar übergeben werden.
//     * Die Animation beginnt bei 10 platzierten verbundenen Knoten.
//     */
//    public void startCarMovement(){
//        List<Vertex> vertexes = getVertexesOfGraph();
//        if(vertexes.size() >= 10) {
//
//            Vertex startVertex = vertexes.get(indexOfStart);
//            Vertex targetVertex = vertexes.get(vertexes.size()-1);
//            if(notDone) path = pathfinder.findPathForRoadVehicle(startVertex, targetVertex);
//
//            for(Vertex v : path){
//                System.out.print(v.getName() + " -> ");
//            }
//            System.out.println();
//
//            if(path.size() >= 10 && notDone) {
//                System.out.println("path "+path.size());
//                moveCarFromPointToPoint(path.get(indexOfStart), path.get(indexOfNext));
//                notDone = false;
//            }
//        }
//    }

    public boolean canPlaceBuildingAtPlaceInMapGrid(int row, int column, Building building){
        return model.getMap().canPlaceBuilding(row, column, building);
    }

    public void showTrafficPartInView(MouseEvent event){
        double mouseX = event.getX();
        double mouseY = event.getY();
        Point2D isoCoord = view.findTileCoord(mouseX, mouseY);
        if(isoCoord != null){
            int xCoord = (int) isoCoord.getX();
            int yCoord = (int) isoCoord.getY();

            Building building = model.getMap().getTileGrid()[xCoord][yCoord].getBuilding();
            if(building instanceof PartOfTrafficGraph){
                ConnectedTrafficPart trafficPart = ((PartOfTrafficGraph) building).getAssociatedPartOfTraffic();
                if(trafficPart != null){
                    view.getMenuPane().showTrafficPart(trafficPart);
                }
            }
        }

    }

    public Tile getTileOfMapTileGrid(int row, int column){
        return model.getMap().getTileGrid()[row][column];
    }

    public String getGamemode(){
        return model.getGamemode();
    }

    public Set<String> getBuildmenus(){
        return model.getBuildmenus();
    }

    public List<Building> getBuildingsByBuildmenu(String buildmenu){
        return model.getBuildingsForBuildmenu(buildmenu);
    }

}
