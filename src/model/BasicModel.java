package model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BasicModel {
    private Set<String> commodities;
//    private List<Reservation> reservations;
    private int day;
    private double speedOfDay;
    private MapModel map;

    private String gamemode;
//    private ToolsModel tools;

    private Set<String> buildmenus = new HashSet<>();
    private List<Building> buildings;


    public BasicModel(Set<String> commodities, int day, double speedOfDay,
                      MapModel map, String gamemode, Set<String> buildmenus, ArrayList<Building> buildings) {
        this.commodities = commodities;
        this.day = day;
        this.speedOfDay = speedOfDay;
        this.map = map;
        this.gamemode = gamemode;
        this.buildmenus = buildmenus;
        this.buildings = buildings;
    }

    public List<Building> getBuildingsForBuildmenu(String buildmenu) {
        buildings.forEach(x -> System.out.println(x.getBuildingName()+"  "+x.getBuildmenu()));

        List<Building> bs = new ArrayList<>();

        for(Building building: buildings){
            if(building.getBuildingName() != null && building.getBuildingName().equals("road-ne")){
                System.out.println("test "+building.getBuildmenu());
            }
            String menu = building.getBuildmenu();
            if( menu != null && menu.equals(buildmenu)){
                bs.add(building);
            }
        }
        return bs;
    }

    public List<Building> getBuildingsForSpecialUse(String special) {

        List<Building> bs = new ArrayList<>();

        for(Building building: buildings){
            if(building instanceof Special && ((Special) building).getSpecial().equals(special)){
                bs.add(building);
            }
        }
        return bs;
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

    public Set<String> getBuildmenus() {
        return buildmenus;
    }

    public void setBuildmenus(Set<String> buildmenus) {
        this.buildmenus = buildmenus;
    }

    public String getGamemode() {
        return gamemode;
    }

    public Field[][] getFieldGridOfMap(){
        return map.getFieldGrid();
    }

    public List<Building> getBuildings() {
        return buildings;
    }

    public void setBuildings(List<Building> buildings) {
        this.buildings = buildings;
    }
}
