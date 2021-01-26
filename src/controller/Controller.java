package controller;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import model.*;
import view.View;

import java.util.ArrayList;
import java.util.List;

public class Controller {
    private View view;
    private BasicModel model;

    public Controller(View view, BasicModel model) {
        this.view = view;
        this.model = model;
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
        }
    }
}
