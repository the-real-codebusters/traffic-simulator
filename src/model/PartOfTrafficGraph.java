package model;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Ein Interface für alle Gebäude, der Verkehrsinfrakstruktur Punkte hinzufügen
 */
public interface PartOfTrafficGraph {
    Map<String, List<Double>> getPoints();
    String getBuildingName();
    List<List<String>> getTransportations();
    TrafficType getTrafficType();
    ConnectedTrafficPart getAssociatedPartOfTraffic();
    void setAssociatedPartOfTraffic(ConnectedTrafficPart part);
    Set<Vertex> getVertices();
}
