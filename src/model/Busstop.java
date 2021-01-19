package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Busstop extends Special {
    private Map<String, List<Double>> points = new HashMap<>();
    private List<List<String>> roads = new ArrayList<>();

    @Override
    public Busstop getNewInstance(){
        Busstop instance = new Busstop();
        setInstanceStandardAttributes(instance);
        instance.setPoints(Map.copyOf(points));
        instance.setRoads(List.copyOf(roads));
        instance.setSpecial(getSpecial());
        return instance;
    }


    public void setPoints(Map<String, List<Double>> points) {
        this.points = points;
    }

    public void setRoads(List<List<String>> roads) {
        this.roads = roads;
    }


    @Override
    public String toString() {
        return super.toString() +" Busstop{" +
                "buildmenu='" + buildmenu + '\'' +
                ", points=" + points +
                ", roads=" + roads +
                '}';
    }
}
