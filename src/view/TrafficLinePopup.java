package view;

import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Popup;
import javafx.stage.Stage;
import model.Station;
import model.TrafficType;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

// Popup das aufgeht, bevor eine TrafficLine erstellt wird und in dem die Stationen
// für die TrafficLine ausgewählt werden sollen
public class TrafficLinePopup extends Popup {

    private final ListView listView = new ListView();
    private final String message;
    private final Button readyButton;
    private final TrafficType trafficType;

    public TrafficLinePopup(View view, TrafficType trafficType){
        Stage stage = view.getStage();

        ResourceBundle resourceBundle = view.getResourceBundleFromController();
        String ready = resourceBundle.getString("ready");
        readyButton = new Button(ready);

        this.trafficType = trafficType;
        String trafficTypeString = resourceBundle.getString(trafficType.name());
        String selectStations = resourceBundle.getString("selectStations");
        message = selectStations + trafficTypeString;

        readyButton.setOnAction( e -> {
            hide();
            view.getMenuPane().setSelectTrafficLineStationsMode(false);
            ObservableList list = listView.getItems();
            list.remove(list.size()-1);
            list.remove(0);
            String stationsForTrafficLine = resourceBundle.getString("stationsOfTrafficLine");
            list.add(0, stationsForTrafficLine);
            listView.setMaxWidth(200);
            new TrafficLineCreationDialog(view, listView, trafficType);
        });
        setAnchorX(stage.getWidth()+stage.getX());
        setAnchorY(stage.getY());

        listView.setEditable(false);
        showList(new ArrayList<>(), resourceBundle);

        show(stage);
    }

    private void clearListView(){
        listView.getItems().clear();
        listView.getItems().add(message);
        listView.getItems().add("");
    }

    public void showList(List<Station> stations, ResourceBundle resourceBundle){
        clearListView();
        for(Station station: stations){
            String stationID = resourceBundle.getString("stationID");
            String displayed = stationID + station.getId()+" ";
            listView.getItems().add(displayed);
        }
        listView.getItems().add(readyButton);
        if(!getContent().contains(listView)){
            getContent().add(listView);
        }
    }

    public TrafficType getTrafficType() {
        return trafficType;
    }
}
