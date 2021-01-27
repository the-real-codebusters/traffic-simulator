package controller;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import model.*;
import view.MenuPane;
import view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Controller {
    private View view;
    private BasicModel model;
    Building selectedBuilding;

    public Controller(View view, BasicModel model) {
        this.view = view;
        this.model = model;

        selectedBuilding = view.getMenuPane().getSelectedBuilding();

        MapModel map = model.getMap();
        model.printModelAttributes();

        // Ein generator wird erzeugt, der eine Map generiert (im Model)
        MapGenerator generator = new MapGenerator(map.getMapgen(), map);
        Tile[][] generatedMap = generator.generateMap(model);
        map.setFieldGrid(generatedMap);

        // Breite und Tiefe der Map aus dem Model werden in der View übernommen
        view.setMapWidth(map.getWidth());
        view.setMapDepth(map.getDepth());

        // Map wird durch Methode der View gezeichnet
        view.drawMap();
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
        // TODO gehört die Methode evtl. eher in den Controller?
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

            List<Vertex> vertexes = getVertexesOfGraph();
            if(vertexes.size() >= 10) {

                int indexOfStart = 0;
                int indexOfEnd = indexOfStart + 1;

                while (indexOfEnd < vertexes.size()-1) {

                    Vertex v1 = vertexes.get(indexOfStart);
                    Vertex v2 = vertexes.get(indexOfEnd);

                    Point2D pointOnCanvas = view.moveCoordinates(v1.getxCoordinateInGameMap(), v1.getyCoordinateInGameMap());
                    pointOnCanvas = view.changePointByTiles(pointOnCanvas,
                            v1.getxCoordinateRelativeToTileOrigin(),
                            v1.getyCoordinateRelativeToTileOrigin());

                    Point2D pointOnCanvas2 = view.moveCoordinates(v2.getxCoordinateInGameMap(), v2.getyCoordinateInGameMap());
                    pointOnCanvas2 = view.changePointByTiles(pointOnCanvas2,
                            v2.getxCoordinateRelativeToTileOrigin(),
                            v2.getyCoordinateRelativeToTileOrigin());

                    view.translateCar(pointOnCanvas, pointOnCanvas2);

                    indexOfStart++;
                    indexOfEnd++;
                }
            }
        }
    }
}
