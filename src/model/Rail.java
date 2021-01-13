package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Rail extends Building {


    // optional
    private String buildmenu;

    //TODO: Himmelsrichtungen (key) als enum definieren
    private Map<String, List<Double>> points = new HashMap<>();
    private List<List<String>> rails = new ArrayList<>();
    // optional
    private List<String> signals = new ArrayList<>();


    public void setBuildmenu(String buildmenu) {
        this.buildmenu = buildmenu;
    }

    public void setPoints(Map<String, List<Double>> points) {
        this.points = points;
    }

    public void setRails(List<List<String>> rails) {
        this.rails = rails;
    }

    public void setSignals(List<String> signals) {
        this.signals = signals;
    }

    @Override
    public String toString() {
        return "Building{" +
                "buildmenu='" + buildmenu + '\'' +
                ", width=" + getWidth() +
                ", depth=" + getDepth() +
                ", points=" + points +
                ", rails=" + rails +
                ", dz=" + getDz() +
                '}';
    }

}

