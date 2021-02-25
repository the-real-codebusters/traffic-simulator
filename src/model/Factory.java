package model;

import java.util.*;

public class Factory extends Special {


    //optional
    private Map<String, Integer> storage = new HashMap<>();

    private Set<Station> nearStations = new HashSet<>();
    private List<ProductionStep> productionSteps = new ArrayList<>();

    @Override
    public Factory getNewInstance(){
        Factory instance = new Factory();
        setInstanceStandardAttributes(instance);

        instance.setProductionSteps(productionSteps);

        instance.setSpecial(getSpecial());
        setTrafficType(TrafficType.NONE);
        return instance;
    }

    public Set<Station> getNearStations() {
        return nearStations;
    }

    public List<ProductionStep> getProductionSteps() {
        return productionSteps;
    }

    public void setProductionSteps(List<ProductionStep> productionSteps) {
        this.productionSteps = productionSteps;
    }

    @Override
    public String toString() {
        return "Factory{" +
                "storage=" + storage +
                ", nearStations=" + nearStations +
                ", productionSteps=" + productionSteps +
                ", width=" + width +
                ", depth=" + depth +
                ", dz=" + dz +
                ", buildingName='" + buildingName + '\'' +
                ", buildmenu='" + buildmenu + '\'' +
                ", originColumn=" + originColumn +
                ", originRow=" + originRow +
                '}';
    }
}
