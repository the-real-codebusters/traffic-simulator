package model;

public class Tower extends Stop{
    private int maxplanes;

    @Override
    public Tower getNewInstance(){
        Tower instance = new Tower();
        setInstanceStandardAttributes(instance);
        instance.setSpecial(getSpecial());
        instance.setMaxplanes(maxplanes);
        setTrafficType(TrafficType.AIR);
        return instance;
    }

    public void setMaxplanes(int maxplanes) {
        this.maxplanes = maxplanes;
    }

    public int getMaxplanes() {
        return maxplanes;
    }

    @Override
    public String toString() {
        return super.toString() +" Tower{" +
                "buildmenu='" + buildmenu + '\'' +
                ", maxplanes=" + maxplanes +
                '}';
    }
}
