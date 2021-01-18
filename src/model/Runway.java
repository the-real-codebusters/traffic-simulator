package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Runway extends Special {
    private List<String> entry = new ArrayList<>();

    private Map<String, List<Double>> points = new HashMap<>();
    private List<List<String>> planes = new ArrayList<>();

    @Override
    public Runway getNewInstance(){
        Runway instance = new Runway();
        setInstanceStandardAttributes(instance);
        instance.setPoints(Map.copyOf(points));
        instance.setSpecial(getSpecial());
        return instance;
    }

    public void setEntry(List<String> entry) {
        this.entry = entry;
    }

    public void setPoints(Map<String, List<Double>> points) {
        this.points = points;
    }

    public void setPlanes(List<List<String>> planes) {
        this.planes = planes;
    }

    @Override
    public String toString() {
        return super.toString() +" Runway{" +
                "buildmenu='" + buildmenu + '\'' +
                ", entry=" + entry +
                ", points=" + points +
                ", planes=" + planes +
                '}';
    }
}
