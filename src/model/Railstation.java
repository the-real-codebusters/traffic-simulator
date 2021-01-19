package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Railstation extends Special {
    private Map<String, List<Double>> points = new HashMap<>();
    private List<List<String>> rails = new ArrayList<>();

    @Override
    public Railstation getNewInstance(){
        Railstation instance = new Railstation();
        setInstanceStandardAttributes(instance);
        instance.setPoints(Map.copyOf(points));
        instance.setRails(List.copyOf(rails));
        instance.setSpecial(getSpecial());
        return instance;
    }

    public void setPoints(Map<String, List<Double>> points) {
        this.points = points;
    }

    public void setRails(List<List<String>> rails) {
        this.rails = rails;
    }

    @Override
    public String toString() {
        return super.toString() +" Railstation{" +
                "buildmenu='" + buildmenu + '\'' +
                ", points=" + points +
                ", rails=" + rails +
                '}';
    }
}
