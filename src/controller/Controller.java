package controller;

import model.*;
import view.View;


public class Controller {
    private View view;

    public Controller(View view, BasicModel model) {
        this.view = view;
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

        // Dummy-Graphen zum Testen der Methoden
        TrafficGraph directedGraph = new TrafficGraph();
        TrafficGraph undirectedGraph = new TrafficGraph();
        TrafficGraph graphWithDuplicates = new TrafficGraph();

        // Methoden von TrafficGraph werden getestet
//        directedGraph.testGraphAdd();
//        System.out.println();
//        directedGraph.testGraphRemove();
//        System.out.println();
//        undirectedGraph.testGraphAddBidirectional();
//        System.out.println();
//        undirectedGraph.testGraphRemoveBidirectional();
//        System.out.println();
//        directedGraph.testJoinPoints();
//        System.out.println();
//        graphWithDuplicates.testChekForDuplicates();
    }
}
