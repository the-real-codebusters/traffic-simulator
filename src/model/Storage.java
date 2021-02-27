package model;

import java.util.HashMap;
import java.util.Map;

public class Storage {

    private Map<String, Integer> maxima;
    private Map<String, Integer> cargo = new HashMap<>();

    public Storage(Map<String, Integer> maxima) {
        this.maxima = maxima;
        for (Map.Entry<String, Integer> entry : maxima.entrySet()) {
//            System.out.println("Key of commodity in Storage "+entry.getKey());
            cargo.put(entry.getKey(), 0);
        }
    }

    public Storage getNewInstance(){
        Map<String, Integer> maxima = new HashMap<>(this.maxima);
//        System.out.println("maxima in getNewInstance "+maxima);
        return new Storage(maxima);
    }

    /**
     * Ändert die Anzahl der angegebenen Ware. Wenn mehr Waren abgelegt werden sollen als Platz vorhanden sind, wird
     * der Rest der Ware vernichtet.
     * @param commodity Die Ware als String
     * @param change Der Wert, um den die Ware im Lager vergrößert / verringert werden soll.
     */
    public void changeCargo(String commodity, int change){
        int changedCargoAmount = cargo.get(commodity) + change;
        int maximumAmount = maxima.get(commodity);
        if(changedCargoAmount > maximumAmount) {
            changedCargoAmount = maximumAmount;
        }
        else if(changedCargoAmount < 0){
            throw new RuntimeException("Amount of a commodity was decreased to a number less than 0 :"+changedCargoAmount);
            // Bei sonst korrektem Programm sollte diese Ausnahme nie geworfen werden.
        }
        cargo.replace(commodity, changedCargoAmount);
    }

    public Map<String, Integer> getMaxima() {
        return maxima;
    }

    public void setMaxima(Map<String, Integer> maxima) {
        this.maxima = maxima;
    }

    public Map<String, Integer> getCargo() {
        return cargo;
    }
}

