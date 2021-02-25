package model;

import java.util.*;

public class Factory extends Special {


    //optional
    private Storage storage;

    private Set<Station> nearStations = new HashSet<>();
    private List<ProductionStep> productionSteps = new ArrayList<>();


    @Override
    public Factory getNewInstance(){
        Factory instance = new Factory();
        setInstanceStandardAttributes(instance);

        List<ProductionStep> newSteps = new ArrayList<>();
        for(ProductionStep oldStep : productionSteps){
            newSteps.add(oldStep.getNewInstance());
        }
        instance.setProductionSteps(newSteps);
        if(storage != null) instance.setStorage(storage.getNewInstance());

        instance.setSpecial(getSpecial());
        setTrafficType(TrafficType.NONE);
        return instance;
    }

    public Map<String, Integer> produceAndConsume(){
        // Gibt es genug zum konsumieren?

        Map<String, Integer> todayProduced = new HashMap<>();

        for(ProductionStep step : productionSteps){
            Map<String, Integer> produce = step.getProduce();
            Map<String, Integer> consume = step.getConsume();

            boolean enoughCargoInStorage = true;
            if(consume.isEmpty()){
                enoughCargoInStorage = true;
            }
            else {
                System.out.println("factory "+buildingName);
                for (Map.Entry<String, Integer> entry : consume.entrySet()) {
                    String commodity = entry.getKey();
                    int amountConsume = entry.getValue();

                    System.out.println(storage);
                    System.out.println(storage.getCargo());
                    System.out.println(storage.getCargo().get(commodity));

                    int realAmount = storage.getCargo().get(commodity);
                    if(realAmount < amountConsume) {
                        enoughCargoInStorage = false;
                    }
                }
            }

            if(enoughCargoInStorage && step.getCounter() == step.getDuration()){
                //Veringere konsumierte Waren

                for (Map.Entry<String, Integer> entry : consume.entrySet()) {
                    String commodity = entry.getKey();
                    int amountConsume = entry.getValue();

                    storage.changeCargo(commodity, -amountConsume);
                }

                for (Map.Entry<String, Integer> entry : produce.entrySet()) {
                    String commodity = entry.getKey();
                    int amountProduce = entry.getValue();

                    todayProduced.put(commodity, amountProduce);
                }

                step.setCounter(0);
            }
            else {
                step.setCounter(step.getCounter()+1);
            }
        }

        return todayProduced;
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

    public Storage getStorage() {
        return storage;
    }

    // Darf nur aufgerufen werden, wenn sicher ist, dass die entsprechende commodity konsumiert wird
    public int getFreeStorageForCommodity(String commodity){
        int realAmount = storage.getCargo().get(commodity);
        int maxAmount = storage.getMaxima().get(commodity);
        return maxAmount-realAmount;
    }

    public void setStorage(Storage storage) {
        System.out.println(storage);
        System.out.println("setStorage called");
        System.out.println("Factory:" + this.buildingName);
        this.storage = storage;
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
