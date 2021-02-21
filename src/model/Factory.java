package model;

import java.util.HashMap;
import java.util.Map;

public class Factory extends Special {

    private Map<String, Integer> produce = new HashMap<>();
    private Map<String, Integer> consume = new HashMap<>();
    //optional
    private Map<String, Integer> storage = new HashMap<>();
    private int duration;

    @Override
    public Factory getNewInstance(){
        Factory instance = new Factory();
        setInstanceStandardAttributes(instance);

        // it was before 21.02.2021
        // instance.setConsume(Map.copyOf(consume));
        instance.setConsume(new HashMap<>(consume));

        // it was before 21.02.2021
        // instance.setProduce(Map.copyOf(produce));
        instance.setProduce(new HashMap<>(produce));

        instance.setSpecial(getSpecial());
        setTrafficType(TrafficType.NONE);
        return instance;
    }

    public void setProduce(Map<String, Integer> produce) {
        this.produce = produce;
    }

    public void setConsume(Map<String, Integer> consume) {
        this.consume = consume;
    }

    public void setStorage(Map<String, Integer> storage) {
        this.storage = storage;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return super.toString() + " Factory{" +
                "produce=" + produce +
                ", consume=" + consume +
                ", storage=" + storage +
                ", duration=" + duration +
                '}';
    }
}
