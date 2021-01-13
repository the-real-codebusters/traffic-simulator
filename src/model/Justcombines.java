package model;

import java.util.HashMap;
import java.util.Map;

public class Justcombines extends Special {
    //TODO: keine Standardattribute wie depth, width, dz vorhanden
    private String buildmenu = "rail";
    private Map<String, String> combines = new HashMap<>();


    public String getBuildmenu() {
        return buildmenu;
    }

    public void setCombines(Map<String, String> combines) {
        this.combines = combines;
    }
}
