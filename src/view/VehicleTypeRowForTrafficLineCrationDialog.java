package view;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

// Klasse zur Anzeige der Fahrzeug-Informationen beim erstellen einer TrafficLine
public class VehicleTypeRowForTrafficLineCrationDialog {
    private String information;
    private ImageView image;
    private Integer desiredNumber;

    public VehicleTypeRowForTrafficLineCrationDialog(String information, Image image, Integer desiredNumber) {
        this.information = information;
        this.image = new ImageView(image);
        this.desiredNumber = desiredNumber;
    }

    public String getInformation() {
        return information;
    }

    public Integer getDesiredNumber() {
        return desiredNumber;
    }

    public ImageView getImage() {
        return image;
    }

    public void setImage(ImageView image) {
        this.image = image;
    }
}
