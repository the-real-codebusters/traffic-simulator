package model;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Vehicle {
    protected TrafficType trafficType;
    protected boolean canTransportCargo;
    protected double speed;
    protected String graphic;
    protected Storage storage;

    protected PositionOnTilemap position;
    protected List<Vertex> pathToNextStation = new ArrayList<>();

    protected List<Vertex> pathToNextStationBeforeMovement;

    // Wenn das false ist, fährt das Fahrzeug zurück, also die Liste der Stationen der Verkehrslinie rückwärts ab
    protected boolean movementInTrafficLineGoesForward = true;

    // Die nächste Station, zu der das fahrzeug fahren will. Sozusagen das aktuelle Ziel
    protected Station nextStation;
    protected Pathfinder pathfinder;

    protected boolean hasWaitedInLastRound;

    protected String kind;

    protected TrafficLine trafficLine;

    private long id;

    protected String name = "vehicle "+(int)(Math.random()*100);


    private TransportPackage vehicleTransportPackage;
    private boolean shouldBeRemoved = false;
    private boolean shouldBeRemovedInLastDay = false;

    /**
     * Gibt eine neue Instanz des Fahrzeugs zurück
     * @return
     */
    public Vehicle getNewInstance(){
        Vehicle instance = new Vehicle();
        instance.setTrafficType(trafficType);
        instance.setCanTransportCargo(canTransportCargo);
        instance.setSpeed(speed);
        instance.setGraphic(graphic);
        instance.setKind(kind);
        if(!graphic.equals("the_engine")){
            instance.setStorage(storage.getNewInstance());
        }
        return instance;
    }

    /**
     * Speichert den Weg zur nächsten Station ab
     * @param startVertex
     */
    public void savePathToNextStationAndUpdateMovement(Vertex startVertex, VehicleMovement movement){
        pathToNextStation = pathfinder.findPathToDesiredStation(nextStation, startVertex, trafficType);
        shouldBeRemoved = false;
        if(pathToNextStation.size() == 0){
            System.out.println("Kein neuer Weg gefunden");
            //Kein Weg gefunden
//            trafficLine.getVehicles().remove(this);
            shouldBeRemoved = true;
//            TrafficLine line = nextStation.getTrafficLineForTrafficType(trafficType);
//            line.getVehicles().remove(this);
            movement.setLastMovementBeforeRemove(true);
        }
    }

    /**
     * Speichert den Weg zur nächsten Station ab
     * @param startVertex
     */
    public void savePathToNextStation(Vertex startVertex){
        pathToNextStation = pathfinder.findPathToDesiredStation(nextStation, startVertex, trafficType);
//        System.out.println("pathToNextStation in savePathToNextStation: "+pathToNextStation);
    }

    public void updateNextStation() {

        if(vehicleTransportPackage != null){
            System.out.println("vehicleTransportPackage "+vehicleTransportPackage.toString());
            System.out.println("vehicle package path size "+vehicleTransportPackage.getPath().size());
            System.out.println("nextStation vehicle "+nextStation.getId());
            if(vehicleTransportPackage.getPath().size() > 0){
                System.out.println("next station for package"+vehicleTransportPackage.getNextStationForTransport().getId());
            }
        }


        if (vehicleTransportPackage != null && nextStation.equals(vehicleTransportPackage.getNextStationForTransport())) {
            deliverTransportPackage();
        }
        if (vehicleTransportPackage == null) {
            collectTransportPackage();
        }

//        TrafficLine line = nextStation.getTrafficLineForTrafficType(trafficType);
        nextStation = trafficLine.getNextStation(nextStation, movementInTrafficLineGoesForward, this);
//        nextStation = line.getNextStation(nextStation, movementInTrafficLineGoesForward, this);

    }

    /**
     * Gibt die Positionen des Fahrzeugs innerhalb des nächsten Tags zurück. Entfernt die abgefahrenen Knoten aus
     * pathToNextStation
     * @return Ein VehicleMovement Objekt
     */
    public VehicleMovement getMovementForNextDay(){
        shouldBeRemovedInLastDay = shouldBeRemoved;

        pathToNextStationBeforeMovement = new ArrayList<>(pathToNextStation);
        // Pro Tag sollen so viele Tiles zurückgelegt werden, wie in speed steht
        double wayToGo = speed;

        if(pathToNextStation.size() > 0 && isAirmovementInNearOfStation()){
            wayToGo = 0.8;
        }

        // Die Bewegung startet an der aktuellen Position
        PositionOnTilemap currentPosition = position;
        VehicleMovement vehicleMovement = new VehicleMovement(currentPosition, graphic, false, trafficType, this);
        double distanceToNextVertex = 0;
        // Solange der zur Verfügung stehende Weg an dem tag noch nicht verbraucht ist und solange es noch Wegstrecke
        // in pathToNextStation gibt, soll dem vehicleMovement ein Paar aus der nächsten Position, also dem angefahrenen
        // Knoten, und der Länge des Wegs zu diesem Knoten mitgegeben werden
        while(wayToGo > 0 && pathToNextStation.size() > 0){
            Vertex nextVertex = pathToNextStation.remove(0);
            //TODO Was wenn letzter Knoten aus pathToNextStation erreicht? Am Ziel?

            //Teste, ob Knoten noch im Graph. Teste außerdem, ob Knoten noch eine Verbindung zum nächsten Knoten hat
            // Wenn nicht wurde Straße/Schiene verändert
            TrafficGraph graph = pathfinder.getGraphForTrafficType(trafficType);
            boolean vertexContained = graph.getMapOfVertexes().containsValue(nextVertex);
            boolean hasEdge = true;
            if(vertexContained && pathToNextStation.size() > 0){
                Vertex firstElementInPathToNextStation = pathToNextStation.remove(0);
                hasEdge = graph.hasBidirectionalEdge(nextVertex.getName(), firstElementInPathToNextStation.getName());
                pathToNextStation.add(0, firstElementInPathToNextStation);
            }
            if(!vertexContained || !hasEdge){
                System.out.println("Fehlende Straße/Schiene entdeckt");
                savePathToNextStationAndUpdateMovement(pathToNextStationBeforeMovement.get(0), vehicleMovement);
                return vehicleMovement;
            }


            distanceToNextVertex = currentPosition.getDistanceToPosition(nextVertex);
            vehicleMovement.appendPairOfPositionAndDistance(nextVertex, distanceToNextVertex);
            currentPosition = nextVertex;
            wayToGo -= distanceToNextVertex;
//            System.out.println("pathToNextStation for vehicle "+graphic+" in while loop");
//            pathToNextStation.forEach((x) -> System.out.println(x.getName()));
        }

//        System.out.println("Movement after while loop: ");
//        for(PositionOnTilemap p: vehicleMovement.getAllPositions()){
//            System.out.println(p.coordsRelativeToMapOrigin());
//        }

//        System.out.println("wayToGo in getMovementForNextDay "+wayToGo);


        if(pathToNextStation.size() == 0 && wayToGo >= 0){
            // Station ist erreicht
            updateNextStation();
            savePathToNextStationAndUpdateMovement((Vertex) currentPosition, vehicleMovement);
//            System.out.println("Movement after method at last vertex to next station: ");
//            for(PositionOnTilemap p: vehicleMovement.getAllPositions()){
//                System.out.println(p.coordsRelativeToMapOrigin());
//            }
            return vehicleMovement;
        }
        // Ansonsten wurde die Zielstation nicht erreicht

        // Wenn wayToGo dann negativ ist, also nicht genug Weg bis zum nächsten Knoten vorhanden war, muss der
        // letzte Knoten aus dem VehicleMovement wieder entfernt werden und stattdessen eine Position anteilig des übrigen
        // Weges in Richtung des nächsten Knotens hinzugefügt werden

        if(wayToGo<0){
            wayToGo+=distanceToNextVertex;
            pathToNextStation.add(0, (Vertex) currentPosition);
            vehicleMovement.removeLastPair();

//            System.out.println("wayToGo in getMovementForNextDay "+wayToGo);

            PositionOnTilemap previouslyLastPosition;
            if(vehicleMovement.getNumberOfPoints() == 0){
//                System.out.println("way to go "+wayToGo);
//                System.out.println("path to next station "+pathToNextStation);
                previouslyLastPosition = vehicleMovement.getStartPosition();
            }
            else previouslyLastPosition = vehicleMovement.getLastPair().getKey();
            VehiclePosition lastPosition = previouslyLastPosition.
                    getnewPositionShiftedTowardsGivenPointByGivenDistance(
                            currentPosition.coordsRelativeToMapOrigin(), wayToGo);

            vehicleMovement.appendPairOfPositionAndDistance(lastPosition, wayToGo);
        }
        else if(wayToGo > 0){
            throw new RuntimeException("wayToGo in getMovementForNextDay() was >0 : "+wayToGo);
        }

//        System.out.println("Movement after method: ");
//        for(PositionOnTilemap p: vehicleMovement.getAllPositions()){
//            System.out.println(p.coordsRelativeToMapOrigin());
//        }
        return vehicleMovement;
    }


    private void deliverTransportPackage(){
            System.out.println("deliverTransportPackage called; path "+vehicleTransportPackage.getPath().size());
//        vehicleTransportPackage.getPath().remove(0);

        nextStation.addTransportPackage(vehicleTransportPackage);
            vehicleTransportPackage = null;

    }

    public void collectTransportPackage(){
        List<TransportPackage> stationPackages = nextStation.getStoredPackages();
        for (TransportPackage transportPackage : stationPackages){
            String packageCommodity = transportPackage.getCommodity();
            if (this.storage.getMaxima().containsKey(packageCommodity)){
//                TrafficLine line = nextStation.getTrafficLineForTrafficType(trafficType);
                if(trafficLine.getStations().contains(transportPackage.getNextStationForTransport())) {
                    if (hasEnoughCargoCapacity(transportPackage)) {
                        this.vehicleTransportPackage = transportPackage;
                    } else {
                        Map<String, TransportPackage> splitPackages = splitPackage(transportPackage);
                        this.vehicleTransportPackage = splitPackages.get("vehiclePackage");
                        nextStation.addTransportPackage(splitPackages.get("stationPackage"));
                    }
                    nextStation.getStoredPackages().remove(transportPackage);
                    vehicleTransportPackage.getPath().remove(0);
                    System.out.println("vehicle "+this.getGraphic()+" hat package genommen: "+vehicleTransportPackage);
                    break;
                }
            }
            else {
                System.out.println("vehicle "+this.getGraphic()+" hat package "+transportPackage+" nicht aufgenommen");
            }
        }
    }

    private Map<String, TransportPackage> splitPackage(TransportPackage transportPackage){
        System.out.println("package splitted");
        int capacity = getCapacityForCommodity(transportPackage.getCommodity());
        TransportPackage vehiclePackage = new TransportPackage(transportPackage.getProducerFactory(),
                transportPackage.getConsumerFactory(), transportPackage.getCommodity(), capacity);

        vehiclePackage.getPath().addAll(transportPackage.getPath());

        int remainingAmount = transportPackage.getAmount() - capacity;
        TransportPackage stationPackage = new TransportPackage(transportPackage.getProducerFactory(),
                transportPackage.getConsumerFactory(), transportPackage.getCommodity(), remainingAmount);

        stationPackage.getPath().addAll(transportPackage.getPath());

        Map<String, TransportPackage> splitPackages = new HashMap<>();
        splitPackages.put("vehiclePackage", vehiclePackage);
        splitPackages.put("stationPackage", stationPackage);
        return splitPackages;
    }

    private boolean hasEnoughCargoCapacity(TransportPackage transportPackage){
        return storage.getMaxima().get(transportPackage.getCommodity()) >= transportPackage.getAmount();
    }

    private int getCapacityForCommodity(String commodity){
        return storage.getMaxima().get(commodity);
    }

    public void revertMovementForNextDay(){
        pathToNextStation = pathToNextStationBeforeMovement;
    }

    /**
     * Lädt eine Ware commodity mit der Menge amount ein. Wenn mehr Waren eingeladen werden sollen als Platz vorhanden
     * ist, wird der Rest der Ware vernichtet.
     * @param commodity
     * @param amount
     * @return
     */
    public void loadCommodity(String commodity, int amount){
        if(amount == 0) return;
        else if(amount < 0) throw new IllegalArgumentException("Argument amount in loadCommodity() was "+amount);

        for(Integer currentAmount: storage.getCargo().values()){
            if(currentAmount != 0) throw new RuntimeException("storage of a vehicle should be empty when loading, but" +
                    "was not");
        }
        storage.changeCargo(commodity, amount);
    }

    private boolean isAirmovementInNearOfStation(){
        boolean air = trafficType.equals(TrafficType.AIR);
        Vertex nextVertex = pathToNextStation.get(0);
        double distanceToNextVertex = nextVertex.coordsRelativeToMapOrigin().distance(position.coordsRelativeToMapOrigin());

        return air && distanceToNextVertex <= 1;
    }

    /**
     * Lädt die Ware aus und gibt sie zurück.
     * @return Null, wenn keine ladung an Bord war. Ansonsten ein Pair von ausgeladener Ware und Menge der Ware
     */
    public Pair<String, Integer> unloadCommodity(){
        int numberOfLoadedCommodities = 0;
        for(Integer currentAmount: storage.getCargo().values()){
            if(currentAmount > 0) numberOfLoadedCommodities++;
        }

        if(numberOfLoadedCommodities == 0){
            return null;
        }
        else if(numberOfLoadedCommodities > 1) throw new IllegalStateException("In a vehicle was more than one different " +
                "commodity. That should not be the case");
        else {
            String unloadedCommodity = "";
            Integer unloadedAmount = 0;
            Map<String, Integer> cargo = storage.getCargo();
            for(String commodity: cargo.keySet()){
                Integer amount = cargo.get(commodity);
                if(amount > 0) {
                    unloadedAmount = amount;
                    unloadedCommodity = commodity;
                    break;
                }
            }
            return new Pair(unloadedCommodity, unloadedAmount);
        }
    }

    public TrafficType getTrafficType() {
        return trafficType;
    }

    public void setTrafficType(TrafficType trafficType) {
        this.trafficType = trafficType;
    }

    public boolean isCanTransportCargo() {
        return canTransportCargo;
    }

    public void setCanTransportCargo(boolean canTransportCargo) {
        this.canTransportCargo = canTransportCargo;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public String getGraphic() {
        return graphic;
    }

    public void setGraphic(String graphic) {
        this.graphic = graphic;
    }

    public Storage getStorage() {
        if (graphic.equals("the_engine")) {
            Map<String, Integer> cargoEmpty = new HashMap<>();
            return new Storage(cargoEmpty);
        }
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public PositionOnTilemap getPosition() {
        return position;
    }

    public void setPosition(PositionOnTilemap position) {
        this.position = position;
    }

    public void setPathfinder(Pathfinder pathfinder) {
        this.pathfinder = pathfinder;
    }

    public boolean isMovementInTrafficLineGoesForward() {
        return movementInTrafficLineGoesForward;
    }

    public void setMovementInTrafficLineGoesForward(boolean movementInTrafficLineGoesForward) {
        this.movementInTrafficLineGoesForward = movementInTrafficLineGoesForward;
    }

    public Station getNextStation() {
        return nextStation;
    }

    public void setNextStation(Station nextStation) {
        this.nextStation = nextStation;
    }

    public boolean isHasWaitedInLastRound() {
        return hasWaitedInLastRound;
    }

    public void setHasWaitedInLastRound(boolean hasWaitedInLastRound) {
        this.hasWaitedInLastRound = hasWaitedInLastRound;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public TrafficLine getTrafficLine() {
        return trafficLine;
    }

    public void setTrafficLine(TrafficLine trafficLine) {
        this.trafficLine = trafficLine;
    }

    public void setShouldBeRemoved(boolean shouldBeRemoved) {
        this.shouldBeRemoved = shouldBeRemoved;
    }

    public String getName() {
        return name;
    }

    public boolean isShouldBeRemoved() {
        return shouldBeRemoved;
    }

    public boolean isShouldBeRemovedInLastDay() {
        return shouldBeRemovedInLastDay;
    }
}
