package view;

import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.StringProperty;

public class VehicleAnimation {
    private DoubleProperty xCoordProperty;
    private DoubleProperty yCoordProperty;
    private Timeline timeline;
    private StringProperty imageName;

    public VehicleAnimation(DoubleProperty xCoordProperty, DoubleProperty yCoordProperty, Timeline timeline, StringProperty imageName) {
        this.xCoordProperty = xCoordProperty;
        this.yCoordProperty = yCoordProperty;
        this.timeline = timeline;
        this.imageName = imageName;
    }


    public DoubleProperty getxCoordProperty() {
        return xCoordProperty;
    }

    public DoubleProperty getyCoordProperty() {
        return yCoordProperty;
    }

    public Timeline getTimeline() {
        return timeline;
    }

    public StringProperty getImageName() {
        return imageName;
    }
}
