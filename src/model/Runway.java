package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Runway extends Special {
    private String buildmenu = "airport";
    private List<String> entry = new ArrayList<>();

    private Map<String, List<Double>> points = new HashMap<>();
    private List<List<String>> planes = new ArrayList<>();

    public String getBuildmenu() {
        return buildmenu;
    }

    public void setEntry(List<String> entry) {
        this.entry = entry;
    }

    public void setPoints(Map<String, List<Double>> points) {
        this.points = points;
    }

    public void setPlanes(List<List<String>> planes) {
        this.planes = planes;
    }
}
