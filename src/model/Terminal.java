package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Terminal extends Special {
    private Map<String, List<Double>> points = new HashMap<>();
    private List<List<String>> planes = new ArrayList<>();

    @Override
    public Terminal getNewInstance(){
        Terminal instance = new Terminal();
        setInstanceStandardAttributes(instance);
        instance.setPoints(Map.copyOf(points));
        instance.setSpecial(getSpecial());
        return instance;
    }

    public void setPoints(Map<String, List<Double>> points) {
        this.points = points;
    }

    public void setPlanes(List<List<String>> planes) {
        this.planes = planes;
    }

    @Override
    public String toString() {
        return super.toString() + " Terminal{" +
                "buildmenu='" + buildmenu + '\'' +
                ", points=" + points +
                ", planes=" + planes +
                '}';
    }
}
