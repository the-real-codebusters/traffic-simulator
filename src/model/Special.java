package model;

public class Special extends Building{
    private String special;

    public String getSpecial() {
        return special;
    }

    public void setSpecial(String special) {
        switch (special) {
            case "railstation": setTrafficType(TrafficType.RAIL); break;
            case "tower": setTrafficType(TrafficType.AIR); break;
            case "terminal": setTrafficType(TrafficType.AIR); break;
            case "taxiway": setTrafficType(TrafficType.AIR); break;
            case "runway": setTrafficType(TrafficType.AIR); break;
            case "nature": setTrafficType(TrafficType.NONE); break;
            case "justcombines": setTrafficType(TrafficType.RAIL); break;
            case "factory": setTrafficType(TrafficType.NONE); break;
            case "busstop": setTrafficType(TrafficType.ROAD); break;
        }
        this.special = special;
    }
}
