package model;

public class Building {
    protected int width;
    protected int depth;
    protected int dz;
    protected String buildingName;
    protected String buildmenu;


    public String getBuildmenu() {
        return buildmenu;
    }

    public void setBuildmenu(String buildmenu) {
        this.buildmenu = buildmenu;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void setDz(int dz) {
        this.dz = dz;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getWidth() {
        return width;
    }

    public int getDepth() {
        return depth;
    }

    public int getDz() {
        return dz;
    }

    @Override
    public String toString() {
        return "Building{" +
                "width=" + width +
                ", depth=" + depth +
                ", dz=" + dz +
                '}';
    }
}
