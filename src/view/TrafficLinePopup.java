package view;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;
import javafx.stage.Stage;
import model.Station;
import model.TrafficType;

import java.util.ArrayList;
import java.util.List;

public class TrafficLinePopup extends Popup {

    private ListView listView = new ListView();
    private String message;
    private Button readyButton = new Button("ready");
    private TrafficType trafficType;

    public TrafficLinePopup(View view, TrafficType trafficType){
        Stage stage = view.getStage();

//        Label message = new Label("Wähle alle Stationen aus, die zu der Verkehrslinie gehören sollen");
//        // set background
//        message.setStyle(" -fx-background-color: grey; " +
//                "-fx-font: 24 arial;" +
//                "-fx-border-width: 2;\n" +
//                "-fx-border-color: black;");
//
//        // add the label
//        getContent().add(message);
        this.trafficType = trafficType;
        message = "Select all stations for the Traffic Line of type "+trafficType.name();
        readyButton.setOnAction( e -> {
            hide();
            view.getMenuPane().setSelectTrafficLineStationsMode(false);
            ObservableList list = listView.getItems();
            list.remove(list.size()-1);
            list.remove(0);
            list.add(0, "Stations of the Traffic Line:");
            listView.setMaxWidth(200);
            new TrafficLineCreationDialog(view, listView, trafficType);
        });
        setAnchorX(stage.getWidth()+stage.getX());
        setAnchorY(stage.getY());
//        setAnchorLocation(AnchorLocation.CONTENT_BOTTOM_RIGHT);

        listView.setEditable(false);
        showList(new ArrayList<>());

        show(stage);
    }

    private void clearListView(){
        listView.getItems().clear();
        listView.getItems().add(message);
        listView.getItems().add("");
    }

    public void showList(List<Station> stations){
        clearListView();
        for(Station station: stations){
            String displayed = "Station: ID: "+station.getId()+" ";
            System.out.println("displayed Station: "+displayed);
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
