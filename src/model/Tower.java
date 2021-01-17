package model;

public class Tower extends Special{
    private int maxplanes;

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
