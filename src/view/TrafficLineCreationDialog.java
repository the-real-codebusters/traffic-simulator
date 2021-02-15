package view;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import model.TrafficType;
import model.Vehicle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrafficLineCreationDialog {

    public TrafficLineCreationDialog(View view){

        BorderPane pane = new BorderPane();
        VBox listBox = new VBox();

        Label message = new Label("     Create a new Traffic Line");
        listBox.getChildren().add(message);

        listBox.getChildren().add(new Label(""));

        pane.setPrefWidth(500);
        pane.setPrefHeight(600);

        Insets insets = new Insets(10);

        //Name eingeben
        TextField textField = new TextField("name of Traffic Line");
        listBox.getChildren().add(textField);
        BorderPane.setMargin(listBox, insets);
        pane.setTop(listBox);

        VBox centerBox = new VBox();
        BorderPane.setMargin(centerBox, insets);
        Label m2 = new Label("     Select Vehicles:");
        centerBox.getChildren().add(m2);

        TrafficType type = TrafficType.ROAD;

        ComboBox<Map<String, Object>> dropdown = new ComboBox<>();

        List<Map<String, Object>> dropdownLabels = new ArrayList<>();
        List<Vehicle> vehicleTypes = view.getController().getVehicleTypesForTrafficType(type);
        for(Vehicle v : vehicleTypes){
            String info = "name: "+v.getGraphic()+"   speed: "+v.getSpeed()+"   cargo: ";
            Map<String, Integer> cargos = v.getStorage().getMaxima();
            for (Map.Entry<String, Integer> entry : cargos.entrySet()) {
                info+= entry.getKey()+": "+entry.getValue()+" ";
            }
//            Label txtImg = new Label(info);
            String imageName = view.getObjectToImageMapping().getImageNameForObjectName(v.getGraphic()+"-nw");
            Image img = view.getResourceForImageName(imageName, 30, view.getImageNameToImageRatio().get(imageName)*30);
//            txtImg.setGraphic(new ImageView(img));
            Map<String, Object> map = new HashMap<>();
            map.put("text", info);
            map.put("image", new ImageView(img));
            dropdownLabels.add(map);
        }
        dropdown.setEditable(false);
        dropdown.setItems(FXCollections.observableList(dropdownLabels));
        setDropdownCellfactory(dropdown);
        centerBox.getChildren().add(dropdown);
        pane.setCenter(centerBox);
        Scene scene = new Scene(pane);

        Stage window = new Stage();
        window.setScene(scene);

        window.setTitle("New Traffic Line");

        window.show();
    }

    private void setDropdownCellfactory(ComboBox dropdown){
        dropdown.setCellFactory(
                new Callback<ListView<Map<String, Object>>, ListCell<Map<String, Object>>>() {
                    @Override public ListCell<Map<String, Object>> call(ListView<Map<String, Object>> p) {
                        return new ListCell<Map<String, Object>>() {
                            private final Label lbl;
                            {
//                                setContentDisplay(ContentDisplay.CENTER);
                                lbl = new Label();
                            }

                            @Override protected void updateItem(Map<String, Object> item, boolean empty) {
                                super.updateItem(item, empty);

                                if (item == null || empty) {
                                    setGraphic(null);
                                } else {
                                    lbl.setText((String) item.get("text"));
                                    lbl.setGraphic((ImageView) item.get("image"));
                                    setGraphic(lbl);
                                }
                            }
                        };
                    }
                });

        dropdown.setConverter(new StringConverter<Map<String, Object>>() {
            @Override
            public String toString(Map<String, Object> map) {
                if (map == null){
                    return null;
                } else {
                    return (String) map.get("text") +";"+ ((ImageView)map.get("image")).getImage().getUrl();
                }
            }

            @Override
            public Map<String, Object> fromString(String string) {
                HashMap<String, Object> map = new HashMap<>();
                String[] parts = string.split(";");

                map.put("text", parts[0]);
                map.put("image", "bildchen");
                return map;

            }
        });
    }


}
