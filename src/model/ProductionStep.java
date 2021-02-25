package model;

import java.util.HashMap;
import java.util.Map;

public class ProductionStep {

    private Map<String, Integer> produce = new HashMap<>();
    private Map<String, Integer> consume = new HashMap<>();

    private int duration;
    private int counter = 0;

    public ProductionStep(Map<String, Integer> produce, Map<String, Integer> consume, int duration) {
        this.produce = produce;
        this.consume = consume;
        this.duration = duration;
    }

    public ProductionStep getNewInstance(){
        ProductionStep step = new ProductionStep(produce, consume, duration);
        return step;
    }

    public Map<String, Integer> getProduce() {
        return produce;
    }

    public void setProduce(Map<String, Integer> produce) {
        this.produce = produce;
    }

    public Map<String, Integer> getConsume() {
        return consume;
    }

    public void setConsume(Map<String, Integer> consume) {
        this.consume = consume;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }
}
