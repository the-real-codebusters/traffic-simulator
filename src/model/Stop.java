package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Stop extends Special{
    private Map<String, List<Double>> points = new HashMap<>();
    private List<List<String>> transportations = new ArrayList<>();

    @Override
    public Stop getNewInstance(){
        Stop instance = new Stop();
        setInstanceStandardAttributes(instance);
        instance.setPoints(Map.copyOf(points));
        instance.setTransportations(List.copyOf(transportations));
        instance.setSpecial(getSpecial());
        return instance;
    }

    public void setPoints(Map<String, List<Double>> points) {
        this.points = points;
    }

    public void setTransportations(List<List<String>> roads) {
        this.transportations = roads;
    }


    @Override
    public String toString() {
        return super.toString() +" Stop{" +
                "buildmenu='" + buildmenu + '\'' +
                ", points=" + points +
                ", roads=" + transportations +
                '}';
    }
}
