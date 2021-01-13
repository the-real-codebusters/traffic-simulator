package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Railstation extends Special {
    private String buildmenu = "rail";
    private Map<String, List<Double>> points = new HashMap<>();
    private List<List<String>> rails = new ArrayList<>();

    public void setPoints(Map<String, List<Double>> points) {
        this.points = points;
    }

    public void setRails(List<List<String>> rails) {
        this.rails = rails;
    }

    public String getBuildmenu() {
        return buildmenu;
    }
}
