package model;

public class Building {
    protected int width;
    protected int depth;
    protected int dz;
    protected String buildingName;
    protected String buildmenu;

    protected int originColumn;
    protected int originRow;

    public int getOriginColumn() {
        return originColumn;
    }

    public void setOriginColumn(int originColumn) {
        this.originColumn = originColumn;
    }

    public int getOriginRow() {
        return originRow;
    }

    public void setOriginRow(int originRow) {
        this.originRow = originRow;
    }

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


    /**
     * Gibt eine neue Instanz des Gebäudes zurück
     * @return
     */
    protected Building getNewInstance(){
        Building instance = new Building();
        setInstanceStandardAttributes(instance);
        return instance;
    }

    /**
     * Setzt die Standardattribute der Instanz auf die Werte dieses Gebäudes
     * @param instance
     */
    protected void setInstanceStandardAttributes(Building instance){
        instance.setBuildmenu(this.getBuildmenu());
        instance.setBuildingName(this.getBuildingName());
        instance.setDepth(this.getDepth());
        instance.setWidth(this.getWidth());
        instance.setDz(this.getDz());
    }

    @Override
    public String toString() {
        return "Building{" +
                "name=" + buildingName +
                ", width=" + width +
                ", depth=" + depth +
                ", dz=" + dz +
                '}';
    }
}
