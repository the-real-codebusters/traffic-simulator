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
        view.getMenuPane().setDayLabel(model.getDay());
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


        MenuPane menuPane = view.getMenuPane();
        Building selectedBuilding = menuPane.getSelectedBuilding();

        if (model.getMap().canPlaceBuilding(xCoord, yCoord, selectedBuilding)) {

            // Wenn ein Gebäude entfernt werden soll
            if(selectedBuilding.getBuildingName().equals("remove")){
                selectedBuilding = new Building();
                selectedBuilding.setBuildingName("ground");
                selectedBuilding.setWidth(1);
                selectedBuilding.setDepth(1);

                Tile selectedTile = model.getMap().getTileGrid()[xCoord][yCoord];
                Building buildingOnSelectedTile = selectedTile.getBuilding();

                // Wenn eine Straße/Rail abgerissen wird, sollen die zugehörigen Points aus Graph entfernt werden
                if(buildingOnSelectedTile instanceof PartOfTrafficGraph){

                    model.getMap().removePointsOnTile(buildingOnSelectedTile, xCoord, yCoord);
                }
            }

            if (selectedBuilding != null && selectedBuilding.getBuildingName().equals("height_up")){
                selectedBuilding = null;
                model.getMap().changeGroundHeight(xCoord, yCoord, 1);
            }

            if (selectedBuilding != null && selectedBuilding.getBuildingName().equals("height_down")){
                selectedBuilding = null;
                model.getMap().changeGroundHeight(xCoord, yCoord, -1);
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

    public int getDayFromModel(){
        return model.getDay();
    }

    public Tile[][] getFields() { return model.getFieldGridOfMap(); }

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
