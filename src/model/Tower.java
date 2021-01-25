package model;

public class Tower extends Stop{
    private int maxplanes;

    @Override
    public Tower getNewInstance(){
        Tower instance = new Tower();
        setInstanceStandardAttributes(instance);
        instance.setSpecial(getSpecial());
        instance.setMaxplanes(maxplanes);
        return instance;
    }

    public void setMaxplanes(int maxplanes) {
        this.maxplanes = maxplanes;
    }

    @Override
    public String toString() {
        return super.toString() +" Tower{" +
                "buildmenu='" + buildmenu + '\'' +
                ", maxplanes=" + maxplanes +
                '}';
    }
}
