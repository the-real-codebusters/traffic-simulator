package model;

import sun.net.NetProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Road extends Building {
    /**
     *  "buildmenu" : "road",
     *       "width" : 1,
     *       "depth" : 1,
     *       "points":{"c" : [0.5, 0.5], "nw" : [0, 0.5]},
     *       "roads":[["nw","c"]],
     *       "dz" : 1,
     *       "combines" : {"road-ne-se-sw":"road-ne-nw-se-sw","road-ne-se":"road-ne-nw-se","road-ne-sw":"road-ne-nw-sw","road-ne":"road-ne-nw","road-se-sw":"road-nw-se-sw","road-se":"road-nw-se","road-sw":"road-nw-sw"}
     */

    // optional
    private String buildmenu;

    //TODO: Himmelsrichtungen (key) als enum definieren
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
        return "Building{" +
                "buildmenu='" + buildmenu + '\'' +
                ", width=" + getWidth() +
                ", depth=" + getDepth() +
                ", points=" + points +
                ", roads=" + roads +
                ", dz=" + getDz() +
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

