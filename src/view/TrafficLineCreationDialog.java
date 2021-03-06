package view;

import controller.Controller;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import model.TrafficType;
import model.Vehicle;

import java.util.*;
import java.util.List;

// Klasse für das Fenster zur Erstellung einer TrafficLine
public class TrafficLineCreationDialog {

    private TableView<VehicleTypeRowForTrafficLineCrationDialog> tableView;
    private ComboBox<Map<String, Object>> dropdown = new ComboBox<>();
    private TextField numberVehiclesField;
    private final int iconWidth = 30;
    private Stage window;

    public TrafficLineCreationDialog(View view, ListView stations, TrafficType trafficType){

        BorderPane pane = new BorderPane();
        VBox listBox = new VBox();
        listBox.setSpacing(5);

        // ResourceBundle aus dem die Klasse die sprachspezifischen Einträge bekommen kann
        ResourceBundle resourceBundle = view.getResourceBundleFromController();

        String createNewTrafficLine = resourceBundle.getString("createNewTrafficLine");
        Label message = new Label(createNewTrafficLine);
        listBox.getChildren().add(message);

        Label type = new Label(resourceBundle.getString("trafficType")+ " " + resourceBundle.getString(trafficType.name()));
        listBox.getChildren().add(type);

        pane.setPrefWidth(750);
        pane.setPrefHeight(600);

        Insets insets = new Insets(10);

        //Name eingeben
        String nameOfTrafficLine = resourceBundle.getString("nameOfTrafficLine");
        TextField textField = new TextField(nameOfTrafficLine);
        listBox.getChildren().add(textField);
        BorderPane.setMargin(listBox, insets);
        pane.setTop(listBox);

        VBox centerBox = new VBox();
        centerBox.setSpacing(10);
        BorderPane.setMargin(centerBox, insets);
        String selectVehiclesAndNumber = resourceBundle.getString("selectVehiclesAndNumber");
        Label m2 = new Label(selectVehiclesAndNumber);
        centerBox.getChildren().add(m2);

        dropdown.setMaxWidth(400);

        List<Map<String, Object>> dropdownLabels = new ArrayList<>();
        List<Vehicle> vehicleTypes = view.getController().getVehicleTypesForTrafficType(trafficType);
        String name = resourceBundle.getString("name");
        String speed = resourceBundle.getString("speed");
        String cargo = resourceBundle.getString("cargo");
        for(Vehicle v : vehicleTypes){
            String graphic = resourceBundle.getString(v.getGraphic());
            String info = name +": "+graphic+"   " +speed +": "+v.getSpeed()+"   "+ cargo +": ";
            Map<String, Integer> cargos = v.getStorage().getMaxima();
            for (Map.Entry<String, Integer> entry : cargos.entrySet()) {
                String cargoName = resourceBundle.getString(entry.getKey());
                info+= cargoName+": "+entry.getValue()+" ";
            }

            String imageName = view.getObjectToImageMapping().getImageNameForObjectName(v.getGraphic()+"-nw");
            Image img = view.getResourceForImageName(imageName, iconWidth, view.getImageNameToImageRatio().get(imageName)*30);

            Map<String, Object> map = new HashMap<>();
            map.put("text", info);
            map.put("image", img);
            dropdownLabels.add(map);
        }
        dropdown.setEditable(false);
        dropdown.setItems(FXCollections.observableList(dropdownLabels));
        setDropdownCellfactory(dropdown);
        HBox vehiclesHbox = new HBox();
        vehiclesHbox.setSpacing(5);
        vehiclesHbox.getChildren().add(dropdown);
        numberVehiclesField = getIntegerFormattedTextField();
        vehiclesHbox.getChildren().add(numberVehiclesField);
        vehiclesHbox.getChildren().add(getAddVehicleButton(view));
        centerBox.getChildren().add(vehiclesHbox);

        tableView = getVehicleTableView(view);
        centerBox.getChildren().add(tableView);

        String createTrafficLine = resourceBundle.getString("createTrafficLine");
        Button createButton = new Button(createTrafficLine);
        createButton.setOnAction( (e) -> {
            Map<String, Integer> mapDesiredNumbers = new HashMap<>();
            Controller controller = view.getController();
            for(VehicleTypeRowForTrafficLineCrationDialog row: tableView.getItems()){
                    String nameLocalized = row.getInformation().split(name+": ")[1].split(" ")[0];

                    // namen der Vehicles müssen hier wieder auf Englisch benutzt werden, da an anderen Stellen
                    // im Code wieder darauf zugegriffen wird
                    Locale locale = new Locale("en_US");
                    ResourceBundle bundleEN = ResourceBundle.getBundle("Bundle", locale);
                    String nameEN;
                    nameEN = bundleEN.getString(nameLocalized);
                    mapDesiredNumbers.put(nameEN, row.getDesiredNumber());

            }

            // Map das ein Vehicle auf die gewünschte Anzahl abbildet
            Map<Vehicle, Integer> vehicleMapDesiredNumbers = controller.getVehicleMapOfDesiredNumbers(mapDesiredNumbers);
            boolean isAcceptable = true;
            if(trafficType.equals(TrafficType.RAIL)){
                int numberOfEngines = 0;
                for (Map.Entry<Vehicle, Integer> entry : vehicleMapDesiredNumbers.entrySet()) {
                    if(entry.getKey().getKind().equals("engine")) {
                        numberOfEngines += entry.getValue();
                    }
                }
                if(numberOfEngines != 1){
                    isAcceptable = false;
                }
            }

            if(isAcceptable){
                controller.createNewTrafficLine(vehicleMapDesiredNumbers, trafficType, textField.getText());
                window.close();
            }
            // Dieser Zweig wird ausgeführt, wenn bei Erstellung einer TrafficLine des Typs RAIL keine
            // Lokomotive ausgewählt wird. Der benutzer wird gewarnt und darüber informiert, dass die aktuelle
            // Auswahl nicht möglich ist, da ein Zug immer eine Lokomotive benötigt
            else {
                String selectionNotPossible = resourceBundle.getString("selectionNotPossible");
                String oneTrainOfType = resourceBundle.getString("oneTrainOfType");
                String exactlyOneEngine = resourceBundle.getString("exactlyOneEngine");
                Alert alert = new Alert(Alert.AlertType.WARNING, selectionNotPossible + " " + oneTrainOfType
                        + resourceBundle.getString(TrafficType.RAIL.toString())+ exactlyOneEngine, ButtonType.OK);
                alert.setTitle(resourceBundle.getString("warning"));
                alert.setHeaderText(resourceBundle.getString("warning"));
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                alert.showAndWait();
            }
        });
        centerBox.getChildren().add(createButton);

        pane.setCenter(centerBox);

        pane.setLeft(stations);

        Scene scene = new Scene(pane);

        window = new Stage();
        window.setScene(scene);

        window.setTitle(resourceBundle.getString("newTrafficLine"));

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

    private Button getAddVehicleButton(View view){
        String add = view.getResourceBundleFromController().getString("add");
        Button button = new Button(add);
        button.setOnAction( (actionEvent -> {
            Map<String, Object> selected = dropdown.getSelectionModel().getSelectedItem();
            Image image = (Image) selected.get("image");
            String infos = (String) selected.get("text");
            String textNumberInput = numberVehiclesField.getText();
            if(textNumberInput != null && !textNumberInput.equals("")){
                Integer numberOfDesiredVehicles = Integer.valueOf(numberVehiclesField.getText());
                VehicleTypeRowForTrafficLineCrationDialog row = new VehicleTypeRowForTrafficLineCrationDialog(infos, image, numberOfDesiredVehicles);
                tableView.getItems().removeIf(n -> (n.getInformation().equals(infos)));
                if(numberOfDesiredVehicles > 0){
                    tableView.getItems().add(row);
                }
            }
        }));
        return button;
    }

    private TableView<VehicleTypeRowForTrafficLineCrationDialog> getVehicleTableView(View view){
        TableView<VehicleTypeRowForTrafficLineCrationDialog> tableView = new TableView();
        tableView.setPrefHeight(500);

        TableColumn<VehicleTypeRowForTrafficLineCrationDialog, ImageView> column1 = new TableColumn<>("Icon");
        column1.setCellValueFactory(new PropertyValueFactory<>("image"));
        column1.setPrefWidth(iconWidth+10);

        String information = view.getResourceBundleFromController().getString("information");
        TableColumn<VehicleTypeRowForTrafficLineCrationDialog, String> column2 = new TableColumn<>(information);
        column2.setCellValueFactory(new PropertyValueFactory<>("Information"));
        column2.setPrefWidth(350);

        String desiredNumber = view.getResourceBundleFromController().getString("desiredNumber");
        TableColumn<VehicleTypeRowForTrafficLineCrationDialog, Integer> column3 = new TableColumn<>(desiredNumber);
        column3.setCellValueFactory(new PropertyValueFactory<>("desiredNumber"));
        column3.setPrefWidth(120);

        tableView.getColumns().addAll(column1, column2, column3);

        return tableView;
    }

}

class IconListCell extends ListCell<Map<String, Object>>{
private final Label lbl;
        {
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
