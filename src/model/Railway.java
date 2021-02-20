package model;

import java.util.ArrayList;
import java.util.List;

public class Railway extends Vehicle {
    private List<Vehicle> wagons = new ArrayList<>();
    private Vehicle engine;

    public List<Vehicle> getWagons() {
        return wagons;
    }

//    public void setWagons(List<Wagon> wagons) {
//        this.wagons = wagons;
//    }

    public Vehicle getEngine() {
        return engine;
    }

    public void addWagon(Vehicle wagon){
        if(! wagon.getTrafficType().equals("wagon") ) throw new IllegalArgumentException("Attribute was not of kind wagon");

        if(wagon.getSpeed() < this.getSpeed()) setSpeed(wagon.getSpeed());

        wagons.add(wagon);
    }

    public void setEngine(Vehicle engine){
        if(! engine.getTrafficType().equals("engine") ) throw new IllegalArgumentException("Attribute was not of kind engine");

        if(engine.getSpeed() < this.getSpeed()) setSpeed(engine.getSpeed());

        this.engine = engine;
    }

}
