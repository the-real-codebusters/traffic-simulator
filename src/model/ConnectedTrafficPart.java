package model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Stellt einen Teil des Verkehrsnetzes dar, der verbunden ist und von einem bestimmten TrafficType ist
 */
public class ConnectedTrafficPart {

    private List<Station> stations = new ArrayList<>();
    private BasicModel model;
    private TrafficType trafficType;
    private List<TrafficLine> trafficLines = new ArrayList<>();

    public ConnectedTrafficPart(BasicModel model, TrafficType trafficType, Station firstStation) {
        this.model = model;
        this.trafficType = trafficType;
        stations.add(firstStation);
        firstStation.setTrafficPartForTrafficType(this, trafficType);
        setAssociatedTrafficPartInEveryBuilding();
        if(trafficType.equals(TrafficType.RAIL)){
            Vertex oneVertexOfRailblock = firstStation.getSomeVertexForTrafficType(TrafficType.RAIL);
            Set<Vertex> verticesOfRailblock = model.getPathfinder().findAllConnectedVerticesUntilSignal(oneVertexOfRailblock);

            System.out.println("verticesOfRailblock in ConnectedTrafficPart: ");
            verticesOfRailblock.forEach((x) -> System.out.println(x.getName()));

            Railblock railblock = new Railblock();
            railblock.addVertices(verticesOfRailblock);
        }
    }

    public boolean checkIfMoreThanOneStation(){
        return stations.size() >= 2;
    }

    /**
     * Fügt eine Station hinzu und setzt alle direkt verbundenen Stationen.
     * Updatet außerdem den Anfangsknoten für neue Fahrzeuge, falls nötig.
     * Sortiert die Stationen in stations außerdem.
     * @param station
     */
    public void addStationAndUpdateConnectedStations(Station station){
        System.out.println("addStation called");
        stations.add(station);
        station.updateDirectlyConnectedStations(trafficType);
        station.setTrafficPartForTrafficType(this, trafficType);
        setAssociatedTrafficPartInEveryBuilding();
    }

    //TODO Wenn nur Straßen hinzugefügt werden ohne Station, geht das mit dem draufklicken und anzeigen des
    // ConnectedTrafficPart noch nicht

    public void setAssociatedTrafficPartInEveryBuilding(){
        System.out.println("setAssociatedTrafficPartInEveryBuilding() called" );
        if(!trafficType.equals(TrafficType.AIR)){
            Vertex someVertex = stations.get(0).getSomeVertexForTrafficType(trafficType);
            Set<PartOfTrafficGraph> associatedBuildings = model.getPathfinder().findAllConnectedBuildings(someVertex, trafficType);
            for(PartOfTrafficGraph aB: associatedBuildings){
                aB.setAssociatedPartOfTraffic(this);
            }
        }
        else {
            for(Station station : stations){
                for(Stop stop : station.getComponents()){
                    stop.setAssociatedPartOfTraffic(this);
                }
            }
        }
    }




    /**
     * Fügt diesen Verkehrsteil und den angegebenen Verkehrsteil zu einem Verkehrsteil zusammen
     * @param otherPart
     */
    public void mergeWithTrafficPart(ConnectedTrafficPart otherPart){
        if(!otherPart.getTrafficType().equals(trafficType)) throw new IllegalArgumentException("Tried to merge parts " +
                "of different trafficTypes");
        System.out.println("Stations "+stations);
        for(Station otherStation: otherPart.getStations()){
            otherStation.setTrafficPartForTrafficType(this, trafficType);
            otherStation.updateDirectlyConnectedStations(trafficType);
            stations.add(otherStation);
        }
        System.out.println("Stations "+stations);
        trafficLines.addAll(otherPart.getTrafficLines());
        setAssociatedTrafficPartInEveryBuilding();
    }

    public void addTrafficLine(TrafficLine trafficLine) {
        trafficLines.add(trafficLine);
        trafficLine.setStartVertexAndStartStationForNewVehicles();
    }
//=======
//    public void mergeWithAirTrafficPart(ConnectedTrafficPart otherPart){
//        if(!otherPart.getTrafficType().equals(trafficType)) throw new IllegalArgumentException("Tried to merge parts " +
//                "of different trafficTypes");
//        System.out.println("Stations "+stations);
//        for(Station otherStation: otherPart.getStations()){
//            //TODO: gleiche Stationen nicht miteinander verbinden
//            if (otherPart.getStations().size() > this.getStations().size()) {
//                if (otherStation != this.getStations().get(0)) {
//                    otherStation.setAirTrafficPart(this);
//                    otherStation.updateDirectlyConnectedStationsForRunway(this.getStations().get(0));
//                }
//            }
//            else {
//                otherStation.setAirTrafficPart(this);
//                otherStation.updateDirectlyConnectedStationsForRunway(this.getStations().get(1));
//            }
//
//            //stations.add(otherStation);
//
//        }
//        otherPart.getTrafficLines().add(new TrafficLine(model, TrafficType.AIR));
//        System.out.println("Stations "+stations);
//        trafficLines.addAll(otherPart.getTrafficLines());
//        setAssociatedTrafficPartInEveryBuilding();
//>>>>>>> master

    public void setStations(List<Station> stations) {
        this.stations = stations;
    }

    public List<Station> getStations() {
        return stations;
    }

    public BasicModel getModel() {
        return model;
    }

    public void setModel(BasicModel model) {
        this.model = model;
    }

    public TrafficType getTrafficType() {
        return trafficType;
    }

    public void setTrafficType(TrafficType trafficType) {
        this.trafficType = trafficType;
    }

    public List<TrafficLine> getTrafficLines() {
        return trafficLines;
    }

    public void setTrafficLines(List<TrafficLine> trafficLines) {
        this.trafficLines = trafficLines;
    }
}
