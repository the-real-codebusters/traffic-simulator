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

    public void setCargo(Map<String, Integer> cargo) {
        this.cargo = cargo;
    }
}

