package model;

import java.util.List;
import java.util.Map;

/**
 * Ein Interface für alle Gebäude, der Verkehrsinfrakstruktur Punkte hinzufügen
 */
public interface PartOfTrafficGraph {
    Map<String, List<Double>> getPoints();
    public String getBuildingName();
    public List<List<String>> getTransportations();
    TrafficType getTrafficType();
}
