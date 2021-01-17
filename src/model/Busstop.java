package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Busstop extends Special {
    private Map<String, List<Double>> points = new HashMap<>();
    private List<List<String>> roads = new ArrayList<>();

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
