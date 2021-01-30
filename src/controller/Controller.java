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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Controller {
    private View view;
    private BasicModel model;
    Building selectedBuilding;
    private Pathfinder pathfinder;

    public Controller(View view, BasicModel model) {
        this.view = view;
        this.model = model;

        selectedBuilding = view.getMenuPane().getSelectedBuilding();

        MapModel map = model.getMap();
//        model.printModelAttributes();

        // Ein generator wird erzeugt, der eine Map generiert (im Model)
        MapGenerator generator = new MapGenerator(map.getMapgen(), map);
        Tile[][] generatedMap = generator.generateMap(model);
        map.setFieldGrid(generatedMap);

        // Breite und Tiefe der Map aus dem Model werden in der View übernommen
        view.setMapWidth(map.getWidth());
        view.setMapDepth(map.getDepth());

        // Map wird durch Methode der View gezeichnet
        view.drawMap();
        TrafficGraph graph = model.getTrafficGraph();
        pathfinder = new Pathfinder(graph);

        // Ist momentan nur zum Testen da
        view.getCanvas().addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton().compareTo(MouseButton.PRIMARY) == 0) {
                double mouseX = event.getX();
                double mouseY = event.getY();
                Point2D isoCoord = view.findTileCoord(mouseX, mouseY);
                Tile selectedTile = model.getFieldGridOfMap()[(int)isoCoord.getX()][(int)isoCoord.getY()];
                generator.generateHeightMap();
            }
        });
    }

    public List<Vertex> getVertexesOfGraph(){
        List<Vertex> vertexes = new ArrayList<>();
        vertexes.addAll(model.getTrafficGraph().getMapOfVertexes().values());
        return vertexes;
    }

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
     * wird außerdem geprüft, ob es sich beim zu platzierenden Gebäude um eine Straße handelt und ob diese mit dem
     * ausgewählten Feld kombiniert werden kann. Anschließend wird das Gebäude auf der Karte platziert und die
     * entsprechenden Points dem Verkehrsgraph hinzugefügt.
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

        String originalBuildingName = selectedBuilding.getBuildingName();

        if (model.getMap().canPlaceBuilding(xCoord, yCoord, selectedBuilding)) {

            if (selectedBuilding instanceof Road) {
                menuPane.checkCombines(xCoord, yCoord);
            }
            model.getMap().placeBuilding(xCoord, yCoord, selectedBuilding);
            selectedBuilding.setBuildingName(originalBuildingName);

            if(selectedBuilding instanceof PartOfTrafficGraph){
                model.addPointsToGraph((PartOfTrafficGraph) selectedBuilding, xCoord, yCoord);
            }

            view.drawMap();
            startCarMovement();
        }
    }

    int indexOfStart = 0;
    int indexOfNext = indexOfStart + 1;
    private List<Vertex> path;
    boolean notDone = true;

    public void moveCarFromPointToPoint(){
        Vertex v1 = path.get(indexOfStart);
        Vertex v2 = path.get(indexOfNext);

        Point2D startPointOnCanvas = view.moveCoordinates(v1.getxCoordinateInGameMap(), v1.getyCoordinateInGameMap());
        startPointOnCanvas = view.changePointByTiles(startPointOnCanvas,
                v1.getxCoordinateRelativeToTileOrigin(),
                v1.getyCoordinateRelativeToTileOrigin());

        Point2D nextPointOnCanvas = view.moveCoordinates(v2.getxCoordinateInGameMap(), v2.getyCoordinateInGameMap());
        nextPointOnCanvas = view.changePointByTiles(nextPointOnCanvas,
                v2.getxCoordinateRelativeToTileOrigin(),
                v2.getyCoordinateRelativeToTileOrigin());

        view.translateCar(startPointOnCanvas, nextPointOnCanvas);
        indexOfStart++;
        indexOfNext++;
    }


    /**
     * Soll momentan dafür sorgen, dass sich ein Auto entlang mehrerer Points bewegt.
     * Es wird eine Liste von Knoten anhand einer Breitensuche ermittelt und durch diese Liste wird iteriert, so dass
     * bei jeder Iteration die nächsten zwei Knoten der Liste der Methode translateCar übergeben werden
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

//            Vertex v1 = vertexes.get(indexOfStart);
//            Point2D startPointOnCanvas = view.moveCoordinates(v1.getxCoordinateInGameMap(), v1.getyCoordinateInGameMap());
//            startPointOnCanvas = view.changePointByTiles(startPointOnCanvas,
//                    v1.getxCoordinateRelativeToTileOrigin(),
//                    v1.getyCoordinateRelativeToTileOrigin());

            if(path.size() >= 10 && notDone) {
                System.out.println("path "+path.size());
                moveCarFromPointToPoint();
                notDone = false;
            }
        }
    }

}
