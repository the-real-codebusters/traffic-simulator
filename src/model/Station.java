package model;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

// Entspricht sozusagen einem Waypoint aus Vorprojekt, außerdem dürfen Fahrzeuge auf einer Station halten

public class Station {
    // Bauwerke, aus denen eine Haltestelle besteht (z.B. dürfen ein Bahnhof und eine Bushaltestelle gemeinsam eine
    // Station bilden, um Warenumstieg zu ermöglichen. Eine Station kann aber auch nur aus einem Bauwerk bestehen
    private List<Stop> components = new ArrayList<>();

    private Storage storage;

    private static final AtomicLong NEXT_ID = new AtomicLong(0);
    private final long id = NEXT_ID.getAndIncrement();
    private int maxPlanes = 0;

    //TODO Mehrere TrafficLines für eine Station
    private TrafficLine roadTrafficLine;
    private TrafficLine railTrafficLine;
    private TrafficLine airTrafficLine;

    private ConnectedTrafficPart roadTrafficPart;
    private ConnectedTrafficPart airTrafficPart;
    private ConnectedTrafficPart railTrafficPart;


    private Set<Station> directlyRoadConnectedStations = new HashSet<>();
    private Set<Station> directlyRailConnectedStations = new HashSet<>();
    private Set<Station> directlyAirConnectedStations = new HashSet<>();

    private Pathfinder pathfinder;
    private BasicModel model;

    private Factory nearFactory;
    private int actualSearchLevel;

    private List<TransportPackage> storedPackages = new ArrayList<>();


    // wird verwendet um einen saubere Flugzeugbewegung auf/zwischen den Runways zu ermöglichen
    private boolean visited;
    private Vertex first;
    private Vertex last;

    private Stop terminal;

    // Flugzeuge, die eine Landebahn nicht anfliegen duerfen, weil diese belegt ist
    private Queue<VehicleMovement> airPlanesWaiting = new ArrayDeque<>();


    public Station(BasicModel model, TrafficLine roadTrafficLine, TrafficLine railTrafficLine,
                   TrafficLine airTrafficLine, Pathfinder pathfinder, ConnectedTrafficPart roadTrafficPart,
                   ConnectedTrafficPart railTrafficPart, ConnectedTrafficPart airTrafficPart) {
        // Stations haben unendliche Lagerkapazität
        Map<String, Integer> maximumCargo = new HashMap<>();
        for(String commodity: model.getCommodities()) {
            maximumCargo.put(commodity, Integer.MAX_VALUE);
        }
        storage = new Storage(maximumCargo);

        this.roadTrafficLine = roadTrafficLine;
        this.railTrafficLine = railTrafficLine;
        this.airTrafficLine = airTrafficLine;
        this.pathfinder = pathfinder;
        this.roadTrafficPart = roadTrafficPart;
        this.railTrafficPart = railTrafficPart;
        this.airTrafficPart = airTrafficPart;
        this.model = model;

        model.getMap().getTrafficLineGraph().addStation(this);
    }

    public Station(Pathfinder pathfinder, BasicModel model) {
        // Stations haben unendliche Lagerkapazität
        Map<String, Integer> maximumCargo = new HashMap<>();
        for(String commodity: model.getCommodities()) {
            maximumCargo.put(commodity, Integer.MAX_VALUE);
        }
        storage = new Storage(maximumCargo);

        this.pathfinder = pathfinder;
        this.model = model;

        model.getMap().getTrafficLineGraph().addStation(this);

    }

    /**
     * Gibt zurück, ob die Station mit der angegebenen Station direkt durch Straßen verbunden ist
     * @param station
     * @return
     */
    public boolean isDirectlyConnectedTo(Station station, TrafficType trafficType){

//        System.out.println("directlyRoadConnectedStations in isDirectlyConnectedTo für Station "+id);
//        directlyRoadConnectedStations.forEach((x) -> System.out.println( "verbunden mit Station: "+x.getId()));

        if(trafficType.equals(TrafficType.ROAD)){
            return directlyRoadConnectedStations.contains(station);
        }
        else if(trafficType.equals(TrafficType.RAIL)){
            return directlyRailConnectedStations.contains(station);
        }
        else if(trafficType.equals(TrafficType.AIR)){
            return directlyAirConnectedStations.contains(station);
        }
        else throw new IllegalArgumentException("Argument in isDirectlyConnectedTo was "+trafficType);
    }

    public TrafficLine getTrafficLineForTrafficType(TrafficType trafficType){
        if(trafficType.equals(TrafficType.ROAD)) return roadTrafficLine;
        if(trafficType.equals(TrafficType.RAIL)) return railTrafficLine;
        if(trafficType.equals(TrafficType.AIR)) return airTrafficLine;
        throw new RuntimeException("Unklarer Verkehrstyp in getTrafficLineForTrafficType");
    }

    /**
     * Setzt die Variable directlyConnectedStations für alle verbundenen Stationen.
     */
    public void updateDirectlyConnectedStations(TrafficType trafficType){
        List<Station> nextStations;
        if(trafficType.equals(TrafficType.AIR)){
            //Airstation nur fertig, wenn Runway, Terminal und Tower enthalten ist
            if(! isWholeAirstation()){
                return;
            }

            nextStations = model.getMap().getAllAirStations();
            nextStations.remove(this);
        }
        else {
            // Mache eine Breitensuche auf dem Graph um alle direkt verbundenen Stationen zu finden
            nextStations = pathfinder.findAllDirectlyConnectedStations(this, trafficType);
        }

//        System.out.println("Connected Stations for Station "+this.getId());
        for(Station n: nextStations){
//            System.out.println("Next Station "+n.getId());
            n.getDirectlyConnectedStations(trafficType).add(this);
        }
        setDirectlyConnectedStations(nextStations, trafficType);
        if(trafficType.equals(TrafficType.AIR)){
            setEntryConnections();
        }
    }

    private void setEntryConnections(){
//        System.out.println("setEntryConnections called");
        List<Vertex> ownEntrys = getEntrys();
//        System.out.println("ownEntrys "+ownEntrys);

        List<Vertex> otherEntrys = new ArrayList<>();
            for(Station otherStation : directlyAirConnectedStations){
                otherEntrys.addAll(otherStation.getEntrys());
            }
//        System.out.println("directlyAirConnectedStations "+directlyAirConnectedStations);
//
//        System.out.println("otherEntrys "+otherEntrys);
            TrafficGraph graph = model.getMap().getGraphForTrafficType(TrafficType.AIR);

            for(Vertex ownEntry : ownEntrys){
                for(Vertex otherEntry: otherEntrys){
                    graph.addEdgeBidirectional(ownEntry.getName(), otherEntry.getName());
                }
            }
    }

//    public void updateDirectlyConnectedStationsForRunway(Station start){
//        // Mache eine Breitensuche auf dem Graph um alle direkt verbundenen Stationen zu finden
//        List<Station> nextStations = pathfinder.findPathForPlane(start, this);
//        System.out.println("Connected Stations for Station "+this.getId());
//        for(Station n: nextStations){
//            System.out.println("Next Station "+n.getId());
//            n.getDirectlyConnectedStations().add(this);
//        }
//        setDirectlyConnectedStations(nextStations);
//    }

    /**
     * Fügt der Station eine Haltestelle hinzu und setzt in der Haltestelle diese Station
     * @param building
     * @return
     */
    public boolean addBuildingAndSetStationInBuilding(Stop building, boolean firstStation){
        building.setStation(this);
        if(building.getTrafficType().equals(TrafficType.AIR)){
            if(building instanceof Tower){
                int maxplanes = ((Tower) building).getMaxplanes();
                if(maxplanes > this.maxPlanes) this.maxPlanes = maxplanes;
            }
            else if(building.getSpecial().equals("terminal")){
                terminal = building;
            }
            updateDirectlyConnectedStations(TrafficType.AIR);
        }

        components.add(building);

        if(!firstStation){
            ConnectedTrafficPart trafficPart = getTrafficPartForTrafficType(building.getTrafficType());
            if(trafficPart == null) return false;
            trafficPart.setAssociatedTrafficPartInEveryBuilding();
        }
        return true;
    }

    public boolean isWholeAirstation(){
        int numberRunways = 0;
        int numberTerminals = 0;

        for(Stop c : components){
            if(c instanceof Runway) numberRunways++;
            else if(c.getSpecial().equals("terminal")) numberTerminals++;
        }

        return numberRunways > 0 && numberTerminals > 0 && maxPlanes > 0;
    }

    public List<Vertex> getEntrys(){
        List<Vertex> entrys = new ArrayList<>();
        for(Stop c : components){
            if(c instanceof Runway) {
                String entryName = ((Runway) c).getFirstAndOnlyEntry();
                Vertex entry = null;
                for(Vertex vertex: c.getVertices()){
                    if(vertex.getName().contains(entryName)){
                        entry = vertex;
                    }
                }
                entrys.add(entry);
            }
        }
        return entrys;
    }

    public boolean hasPartOfTrafficType(TrafficType trafficType){
        return getTrafficPartForTrafficType(trafficType) != null;
    }

    public Vertex getSomeVertexForTrafficType(TrafficType trafficType){
        for(Stop stop: components){
            if(stop.getTrafficType().equals(trafficType)){
                return stop.getVertices().iterator().next();
            }
        }
        throw new RuntimeException("Found no Vertex for trafficType "+trafficType);
    }

    public ConnectedTrafficPart getTrafficPartForTrafficType(TrafficType trafficType){
        if(trafficType.equals(TrafficType.AIR)) return airTrafficPart;
        else if(trafficType.equals(TrafficType.ROAD)) return roadTrafficPart;
        else if (trafficType.equals(TrafficType.RAIL)) return railTrafficPart;
        else throw new RuntimeException("TrafficPart in Station "+id+" was not there for traffictype "+trafficType);
    }

    public void setTrafficPartForTrafficType(ConnectedTrafficPart trafficPart, TrafficType trafficType){
        if(trafficType.equals(TrafficType.AIR)) airTrafficPart = trafficPart;
        else if(trafficType.equals(TrafficType.ROAD)) roadTrafficPart = trafficPart;
        else if (trafficType.equals(TrafficType.RAIL)) railTrafficPart = trafficPart;
    }

    public Stop getTerminal() {
        return terminal;
    }

    //Die Methoden equals() und hashCode() gehen davon aus, dass die id einer Station unique ist
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Station station = (Station) o;
        return id == station.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public List<Stop> getComponents() {
        return components;
    }

    public Storage getStorage() {
        return storage;
    }

    public long getId() {
        return id;
    }

    public ConnectedTrafficPart getAirTrafficPart() {
        return airTrafficPart;
    }

    public void setAirTrafficPart(ConnectedTrafficPart airTrafficPart) {
        this.airTrafficPart = airTrafficPart;
    }

    public Set<Station> getDirectlyConnectedStations(TrafficType type) {
        if(type.equals(TrafficType.ROAD)){
            return directlyRoadConnectedStations;
        }
        else if(type.equals(TrafficType.RAIL)){
            return directlyRailConnectedStations;
        }
        else if(type.equals(TrafficType.AIR)){
            return directlyAirConnectedStations;
        }
        throw new IllegalArgumentException("traffic type was "+type);
    }

    public void setDirectlyConnectedStations(List<Station> directlyConnectedStations, TrafficType type) {
        if(type.equals(TrafficType.ROAD)){
            directlyRoadConnectedStations = new HashSet<>(directlyConnectedStations);
        }
        else if(type.equals(TrafficType.RAIL)){
            directlyRailConnectedStations = new HashSet<>(directlyConnectedStations);
        }
        else if(type.equals(TrafficType.AIR)){
            directlyAirConnectedStations = new HashSet<>(directlyConnectedStations);
        }
        else throw new IllegalArgumentException("traffic type was "+type);
    }

    public void addTransportPackage(TransportPackage transportPackage){
        if (nearFactory == transportPackage.getConsumerFactory()){
            nearFactory.getStorage().changeCargo(transportPackage.getCommodity(), transportPackage.getAmount());
        } else {
            storedPackages.add(transportPackage);
        }
    }

    public ConnectedTrafficPart getRoadTrafficPart() {
        return roadTrafficPart;
    }

    public void setRoadTrafficPart(ConnectedTrafficPart roadTrafficPart) {
        this.roadTrafficPart = roadTrafficPart;
    }

    public TrafficLine getRoadTrafficLine() {
        return roadTrafficLine;
    }

    public void setRoadTrafficLine(TrafficLine roadTrafficLine) {
        this.roadTrafficLine = roadTrafficLine;
    }

    public TrafficLine getRailTrafficLine() {
        return railTrafficLine;
    }

    public void setRailTrafficLine(TrafficLine railTrafficLine) {
        this.railTrafficLine = railTrafficLine;
    }

    public TrafficLine getAirTrafficLine() {
        return airTrafficLine;
    }

    public void setAirTrafficLine(TrafficLine airTrafficLine) {
        this.airTrafficLine = airTrafficLine;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public Vertex getLast() {
        return last;
    }

    public void setLast(Vertex last) {
        this.last = last;
    }

    public Vertex getFirst() {
        return first;
    }

    public void setFirst(Vertex first) {
        this.first = first;
    }

    public void addAirPlanesWaiting(VehicleMovement e) {
        airPlanesWaiting.offer(e);
    }

    public void removeAirPlanesWaiting() {
        airPlanesWaiting.poll();
    }
    
    public ConnectedTrafficPart getRailTrafficPart() {
        return railTrafficPart;
    }

    public void setRailTrafficPart(ConnectedTrafficPart railTrafficPart) {
        this.railTrafficPart = railTrafficPart;
    }

    public Factory getNearFactory() {
        return nearFactory;
    }

    public void setNearFactory(Factory nearFactory) {
        this.nearFactory = nearFactory;
    }

    public int getActualSearchLevel() {
        return actualSearchLevel;
    }

    public void setActualSearchLevel(int actualSearchLevel) {
        this.actualSearchLevel = actualSearchLevel;
    }

    public List<TransportPackage> getStoredPackages() {
        return storedPackages;
    }

    public void setStoredPackages(List<TransportPackage> storedPackages) {
        this.storedPackages = storedPackages;
    }
}
