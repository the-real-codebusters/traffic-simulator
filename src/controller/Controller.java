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

    //Wenn eine Verkehrslinie erstellt werden soll und der Benutzer Stationen auswählen kann, sollen die Stationen
    //zu dieser Liste hinzugefügt werden
    private List<Station> stationsOfPlannedTrafficLine = new ArrayList<>();
    private ConnectedTrafficPart trafficPartOfPlannedTrafficLine;

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

        TrafficGraph roadGraph = model.getMap().getRoadGraph();
        TrafficGraph railGraph = model.getMap().getRailGraph();

        pathfinder = new Pathfinder(roadGraph, railGraph);
        model.setPathfinder(pathfinder);

//        new TrafficLineCreationDialog(view);
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

    public List<Vertex> getVertexesOfGraphs(){
        List<Vertex> vertexes = new ArrayList<>();
        vertexes.addAll(model.getMap().getRoadGraph().getMapOfVertexes().values());
        vertexes.addAll(model.getMap().getRailGraph().getMapOfVertexes().values());

        return vertexes;
    }

    public List<Vehicle> getVehicleTypesForTrafficType(TrafficType type){
        return model.getVehicleTypesForName(type);
    }

    /**
     * Zeichnet die Knoten des Straßengraphen als gelbe Punkte in der View ein. Dies soll vor allem zum Testen der
     * Animationen dienen.
     */
    public void drawVertexesOfGraph(){
        Canvas canvas = view.getCanvas();
        List<Vertex> vertexes = getVertexesOfGraphs();
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

    public boolean canPlaceBuildingAtPlaceInMapGrid(int row, int column, Building building){
        return model.getMap().canPlaceBuilding(row, column, building);
    }

    public void showTrafficPartInView(MouseEvent event){
        Building building = getBuildingForMouseEvent(event);
        if(building instanceof PartOfTrafficGraph){
            ConnectedTrafficPart trafficPart = ((PartOfTrafficGraph) building).getAssociatedPartOfTraffic();
            if(trafficPart != null){
                view.getMenuPane().showTrafficPart(trafficPart);
            }
        }
    }

    public void selectStationsForTrafficLine(MouseEvent event){
        Building building = getBuildingForMouseEvent(event);
        if(building instanceof Stop){
            Station station = ((Stop) building).getStation();
            TrafficType trafficType = view.getTrafficLinePopup().getTrafficType();
            System.out.println("trafficType in selectStationsForTrafficLine "+trafficType);

            if(stationsOfPlannedTrafficLine.size() == 0){
                trafficPartOfPlannedTrafficLine = station.getTrafficPartForTrafficType(trafficType);
            }

            //True, wenn Station Teil eines Verkehrsteils mit dem angegebenen typ ist und wenn die Station Teil des
            //konkret angeklickten Verkehrsteils ist
            if(station.hasPartOfTrafficType(trafficType) &&
                trafficPartOfPlannedTrafficLine.getStations().contains(station)
            ){
                // Benutzer kann Stationen aus geplanter Liste mit nochmaligem Klick löschen
                if(stationsOfPlannedTrafficLine.contains(station)){
                    stationsOfPlannedTrafficLine.remove(station);
                }
                else {
                    stationsOfPlannedTrafficLine.add(station);
                }
                System.out.println("stationsOfPlannedTrafficLine in controller: ");
                stationsOfPlannedTrafficLine.forEach((x) -> System.out.println("Station id: "+x.getId()));
                //Zeigt Liste in Popup an
                view.getTrafficLinePopup().showList(stationsOfPlannedTrafficLine);
            }
        }
    }

    private Building getBuildingForMouseEvent(MouseEvent event){
        double mouseX = event.getX();
        double mouseY = event.getY();
        Point2D isoCoord = view.findTileCoord(mouseX, mouseY);
        int xCoord = (int) isoCoord.getX();
        int yCoord = (int) isoCoord.getY();

        Building building = model.getMap().getTileGrid()[xCoord][yCoord].getBuilding();
        return building;
    }


    public void createNewTrafficLine(Map<String, Integer> desiredNumbersOfVehicleNames,
                                     TrafficType trafficType, String name){

        Map<Vehicle, Integer> mapDesiredNumbers = new HashMap<>();
        for (Map.Entry<String, Integer> entry : desiredNumbersOfVehicleNames.entrySet()) {
            Vehicle vehicle = model.getVehicleTypesForName(entry.getKey());
            System.out.println("vehicle in createNewTrafficLine "+vehicle.getGraphic());
            mapDesiredNumbers.put(vehicle, entry.getValue());
        }
        List<Station> stationList = new ArrayList<>(stationsOfPlannedTrafficLine);
        TrafficLine trafficLine = new TrafficLine(model, trafficType, stationList, mapDesiredNumbers, name);
        trafficPartOfPlannedTrafficLine.addTrafficLine(trafficLine);
        clearPlannedTrafficLine();
    }

    public void clearPlannedTrafficLine(){
        stationsOfPlannedTrafficLine.clear();
        this.trafficPartOfPlannedTrafficLine = null;
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
