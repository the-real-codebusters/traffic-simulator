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
    private final View view;
    private final BasicModel model;
    private final ResourceBundle resourceBundle;

    //Wenn eine Verkehrslinie erstellt werden soll und der Benutzer Stationen auswählen kann, sollen die Stationen
    //zu dieser Liste hinzugefügt werden
    private final List<Station> stationsOfPlannedTrafficLine = new ArrayList<>();
    private ConnectedTrafficPart trafficPartOfPlannedTrafficLine;

    public Controller(View view, BasicModel model, ResourceBundle resourceBundle) {
        this.view = view;
        this.model = model;
        this.resourceBundle = resourceBundle;

        MapModel map = model.getMap();

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
        TrafficGraph airGraph = model.getMap().getAirGraph();

        Pathfinder pathfinder = new Pathfinder(roadGraph, railGraph, airGraph, model.getMap().getTrafficLineGraph());
        model.setPathfinder(pathfinder);

        //Starte Game-Loop
        gameLoop();
    }

    /**
     * Hält Game-Loop am laufen. Bewegungen werden durch die View angezeigt
     */
    public void gameLoop(){
        List<VehicleMovement> movements = model.simulateOneDay();

        view.getMenuPane().setDayLabel(model.getDay());
        if(movements.size() > 0){
            view.translateVehicles(movements);
        }
        else {
            waitForOneDay();
        }
    }

    /**
     * Wartet die Zeit eines Tags. Dient zur Überbrückung der Zeit, wenn es keine Fahrzeuge gibt
     */
    public void waitForOneDay(){
        Timeline timeline = new Timeline(new KeyFrame(
                Duration.seconds(view.getDayDuration()),
                ae -> gameLoop()));
        timeline.play();
    }

    public List<Vertex> getVertexesOfGraphs(){
        List<Vertex> vertexes = new ArrayList<>();
        vertexes.addAll(model.getMap().getRoadGraph().getMapOfVertexes().values());
        vertexes.addAll(model.getMap().getRailGraph().getMapOfVertexes().values());
        vertexes.addAll(model.getMap().getAirGraph().getMapOfVertexes().values());

        return vertexes;
    }

    public List<Vehicle> getVehicleTypesForTrafficType(TrafficType type){
        return model.getVehicleTypeForName(type);
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
        }
    }


    /**
     * Die Methode bekommt ein event übergeben und prüft, ob ein Gebäude platziert werden darf. Ist dies der Fall, so
     * wird außerdem geprüft, ob es sich beim zu platzierenden Gebäude um eine Straße oder ien Gleis handelt und ob
     * diese mit dem ausgewählten Feld kombiniert werden kann. Anschließend wird das Gebäude auf der Karte platziert
     * und die entsprechenden Points dem Verkehrsgraph hinzugefügt. Wenn es sich um eine Haltestelle handelt, wird
     * außerdem entweder eine neue Station erstellt oder die Haltestelle der passenden Station hinzugefügt. Außerdem wird
     * entweder ein neuer Verkehrsbereich erstellt oder die Station dem vorhandenen Verkehrsbereich hinzugefügt.
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
                    Tile selectedTile = model.getMap().getTileGrid()[xCoord][yCoord];
                    Building buildingOnSelectedTile = selectedTile.getBuilding();
                    int endRow = buildingOnSelectedTile.getOriginRow() + buildingOnSelectedTile.getWidth();
                    int endColumn = buildingOnSelectedTile.getOriginColumn() + buildingOnSelectedTile.getDepth();
                    for(int row = buildingOnSelectedTile.getOriginRow(); row < endRow; row++){
                        for(int column = buildingOnSelectedTile.getOriginColumn(); column < endColumn; column++){
                            selectedBuilding = new Building();
                            selectedBuilding.setBuildingName("ground");
                            selectedBuilding.setWidth(1);
                            selectedBuilding.setDepth(1);
                            model.getMap().placeBuilding(row, column, selectedBuilding);
                        }
                    }


                    // Wenn eine Straße/Rail abgerissen wird, sollen die zugehörigen Points aus Graph entfernt werden
                    if(buildingOnSelectedTile instanceof PartOfTrafficGraph){

                        model.getMap().removePointsOnTile(buildingOnSelectedTile);
                    }
                    return;
                }

                if (selectedBuilding != null && selectedBuilding.getBuildingName().equals("height_up")){
                    selectedBuilding = null;
                    model.getMap().changeGroundHeight(xCoord, yCoord, 1);
                }

                if (selectedBuilding != null && selectedBuilding.getBuildingName().equals("height_down")){
                    selectedBuilding = null;
                    model.getMap().changeGroundHeight(xCoord, yCoord, -1);
                }

                if (selectedBuilding instanceof Road || selectedBuilding instanceof Rail
                    || selectedBuilding instanceof JustCombines) {
                    selectedBuilding = model.checkCombines(xCoord, yCoord, selectedBuilding);
                }


                if(selectedBuilding != null) {
                    model.getMap().placeBuilding(xCoord, yCoord, selectedBuilding);
                }

                //Neuzeichnen der Map zum Anzeigen des neuen Buildings
                view.drawMap();
            }
        }
    }

    public boolean canPlaceBuildingAtPlaceInMapGrid(int row, int column, Building building){
        return model.getMap().canPlaceBuilding(row, column, building);
    }

    //Zeigt auf der MenuPane Informationen zum Verkehrsbereich des buildings an
    public void showTrafficPartInView(Building building){
        if(building instanceof PartOfTrafficGraph){
            ConnectedTrafficPart trafficPart = ((PartOfTrafficGraph) building).getAssociatedPartOfTraffic();

            if(trafficPart != null){
                view.getMenuPane().showTrafficPart(trafficPart);
            }
        }
    }

    /**
     * Im Modus der Auswahl der Stationen für eine Verkehrslinie werden ausgewählte Stationen in einem Popup angezeigt
     * @param event
     */
    public void selectStationsForTrafficLine(MouseEvent event){
        Building building = getBuildingForMouseEvent(event);
        //Nur Stops sind Teil einer Station
        if(building instanceof Stop){
            Station station = ((Stop) building).getStation();
            TrafficType trafficType = view.getTrafficLinePopup().getTrafficType();

            // Bei Flughäfen darf nur ein vollständiger Flughafen ausgewählt werden. Deswegen springe aus der Methode
            if(trafficType.equals(TrafficType.AIR) && !station.isWholeAirstation()){
                return;
            }

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
                stationsOfPlannedTrafficLine.forEach((x) -> System.out.println("Station id: "+x.getId()));
                //Zeigt Liste in Popup an
                view.getTrafficLinePopup().showList(stationsOfPlannedTrafficLine, getResourceBundle());
            }
        }
    }

    /**
     * Gibt das Gebäude zurück, auf das geklickt wurde
     * @param event
     * @return
     */
    public Building getBuildingForMouseEvent(MouseEvent event){
        double mouseX = event.getX();
        double mouseY = event.getY();
        Point2D isoCoord = view.findTileCoord(mouseX, mouseY);
        if(isoCoord == null) return  null;
        int xCoord = (int) isoCoord.getX();
        int yCoord = (int) isoCoord.getY();

        Building building = model.getMap().getTileGrid()[xCoord][yCoord].getBuilding();
        return building;
    }

    public void createNewTrafficLine(Map<Vehicle, Integer> mapDesiredNumbers,
                                     TrafficType trafficType, String name){
        List<Station> stationList = new ArrayList<>(stationsOfPlannedTrafficLine);
        TrafficLine trafficLine = new TrafficLine(model, trafficType, stationList, mapDesiredNumbers, name);
        trafficPartOfPlannedTrafficLine.addTrafficLine(trafficLine);

        //Lösche geplante TrafficLine wieder aus dem Controller, da sie jetzt erstellt wurde
        clearPlannedTrafficLine();

        //Immer wenn eine neue Verkehrslinie hinzugefügt wurde, sollen die Stationen im Graph der TrafficLines eingetragen werden,
        //der zum Warentransport verwendet wird
        TrafficLineGraph trafficLineGraph = model.getMap().getTrafficLineGraph();
        trafficLineGraph.generateEntriesFromStationList(stationList);
    }

    /**
     * Generiert aus einer Map von Fahrzeugnamen und den gewünschten Anzahlen eine Map aus Fahrzeugen und den gewünschten
     * Anzahlen
     * @param desiredNumbersOfVehicleNames
     * @return
     */
    public Map<Vehicle, Integer> getVehicleMapOfDesiredNumbers(Map<String, Integer> desiredNumbersOfVehicleNames) {
        Map<Vehicle, Integer> mapDesiredNumbers = new HashMap<>();
        for (Map.Entry<String, Integer> entry : desiredNumbersOfVehicleNames.entrySet()) {
            Vehicle vehicle = model.getVehicleTypeForName(entry.getKey());
            mapDesiredNumbers.put(vehicle, entry.getValue());
        }
        return mapDesiredNumbers;
    }

    public void clearPlannedTrafficLine(){
        stationsOfPlannedTrafficLine.clear();
        this.trafficPartOfPlannedTrafficLine = null;
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

    public Locale getLocale() {
        return resourceBundle.getLocale();
    }

    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }
}
