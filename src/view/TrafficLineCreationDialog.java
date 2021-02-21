package view;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import model.Station;
import model.TrafficType;
import model.Vehicle;

import java.awt.*;
import java.util.*;
import java.util.List;

public class TrafficLineCreationDialog {

    private TableView<VehicleTypeRow> tableView;
    private ComboBox<Map<String, Object>> dropdown = new ComboBox<>();
    private TextField numberVehiclesField;
    private final int iconWidth = 30;
    private Stage window;

    public TrafficLineCreationDialog(View view, ListView stations, TrafficType trafficType){

        BorderPane pane = new BorderPane();
        VBox listBox = new VBox();
        listBox.setSpacing(5);

        Label message = new Label("Create a new Traffic Line");
        listBox.getChildren().add(message);

        Label type = new Label("Traffic Type: "+trafficType.name());
        listBox.getChildren().add(type);

        pane.setPrefWidth(750);
        pane.setPrefHeight(600);

        Insets insets = new Insets(10);

        //Name eingeben
        TextField textField = new TextField("name of Traffic Line");
        listBox.getChildren().add(textField);
        BorderPane.setMargin(listBox, insets);
        pane.setTop(listBox);

        VBox centerBox = new VBox();
        centerBox.setSpacing(10);
        BorderPane.setMargin(centerBox, insets);
        Label m2 = new Label("     Select vehicle type and desired number of vehicles:");
        centerBox.getChildren().add(m2);

        dropdown.setMaxWidth(400);

        List<Map<String, Object>> dropdownLabels = new ArrayList<>();
        List<Vehicle> vehicleTypes = view.getController().getVehicleTypesForTrafficType(trafficType);
        System.out.println("Vehicle Types für den TrafficType "+trafficType);
        vehicleTypes.forEach((x) -> System.out.println(x.getGraphic()));
        for(Vehicle v : vehicleTypes){
            String info = "name: "+v.getGraphic()+"   speed: "+v.getSpeed()+"   cargo: ";
            Map<String, Integer> cargos = v.getStorage().getMaxima();
            for (Map.Entry<String, Integer> entry : cargos.entrySet()) {
                info+= entry.getKey()+": "+entry.getValue()+" ";
            }
//            Label txtImg = new Label(info);
            String imageName = view.getObjectToImageMapping().getImageNameForObjectName(v.getGraphic()+"-nw");
            Image img = view.getResourceForImageName(imageName, iconWidth, view.getImageNameToImageRatio().get(imageName)*30);
//            txtImg.setGraphic(new ImageView(img));
            Map<String, Object> map = new HashMap<>();
            map.put("text", info);
            map.put("image", img);
            dropdownLabels.add(map);
        }
        dropdown.setEditable(false);
        dropdown.setItems(FXCollections.observableList(dropdownLabels));
        setDropdownCellfactory(dropdown);
        HBox vehiclesHbox = new HBox();
//        vehiclesHbox.setPadding(new Insets(0, 10, 0, 10));
        vehiclesHbox.setSpacing(5);
        vehiclesHbox.getChildren().add(dropdown);
        numberVehiclesField = getIntegerFormattedTextField();
        vehiclesHbox.getChildren().add(numberVehiclesField);
        vehiclesHbox.getChildren().add(getAddVehicleButton());
        centerBox.getChildren().add(vehiclesHbox);

        tableView = getVehicleTableView();
        centerBox.getChildren().add(tableView);

        Button createButton = new Button("create traffic line");
        createButton.setOnAction( (e) -> {
            Map<String, Integer> mapDesiredNumbers = new HashMap<>();
            for(VehicleTypeRow row: tableView.getItems()){
                String name = row.getInformation().split("name: ")[1].split(" ")[0];
                mapDesiredNumbers.put(name, row.getDesiredNumber());
            }
            System.out.println(mapDesiredNumbers);
            view.getController().createNewTrafficLine(mapDesiredNumbers, trafficType, textField.getText());
            window.close();
        });
        centerBox.getChildren().add(createButton);

        pane.setCenter(centerBox);

        pane.setLeft(stations);

        Scene scene = new Scene(pane);

        window = new Stage();
        window.setScene(scene);

        window.setTitle("New Traffic Line");

        window.setOnCloseRequest((event) -> {
            view.getController().clearPlannedTrafficLine();
        });

        window.show();
    }

    private void setDropdownCellfactory(ComboBox dropdown){
        dropdown.setCellFactory(
                new Callback<ListView<Map<String, Object>>, ListCell<Map<String, Object>>>() {
                    @Override public ListCell<Map<String, Object>> call(ListView<Map<String, Object>> p) {
                        return new IconListCell();
                    }
                });

        dropdown.setConverter(new StringConverter<Map<String, Object>>() {
            @Override
            public String toString(Map<String, Object> map) {
                if (map == null){
                    return null;
                } else {
                    // it was before 21.02.2021
                    // return (String) map.get("text") +";"+ ((ImageView)map.get("image")).getImage().getUrl();
                    return (String) map.get("text") +";"+ ((ImageView)map.get("image")).getImage().impl_getUrl();
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

        dropdown.setButtonCell(new IconListCell());
    }

    private TextField getIntegerFormattedTextField(){
        TextField textField = new TextField();
        textField.setMaxWidth(30);
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            int from = 0;
            int to = 20;
            if (newValue != null && !newValue.equals("")) {
                try {
                    int number = Integer.parseInt(newValue);
                    if (number < from || number > to) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException ignored) {
                    textField.setText(oldValue);
                }
            }
        });
        return  textField;
    }

    private Button getAddVehicleButton(){
        Button button = new Button("add");
        button.setOnAction( (actionEvent -> {
            Map<String, Object> selected = dropdown.getSelectionModel().getSelectedItem();
            Image image = (Image) selected.get("image");
            String infos = (String) selected.get("text");
            String textNumberInput = numberVehiclesField.getText();
            if(textNumberInput != null && !textNumberInput.equals("")){
                Integer numberOfDesiredVehicles = Integer.valueOf(numberVehiclesField.getText());
                VehicleTypeRow row = new VehicleTypeRow(infos, image, numberOfDesiredVehicles);
                tableView.getItems().removeIf(n -> (n.getInformation().equals(infos)));
                if(numberOfDesiredVehicles > 0){
                    tableView.getItems().add(row);
                }
            }
        }));
        return button;
    }

    private TableView<VehicleTypeRow> getVehicleTableView(){
        TableView<VehicleTypeRow> tableView = new TableView();
        tableView.setPrefHeight(500);

        TableColumn<VehicleTypeRow, ImageView> column1 = new TableColumn<>("Icon");
        column1.setCellValueFactory(new PropertyValueFactory<>("image"));
        column1.setPrefWidth(iconWidth+10);

        TableColumn<VehicleTypeRow, String> column2 = new TableColumn<>("Information");
        column2.setCellValueFactory(new PropertyValueFactory<>("information"));
        column2.setPrefWidth(350);

        TableColumn<VehicleTypeRow, Integer> column3 = new TableColumn<>("Desired Number");
        column3.setCellValueFactory(new PropertyValueFactory<>("desiredNumber"));
        column3.setPrefWidth(120);

        tableView.getColumns().addAll(column1, column2, column3);

        return tableView;
    }

//    private ListView getStationsListView(){
//
//    }

}

class IconListCell extends ListCell<Map<String, Object>>{
private final Label lbl;
        {
//                                setContentDisplay(ContentDisplay.CENTER);
        lbl = new Label();
        lbl.setTextFill(Color.BLACK);
        }

@Override public void updateItem(Map<String, Object> item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null || empty) {
        setGraphic(null);
        } else {
        lbl.setText((java.lang.String) item.get("text"));
        lbl.setGraphic(new ImageView((Image)item.get("image")));
        setGraphic(lbl);
        }
        }
        };
