package model;

import java.util.*;

public class BasicModel {
    private Set<String> commodities;
//    private List<Reservation> reservations;
    private int day;
    private double speedOfDay;
    private MapModel map;
    private TrafficGraph roadsGraph;

    private String gamemode;
//    private ToolsModel tools;

    private Set<String> buildmenus = new HashSet<>();
    private List<Building> buildings = new ArrayList<>();


    public BasicModel(Set<String> commodities, int day, double speedOfDay, MapModel map, String gamemode,
                      Set<String> buildmenus, ArrayList<Building> buildings, TrafficGraph roadsGraph) {
        this.commodities = commodities;
        this.day = day;
        this.speedOfDay = speedOfDay;
        this.map = map;
        this.gamemode = gamemode;
        this.buildmenus = buildmenus;
        this.buildings = buildings;
        this.roadsGraph = roadsGraph;
    }

    public BasicModel() {
        this.commodities = new HashSet<String>();
        this.day = 0;
        this.speedOfDay = 1.0;
        this.map = null;
        this.gamemode = null;
        this.buildmenus = null;
        this.roadsGraph = new TrafficGraph();
    }

    public List<Building> getBuildingsForBuildmenu(String buildmenu) {
        // TODO Benutze Buildings aus aus Model, wie von JSONParser eingelesen

        List<Building> bs = new ArrayList<>();

        for(Building building: buildings){
            if(building.getBuildingName() != null && building.getBuildingName().equals("road-ne")){
                System.out.println("test "+building.getBuildingName());
            }
            String menu = building.getBuildmenu();
            if( menu != null && menu.equals(buildmenu)){
                bs.add(building);
            }
        }
        return bs;
    }


    public List<Road> getRoadsFromBuildings() {

        List<Road> roads = new ArrayList<>();
        for(Building building: buildings){
            if(building.getBuildingName() != null && building.getBuildingName().contains("road")){
                roads.add((Road) building);
            }
        }
        return roads;
    }


    public List<Special> getBuildingsForSpecialUse(String special) {

        List<Special> bs = new ArrayList<>();

        for(Building building: buildings){
            if(building instanceof Special && ((Special) building).getSpecial().equals(special)){
                bs.add((Special) building);
            }
        }
        return bs;
    }


    public void addPointsToGraph(Building selectedBuilding, int xCoordOfTile, int yCoordOfTile){
        List<Road> roads = getRoadsFromBuildings();
        for (Road road : roads) {
            if (selectedBuilding.getBuildingName().equals(road.getBuildingName())) {

                Map<String, List<Double>> points = road.getPoints();
                for (Map.Entry<String, List<Double>> entry : points.entrySet()) {

                    // Damit name unique bleibt, sonst gäbe es Duplikate
                    String vertexName = xCoordOfTile+"-"+yCoordOfTile+"-"+entry.getKey();

                    double xCoordOfPoint = entry.getValue().get(0);
                    double yCoordOfPoint = entry.getValue().get(1);

                    Vertex v = new Vertex(vertexName, xCoordOfPoint, yCoordOfPoint, xCoordOfTile, yCoordOfTile);

                    double vXcord = v.coordsRelativeToMapOrigin().getX();
                    double vYcord = v.coordsRelativeToMapOrigin().getY();
                    System.out.println("Points: " + v.getName() + " " + vXcord + " " + vYcord);

                    getRoadsGraph().addVertex(v);

                    for (Vertex v1 : getRoadsGraph().getMapOfVertexes().values()){
                        if(!v.getName().equals(v1.getName())
                                && (v.getxCoordinateInGameMap() == v1.getxCoordinateInGameMap())
                                && (v.getyCoordinateInGameMap() == v1.getyCoordinateInGameMap())) {
                            getRoadsGraph().addEdgeBidirectional(v1.getName(), v.getName());
                        }
                    }
                }
            }
        }
        getRoadsGraph().checkForDuplicatePoints();
        getRoadsGraph().printGraph();
    }

    public void addCommodities(List<String> commodities) {
        this.commodities.addAll(commodities);
    }

    public Set<String> getCommodities() {
        return commodities;
    }

    public void setCommodities(Set<String> commodities) {
        this.commodities = commodities;
    }

//    public List<Reservation> getReservations() {
//        return reservations;
//    }
//
//    public void setReservations(List<Reservation> reservations) {
//        this.reservations = reservations;
//    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public double getSpeedOfDay() {
        return speedOfDay;
    }

    public void setSpeedOfDay(double speedOfDay) {
        this.speedOfDay = speedOfDay;
    }

    public MapModel getMap() {
        return map;
    }

    public void setMap(MapModel map) {
        this.map = map;
    }

//    public ToolsModel getTools() {
//        return tools;
//    }
//
//    public void setTools(ToolsModel tools) {
//        this.tools = tools;
//    }


    public TrafficGraph getRoadsGraph() {
        return roadsGraph;
    }

    public void setRoadsGraph(TrafficGraph roadsGraph) {
        this.roadsGraph = roadsGraph;
    }

    public Set<String> getBuildmenus() {
        return buildmenus;
    }

    public void setBuildmenus(Set<String> buildmenus) {
        this.buildmenus = buildmenus;
    }

    public String getGamemode() {
        return gamemode;
    }

    public void setGamemode(String gamemode) {
        this.gamemode = gamemode;
    }

    public List<Building> getBuildings() {
        return buildings;
    }

    public void setBuildings(List<Building> buildings) {
        this.buildings = buildings;
    }

    public Field[][] getFieldGridOfMap(){
        return map.getFieldGrid();
    }

    public void printModelAttributes(){
        System.out.println("Model attributes: ");
        System.out.print("Commodities: ");
        for(String commodity : commodities){
            System.out.print(commodity + ", ");
        }
        System.out.println();
        System.out.println("Day: " + day);
        System.out.println("Speed of day: " + speedOfDay);
        System.out.println("Map with following attributes:\n    Width: " + map.getWidth() + "\n    Depth: " + map.getDepth());
        System.out.println("Gamemode: " + gamemode);
        System.out.print("Buildings: ");
        for(String buildmenu : buildmenus){
            System.out.print(buildmenu + ", ");
        }
    }
}
