package model;

import java.util.*;

/**
 * Ein Teil einer Haltestelle
 */
public class Stop extends Special implements PartOfTrafficGraph{
    protected Map<String, List<Double>> points = new HashMap<>();
    protected List<List<String>> transportations = new ArrayList<>();
    private Station station;
    private Set<Vertex> vertices = new HashSet<>();
    private ConnectedTrafficPart associatedPartOfTraffic;


    @Override
    public Stop getNewInstance(){
        Stop instance = new Stop();
        setInstanceStandardAttributes(instance);

        // it was before 21.02.2021
        // instance.setPoints(Map.copyOf(points));
        instance.setPoints(new HashMap<>(points));

        // it was before 21.02.2021
        // instance.setTransportations(List.copyOf(transportations));
        instance.setTransportations(new ArrayList<>(transportations));

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

    @Override
    public Set<Vertex> getVertices() {
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
    public ConnectedTrafficPart getAssociatedPartOfTraffic() {
        return associatedPartOfTraffic;
    }

    public void setAssociatedPartOfTraffic(ConnectedTrafficPart associatedPartOfTraffic) {
        this.associatedPartOfTraffic = associatedPartOfTraffic;
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
