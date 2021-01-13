package model;

public class Tower extends Special{
    private String buildmenu = "airport";
    private int maxplanes;

    public String getBuildmenu() {
        return buildmenu;
    }

    public void setMaxplanes(int maxplanes) {
        this.maxplanes = maxplanes;
    }
}
