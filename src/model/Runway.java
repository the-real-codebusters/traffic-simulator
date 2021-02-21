package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Runway extends Stop {
    private List<String> entry = new ArrayList<>();

    @Override
    public Runway getNewInstance(){
        Runway instance = new Runway();
        setInstanceStandardAttributes(instance);

        // it was before 21.02.2021
        // instance.setPoints(Map.copyOf(points));
        instance.setPoints(new HashMap<>(points));

        instance.setSpecial(getSpecial());

        // it was before 21.02.2021
        // instance.setTransportations(List.copyOf(transportations));
        instance.setTransportations(new ArrayList<>(transportations));

        instance.setEntry(entry);
        instance.setTrafficType(TrafficType.AIR);
        return instance;
    }

    public void setEntry(List<String> entry) {
        this.entry = entry;
    }

    @Override
    public String toString() {
        return super.toString() +" Runway{" +
                ", entry=" + entry +
                '}';
    }
}
