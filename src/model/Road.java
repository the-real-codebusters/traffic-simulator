package model;

import java.util.*;
import java.util.stream.Collectors;

public class Road extends Building implements PartOfTrafficGraph{


    private Map<String, List<Double>> points = new HashMap<>();
    private List<List<String>> roads = new ArrayList<>();
    // optional
    private Map<String, String> combines = new HashMap<>();
    private Set<Vertex> vertices = new HashSet<>();
    private ConnectedTrafficPart associatedPartOfTraffic;


    @Override
    public Road getNewInstance(){
        Road instance = new Road();
        setInstanceStandardAttributes(instance);
        instance.setPoints(Map.copyOf(points));
        instance.setCombines(Map.copyOf(combines));
        instance.setRoads(roads);
        instance.setTrafficType(TrafficType.ROAD);
        return instance;
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

    public Map<String, List<Double>> getPoints() {
        return points;
    }

    public List<List<String>> getRoads() {
        return roads;
    }

    public List<List<String>> getTransportations() {
        return roads;
    }

    public Set<Vertex> getVertices() {
        return vertices;
    }

    public void setVertices(Set<Vertex> vertices) {
        this.vertices = vertices;
    }

    public Map<String, String> getCombines() {
        return combines;
    }

    @Override
    public ConnectedTrafficPart getAssociatedPartOfTraffic() {
        return associatedPartOfTraffic;
    }

    public void setAssociatedPartOfTraffic(ConnectedTrafficPart associatedPartOfTraffic) {
        this.associatedPartOfTraffic = associatedPartOfTraffic;
    }

    @Override
    public String toString() {

        return super.toString() + " Road{" +
                "buildmenu='" + this.getBuildmenu() + '\'' +
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

