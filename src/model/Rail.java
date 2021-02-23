package model;

import java.util.*;
import java.util.stream.Collectors;

public class Rail extends Building implements PartOfTrafficGraph{

    private Map<String, List<Double>> points = new HashMap<>();
    private List<List<String>> rails = new ArrayList<>();
    // optional. Wenn Railsignal, dann hat es signals
    private List<String> signals = new ArrayList<>();

    // optional
    private Map<String, String> combines = new HashMap<>();
    private ConnectedTrafficPart associatedPartOfTraffic;
    private Set<Vertex> vertices = new HashSet<>();


    @Override
    public Rail getNewInstance(){
        Rail instance = new Rail();
        setInstanceStandardAttributes(instance);
        instance.setPoints(Map.copyOf(points));
        instance.setRails(List.copyOf(rails));
        instance.setSignals(List.copyOf(signals));
        instance.setCombines(Map.copyOf(combines));
        instance.setTrafficType(TrafficType.RAIL);
        return instance;
    }

    public Map<String, String> getCombines() {
        return combines;
    }

    public void setCombines(Map<String, String> combines) {
        this.combines = combines;
    }

    public void setPoints(Map<String, List<Double>> points) {
        this.points = points;
    }

    public Map<String, List<Double>> getPoints() {
        return points;
    }

    public void setRails(List<List<String>> rails) {
        this.rails = rails;
    }

    public List<List<String>> getRails() {
        return rails;
    }

    public List<List<String>> getTransportations() {
        return rails;
    }

    public void setSignals(List<String> signals) {
        this.signals = signals;
    }

    public List<String> getSignals() {
        return signals;
    }

    @Override
    public ConnectedTrafficPart getAssociatedPartOfTraffic() {
        return associatedPartOfTraffic;
    }

    @Override
    public Set<Vertex> getVertices() {
        return vertices;
    }

    @Override
    public void setAssociatedPartOfTraffic(ConnectedTrafficPart associatedPartOfTraffic) {
        this.associatedPartOfTraffic = associatedPartOfTraffic;
    }

    @Override
    public String toString() {
        return super.toString() +" Rail{" +
                "buildmenu='" + buildmenu + '\'' +
                ", points=" + points +
                ", rails=" + rails +
                ", signals=" + signals +
                '}';
    }
}

