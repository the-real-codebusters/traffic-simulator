import java.util.ArrayList;
import java.util.List;

public class Railway extends Vehicle {
    private List<Wagon> wagons = new ArrayList<Wagon>();
    private Engine engine;

    public List<Wagon> getWagons() {
        return wagons;
    }

    public void setWagons(List<Wagon> wagons) {
        this.wagons = wagons;
    }

    public Engine getEngine() {
        return engine;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }
}
