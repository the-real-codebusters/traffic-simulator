package controller;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import model.*;
import view.MenuPane;
import view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Controller {
    private View view;
    private BasicModel model;
    private Pathfinder pathfinder;

    public Controller(View view, BasicModel model) {
        this.view = view;
        this.model = model;

        MapModel map = model.getMap();
//        model.printModelAttributes();

        // Ein generator wird erzeugt, der eine Map generiert (im Model)
        MapGenerator generator = new MapGenerator(map.getMapgen(), map);
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
        view.getCanvas().addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton().compareTo(MouseButton.PRIMARY) == 0) {
                double mouseX = event.getX();
                double mouseY = event.getY();
                Point2D isoCoord = view.findTileCoord(mouseX, mouseY);
                Tile selectedTile = model.getFieldGridOfMap()[(int)isoCoord.getX()][(int)isoCoord.getY()];
                generator.generateHeightMap();// Ist momentan nur zum Testen da
            }
        });
    }

    public void simulateOneDay(){
        List<Vehicle> activeVehicles = model.simulateOneDay();

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
            Point2D pointOnCanvas = view.moveCoordinates(vertex.getxCoordinateInGameMap(), vertex.getyCoordinateInGameMap());
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
     * und die entsprechenden Points dem Verkehrsgraph hinzugefügt.
     * @param event MouseEvent, wodurch die Methode ausgelöst wurde
     */
    public void managePlacement(MouseEvent event) {

        double mouseX = event.getX();
        double mouseY = event.getY();
        Point2D isoCoord = view.findTileCoord(mouseX, mouseY);
        int xCoord = (int) isoCoord.getX();
        int yCoord = (int) isoCoord.getY();

        MenuPane menuPane = view.getMenuPane();
        Building selectedBuilding = menuPane.getSelectedBuilding();

        if (model.getMap().canPlaceBuilding(xCoord, yCoord, selectedBuilding)) {

            if (selectedBuilding instanceof Road || selectedBuilding instanceof Rail) {
                selectedBuilding = model.checkCombines(xCoord, yCoord, selectedBuilding);
            }
            Building placedBuilding = model.getMap().placeBuilding(xCoord, yCoord, selectedBuilding);

            if(placedBuilding instanceof PartOfTrafficGraph){
                PartOfTrafficGraph partOfGraph = (PartOfTrafficGraph) placedBuilding;
                List<Vertex> addedVertices = model.getMap().addPointsToGraph(partOfGraph, xCoord, yCoord);

                if(placedBuilding instanceof Stop){
                    Station actualStation = ((Stop) placedBuilding).getStation();
                    List<Vertex> pathToStation = pathfinder.findPathToNextStation(addedVertices.get(0), actualStation);
                    // TODO Was wenn die Liste addedVertices leer ist?

                    boolean anotherStationFindable = false;
                    if(pathToStation.size() > 0) anotherStationFindable = true;

                    TrafficType trafficType = placedBuilding.getTrafficType();

                    if(anotherStationFindable) {
                        Vertex lastVertex = pathToStation.get(pathToStation.size()-1);
                        Station nextStation = lastVertex.getStation();
                        if(trafficType.equals(TrafficType.ROAD)){
                            System.out.println("nextStation "+nextStation.getId());
                            System.out.println("roadTrafficLine "+nextStation.getRoadTrafficLine());
                            nextStation.getRoadTrafficLine().addStationAndUpdateConnectedStations(actualStation);
                        }
                        else ; //TODO
                    }
                    else {
                        TrafficLine trafficLine = null;
                        switch (trafficType) {
                            case AIR: break;
                            case RAIL: break;
                            case ROAD:  trafficLine = new RoadTrafficLine(3, model);
                                        actualStation.setRoadTrafficLine(trafficLine);
                                        break;
                            default: break;
                        }
                        // TODO AIR, RAIL, desiredNumber
                        // Es crasht hier manchmal, weil Rails noch nicht umgesetzt ist

                        placedBuilding.setTrafficLine(trafficLine);
                        model.getNewCreatedOrIncompleteTrafficLines().add(trafficLine);
                }

                }

            }



            // Suchen, ob andere Station durch Graph findbar. Wenn ja, dann hinzufügen zu existierender Verkehrslinie
            // Wenn nein, dann neu erstellen

            view.drawMap();
            startCarMovement();
        }
    }

    // Diese globalen Variablen dienen einer experimentellen Anzeige der Animationen.
    // TODO In einem fertigen Programm sollten die Variablen nicht mehr in dieser Form vorhanden sein
    public int indexOfStart = 0;
    public int indexOfNext = indexOfStart + 1;
    public List<Vertex> path;
    public boolean notDone = true;

    /**
     * Bewegt ein Bild des Autos von Knoten v1 zu Knoten v2
     */
    public void moveCarFromPointToPoint(Vertex v1, Vertex v2){

        Point2D startPointOnCanvas = view.moveCoordinates(v1.getxCoordinateInGameMap(), v1.getyCoordinateInGameMap());
        startPointOnCanvas = view.changePointByTiles(startPointOnCanvas,
                v1.getxCoordinateRelativeToTileOrigin(),
                v1.getyCoordinateRelativeToTileOrigin());

        Point2D nextPointOnCanvas = view.moveCoordinates(v2.getxCoordinateInGameMap(), v2.getyCoordinateInGameMap());
        nextPointOnCanvas = view.changePointByTiles(nextPointOnCanvas,
                v2.getxCoordinateRelativeToTileOrigin(),
                v2.getyCoordinateRelativeToTileOrigin());

        view.translateCar(startPointOnCanvas, nextPointOnCanvas);
    }


    /**
     * Soll momentan dafür sorgen, dass sich ein Auto entlang mehrerer Points bewegt.
     * Es wird eine Liste von Knoten anhand einer Breitensuche ermittelt und durch diese Liste wird iteriert, so dass
     * bei jeder Iteration die nächsten zwei Knoten der Liste der Methode translateCar übergeben werden.
     * Die Animation beginnt bei 10 platzierten verbundenen Knoten.
     */
    public void startCarMovement(){
        List<Vertex> vertexes = getVertexesOfGraph();
        if(vertexes.size() >= 10) {

            Vertex startVertex = vertexes.get(indexOfStart);
            Vertex targetVertex = vertexes.get(vertexes.size()-1);
            if(notDone) path = pathfinder.findPathForRoadVehicle(startVertex, targetVertex);

            for(Vertex v : path){
                System.out.print(v.getName() + " -> ");
            }
            System.out.println();

            if(path.size() >= 10 && notDone) {
                System.out.println("path "+path.size());
                moveCarFromPointToPoint(path.get(indexOfStart), path.get(indexOfNext));
                notDone = false;
            }
        }
    }

    public boolean canPlaceBuildingAtPlaceInMapGrid(int row, int column, Building building){
        return model.getMap().canPlaceBuilding(row, column, building);
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
