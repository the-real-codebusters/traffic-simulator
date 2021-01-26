package model;

import java.util.List;
import java.util.Map;

public interface PartOfTrafficGraph {
    Map<String, List<Double>> getPoints();
    public String getBuildingName();
    public List<List<String>> getTransportations();

}
