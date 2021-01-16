package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Road extends Building {

    // optional
    private String buildmenu;


    private Map<String, List<Double>> points = new HashMap<>();
    private List<List<String>> roads = new ArrayList<>();
    // optional
    private Map<String, String> combines = new HashMap<>();


    public void setBuildmenu(String buildmenu) {
        this.buildmenu = buildmenu;
    }

    public void setPoints(Map<String, List<Double>> points) {
        this.points = points;
    }

    public void setRoads(List<List<String>> roads) {
        this.roads = roads;
    }

    public void setCombines(Map<String, String> combines) {
        this.combines = combines;
    }


    @Override
    public String toString() {

        return super.toString() + " Road{" +
                "buildmenu='" + buildmenu + '\'' +
                ", points=" + points +
                ", roads=" + roads +
                ", combines=" + convertMap(combines) +
                '}';
    }

    private String convertMap(Map<String, String> map) {
        String mapAsString = map.keySet().stream()
                .map(key -> key + "=" + map.get(key))
                .collect(Collectors.joining(", ", "{", "}"));
        return mapAsString;
    }
}

