package view;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class VehicleTypeRow {
    private String information;
    private ImageView image;
    private Integer desiredNumber;

    public VehicleTypeRow(String information, Image image, Integer desiredNumber) {
        this.information = information;
        this.image = new ImageView(image);
        this.desiredNumber = desiredNumber;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public ImageView getImage() {
        return image;
    }

    public void setImage(ImageView image) {
        this.image = image;
    }

    public Integer getDesiredNumber() {
        return desiredNumber;
    }

    public void setDesiredNumber(Integer desiredNumber) {
        this.desiredNumber = desiredNumber;
    }

}
