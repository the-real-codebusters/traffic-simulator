package model;

import java.util.HashMap;
import java.util.Map;

public class Storage {

    private Map<String, Integer> maxima = new HashMap<>();
    private Map<String, Integer> cargo = new HashMap<>();

    public Map<String, Integer> getMaxima() {
        return maxima;
    }

    public void setMaxima(Map<String, Integer> maxima) {
        this.maxima = maxima;
    }

    public Map<String, Integer> getCargo() {
        return cargo;
    }

    public void changeCargo(String commodity, int change){
        int changedCargoAmount = cargo.get(commodity) + change;
        int maximumAmount = maxima.get(commodity);
        if(changedCargoAmount > maximumAmount) {
            changedCargoAmount = maximumAmount;
        }
        else if(changedCargoAmount < 0){
            changedCargoAmount = 0;
        }
        cargo.replace(commodity, changedCargoAmount);
    }
}

