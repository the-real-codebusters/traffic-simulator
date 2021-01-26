package controller;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import model.*;
import view.MenuPane;
import view.View;

import java.util.Map;


public class Controller {
    private View view;
    private BasicModel model;
    private Building selectedBuilding;


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
        setCanvasEvents(view.getCanvas());

    }


    private void setCanvasEvents(Canvas canvas) {

        // Holt sich sozusagen das ausgewählte Building zum aktuellen Zeitpunkt
        canvas.addEventHandler(MouseEvent.MOUSE_MOVED, mouseEvent -> {
            MenuPane menuPane = view.getMenuPane();
            selectedBuilding = menuPane.getSelectedBuilding();
        });


        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {

            if (event.getButton().compareTo(MouseButton.SECONDARY) == 0) {
                selectedBuilding = null;
                view.drawMap();
            } else if (
                    event.getButton().compareTo(MouseButton.PRIMARY) == 0 &&
                            selectedBuilding != null) {
                managePlacement(event);
            }
        });


        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, dragEvent -> {

            if (dragEvent.getButton().compareTo(MouseButton.PRIMARY) == 0 &&
                    selectedBuilding != null) {
                managePlacement(dragEvent);
            }
        });
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

        Building selectedBuilding = view.getMenuPane().getSelectedBuilding();
        System.out.println(selectedBuilding);

        String originalBuildingName = selectedBuilding.getBuildingName();

        if (model.getMap().canPlaceBuilding(xCoord, yCoord, selectedBuilding)) {

            if (selectedBuilding instanceof Road) {
                checkCombines(xCoord, yCoord);
            }
            model.getMap().placeBuilding(xCoord, yCoord, selectedBuilding);
            selectedBuilding.setBuildingName(originalBuildingName);

            if(selectedBuilding instanceof PartOfTrafficGraph){
                model.addPointsToGraph((PartOfTrafficGraph) selectedBuilding, xCoord, yCoord);
            }

            view.drawMap();
        }
    }

    /**
     * Überprüft, ob das zu platzierende Straßenfeld mit dem ausgewählten Straßenfeld auf der Map Feld kombiniert
     * werden kann. Falls dies der Fall ist, wird das daraus entstehende Feld im Model gespeichert und in der View
     * angezeigt
     *
     * @param xCoord x-Koordinate des angeklickten Feldes, auf das die Straße gebaut werden soll
     * @param yCoord y-Koordinate des angeklickten Feldes, auf das die Straße gebaut werden soll
     */
    public void checkCombines(int xCoord, int yCoord) {

        Tile selectedField = model.getMap().getFieldGrid()[xCoord][yCoord];
        Building buildingOnSelectedTile = selectedField.getBuilding();
        if (buildingOnSelectedTile instanceof Road) {
            Map<String, String> combinations = ((Road) selectedBuilding).getCombines();
            for (Map.Entry<String, String> entry : combinations.entrySet()) {
                if (buildingOnSelectedTile.getBuildingName().equals(entry.getKey())) {
                    String newBuildingName = entry.getValue();

                    System.out.println(selectedBuilding.getBuildingName() + " and " +
                            buildingOnSelectedTile.getBuildingName() + " can be combined to " + newBuildingName);

                    selectedBuilding.setBuildingName(newBuildingName);
                    selectedField.setBuilding(buildingOnSelectedTile);
                    // Wenn eine Kombination einmal gefunden wurde, soll nicht weiter gesucht werden
                    break;

                } else {
                    selectedBuilding.setBuildingName(buildingOnSelectedTile.getBuildingName());
                    selectedField.setBuilding(buildingOnSelectedTile);

                }
            }
        }
    }
}
