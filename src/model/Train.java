package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Train extends Vehicle {
    private List<Vehicle> wagons = new ArrayList<>();
    private Vehicle engine;
//    private List<VehicleMovement> movementsOfLastDay = new ArrayList<>();
    private final double wagonShift = 1.03;

    private int numberOfVertices = 10;
    private int verticesCounter = 0;
    private BasicModel model;
//    private List<Vertex> pathForFewDays = new ArrayList<>();

    public Train(List<Vehicle> wagons, Vehicle engine, BasicModel model) {
        trafficType = TrafficType.RAIL;
        canTransportCargo = true;
        speed = engine.getSpeed();
        storage = new Storage(new HashMap<>());
        kind = "train";
        this.model = model;

        for(Vehicle wagon: wagons){
            addWagon(wagon);
        }
        setEngine(engine);
    }

    public List<VehicleMovement> getTrainMovementsForNextDay(){
        List<VehicleMovement> movements = new ArrayList<>();
        boolean trainShouldWait = false;
        if(verticesCounter <= numberOfVertices){
//            System.out.println();
            // Dann suche konkreten Weg für einige Tage

            //Erstes Ziel: Zug wartet, wenn konkreter Weg reserviert
            //Angenommen wir haben Liste von nächsten Vertices:
            Map<Vertex, Integer> nextVerticesAndDay = getVerticesForNextTime();
            if(nextVerticesAndDay!= null){
                boolean reserved = false;
                for (Map.Entry<Vertex, Integer> entry : nextVerticesAndDay.entrySet()) {
                    if(entry.getKey().getRailblock().isReservedAtDay(entry.getValue(), this)){
                        System.out.println("Found reservation, train should wait");
                        reserved = true;
                        break;
                    }
                }

                if(reserved){
                    //Zug wartet
                    trainShouldWait = true;
                }
                else{
                    for(Map.Entry<Vertex, Integer> entry : nextVerticesAndDay.entrySet()) {
                        //Reserviere für Tag
                        Railblock railblock = entry.getKey().getRailblock();
                        railblock.clearMapForAllPastDays(model.getDay());

                        Map<Integer, Train> reservations = railblock.getReservations();
                        reservations.put(entry.getValue(), this);

//                        System.out.println("Reservations for Vertex "+entry.getKey().getName());
//                        reservations.forEach((x, y) -> System.out.println("Tag "+x+"  vehicle name "+y.getName()));
                    }
                }
            }

            else {
                trainShouldWait = true;
//                System.out.println("nextVerticesAndDay was null");
            }
//            verticesCounter += nextVerticesAndDay.size();
        }
        else {
            verticesCounter = 0;
        }

        VehicleMovement engineMovement = getMovementForNextDay();
        if(trainShouldWait){
            engineMovement.setWait(trainShouldWait);
            revertMovementForNextDay();
            for(int i=0; i<numberOfVertices; i++){
                pathToNextStation.iterator().next().getRailblock().getReservations().put(model.getDay()+i, this);
            }
        } else {
            verticesCounter += engineMovement.getNumberOfPoints();
        }
        engineMovement.setVehicleName(engine.getGraphic());
        movements.add(engineMovement);
        //Letzte Bewegung weglassen
        for(int i=1; i<=wagons.size(); i++){
            int[] direction = reverseDirection(engineMovement.getDirectionOfLastMove());
            VehicleMovement movement =
                    engineMovement.getNewShiftedMovement(wagonShift*i, direction, wagons.get(i-1).getGraphic(), wagons.get(i-1));
            movements.add(movement);
        }
        numberOfVertices += pathToNextStationBeforeMovement.size() - pathToNextStation.size();
        return movements;
    }
//
//    public VehicleMovement getTrainMovementForNextDay(){
//        List<Vertex> pathInPrinciple = pathToNextStation;
//        pathToNextStation = pathForFewVertices;
//        VehicleMovement movement = getMovementForNextDay();
//        pathToNextStation = pathInPrinciple;
//        return movement;
//    }

    private int[] reverseDirection(int[] direction){
        int[] nDirection = new int[2];
        nDirection[0] = direction[0]*-1;
        nDirection[1] = direction[1]*-1;
        return nDirection;
    }

    public List<Vehicle> getWagons() {
        return wagons;
    }

    public Vehicle getEngine() {
        return engine;
    }

    public String getName() {
        return name;
    }

    public Map<Vertex, Integer> getVerticesForNextTime(){
        //Ziel: Map aus Tag und Vertex

        Map<Vertex, Integer> map = new HashMap<>();

        List<Vertex> currentPlannedPathToNextStation = new ArrayList<>(pathToNextStation);
//        pathToNextStationBeforeMovement = new ArrayList<>(pathToNextStation);
        // Pro Tag sollen so viele Tiles zurückgelegt werden, wie in speed steht
        PositionOnTilemap currentPosition = position;


        for(int i=0; i<3; i++){
            double wayToGo = speed;
            // Die Bewegung startet an der aktuellen Position
            VehicleMovement vehicleMovement = new VehicleMovement(currentPosition, graphic, false, trafficType, this);
            double distanceToNextVertex = 0;
            // Solange der zur Verfügung stehende Weg an dem tag noch nicht verbraucht ist und solange es noch Wegstrecke
            // in pathToNextStation gibt, soll dem vehicleMovement ein Paar aus der nächsten Position, also dem angefahrenen
            // Knoten, und der Länge des Wegs zu diesem Knoten mitgegeben werden
            while(wayToGo > 0 && currentPlannedPathToNextStation.size() > 0){
                Vertex nextVertex = currentPlannedPathToNextStation.remove(0);

                //Teste, ob Knoten noch im Graph. Teste außerdem, ob Knoten noch eine Verbindung zum nächsten Knoten hat
                // Wenn nicht wurde Straße/Schiene verändert
                TrafficGraph graph = pathfinder.getGraphForTrafficType(trafficType);
                boolean vertexContained = graph.getMapOfVertexes().containsValue(nextVertex);
                boolean hasEdge = true;
                if(vertexContained && currentPlannedPathToNextStation.size() > 0){
                    Vertex firstElementInPathToNextStation = currentPlannedPathToNextStation.remove(0);
                    hasEdge = graph.hasBidirectionalEdge(nextVertex.getName(), firstElementInPathToNextStation.getName());
                    currentPlannedPathToNextStation.add(0, firstElementInPathToNextStation);
                }
                if(!vertexContained || !hasEdge){
                    System.out.println("Fehlende Straße/Schiene entdeckt");
                    savePathToNextStationAndUpdateMovement(currentPlannedPathToNextStation.get(0), vehicleMovement);
                    return null;
                }

                distanceToNextVertex = currentPosition.getDistanceToPosition(nextVertex);
                vehicleMovement.appendPairOfPositionAndDistance(nextVertex, distanceToNextVertex);
                currentPosition = nextVertex;
                wayToGo -= distanceToNextVertex;
            }

            if(currentPlannedPathToNextStation.size() == 0 && wayToGo >= 0){
                // Station gefunden
                //Aus For-Schleife der movements springen
                Station stationAfterNext = trafficLine.getNextStation(nextStation, movementInTrafficLineGoesForward, this);
                Vertex vertexOfNextStation = nextStation.getSomeVertexForTrafficType(TrafficType.RAIL);
                List<Vertex> nextPath = pathfinder.findPathToDesiredStation(stationAfterNext, vertexOfNextStation, trafficType);
                currentPlannedPathToNextStation.addAll(nextPath);
                wayToGo = 0;
            }
            // Ansonsten wurde die Zielstation nicht erreicht

            // Wenn wayToGo dann negativ ist, also nicht genug Weg bis zum nächsten Knoten vorhanden war, muss der
            // letzte Knoten aus dem VehicleMovement wieder entfernt werden und stattdessen eine Position anteilig des übrigen
            // Weges in Richtung des nächsten Knotens hinzugefügt werden

            if(wayToGo<0){
                wayToGo+=distanceToNextVertex;
                currentPlannedPathToNextStation.add(0, (Vertex) currentPosition);
                vehicleMovement.removeLastPair();

                PositionOnTilemap previouslyLastPosition;
                if(vehicleMovement.getNumberOfPoints() == 0){
                    previouslyLastPosition = vehicleMovement.getStartPosition();
                }
                else previouslyLastPosition = vehicleMovement.getLastPair().getKey();
                VehiclePosition lastPosition = previouslyLastPosition.
                        getnewPositionShiftedTowardsGivenPointByGivenDistance(
                                currentPosition.coordsRelativeToMapOrigin(), wayToGo);

                vehicleMovement.appendPairOfPositionAndDistance(lastPosition, wayToGo);
                currentPosition = lastPosition;
            }
            else if(wayToGo > 0){
                throw new RuntimeException("wayToGo in getMovementForNextDay() was >0 : "+wayToGo);
            }

            for(PositionOnTilemap pos : vehicleMovement.getAllPositions()){
                if(pos instanceof Vertex){
                    map.put((Vertex) pos, model.getDay()+i);
                }
            }
        }
        return map;
    }


    public void addWagon(Vehicle wagon){

        if(! wagon.getKind().equals("wagon") ) throw new IllegalArgumentException("Attribute was not of kind wagon");

        if(wagon.getSpeed() < this.getSpeed()) setSpeed(wagon.getSpeed());

        Map<String, Integer> addedStorage = wagon.getStorage().getMaxima();
        for (Map.Entry<String, Integer> entry : addedStorage.entrySet()) {
            String commodity = entry.getKey();
            Integer amount = entry.getValue();
            Map<String, Integer> actualStorage = storage.getMaxima();

            if(actualStorage.containsKey(commodity)){
                actualStorage.replace(commodity, actualStorage.get(commodity) + amount);
            }
            else {
                actualStorage.put(commodity, amount);
            }
        }

        wagons.add(wagon);
    }

    public void setEngine(Vehicle engine){
        if(! engine.getKind().equals("engine") ) throw new IllegalArgumentException("Attribute was not of kind engine");

        if(engine.getSpeed() < this.getSpeed()) setSpeed(engine.getSpeed());

        this.engine = engine;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gibt eine neue Instanz des Fahrzeugs zurück
     * @return
     */
    @Override
    public Vehicle getNewInstance(){
        //Vorsicht, hier wird eagons und engine einfach weiter verwendet
        Train instance = new Train(wagons, engine, model);
        instance.setTrafficType(trafficType);
        instance.setCanTransportCargo(canTransportCargo);
        instance.setSpeed(speed);
        instance.setGraphic(graphic);
        instance.setKind(kind);
        instance.setStorage(storage.getNewInstance());
        instance.name = "train "+(int)(Math.random()*100);
        return instance;
    }


}
