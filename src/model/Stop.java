package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ein Teil einer Haltestelle
 */
public class Stop extends Special implements PartOfTrafficGraph{
    protected Map<String, List<Double>> points = new HashMap<>();
    protected List<List<String>> transportations = new ArrayList<>();
    private Station station;
    private List<Vertex> vertices = new ArrayList<>();

    @Override
    public Stop getNewInstance(){
        Stop instance = new Stop();
        setInstanceStandardAttributes(instance);
        instance.setPoints(Map.copyOf(points));
        instance.setTransportations(List.copyOf(transportations));
        instance.setSpecial(getSpecial());
        instance.setTrafficType(getTrafficType());
        return instance;
    }

    public Map<String, List<Double>> getPoints() {
        return points;
    }

    public void setPoints(Map<String, List<Double>> points) {
        this.points = points;
    }

    public void setTransportations(List<List<String>> roads) {
        this.transportations = roads;
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    @Override
    public List<List<String>> getTransportations() {
        return transportations;
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
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
