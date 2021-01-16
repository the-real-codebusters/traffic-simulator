package controller;

import model.*;
import view.View;

import java.util.ArrayList;
import java.util.List;

public class Controller {
    private View view;

    public Controller(View view, MapModel map) {
        this.view = view;

        // buildings und map sind hier lediglich Testobjekte und sollen später anhand der Daten in der
        // JSON-Datei erzeugt werden
        List<Building> buildings = new ArrayList<>();

        // Ein generator wird erzeugt, der eine Map generiert (im Model)
        MapGenerator generator = new MapGenerator("planverkehr", map);
        Field[][] generatedMap = generator.generateMap(map);
        map.setFieldGrid(generatedMap);

        // Breite und Tiefe der Map aus dem Model werden in der View übernommen
        view.setMapWidth(map.getWidth());
        view.setMapDepth(map.getDepth());

        // Map wird durch Methode der View gezeichnet
        view.drawMap(generatedMap);

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
