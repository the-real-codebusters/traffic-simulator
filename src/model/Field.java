package model;

import java.util.Objects;

public class Field {

    private int height;
    private Building building;
    boolean isBuildingOrigin;

    public Field(int height, Building building) {
        this.height = height;
        this.building = building;
        if(height < 0 ) this.building = null;
    }

    // TODO: Braucht diese Klasse eventuell eine equals() Methode? ich denke eigentlich nicht, deshalb gelöscht

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isBuildingOrigin() {
        return isBuildingOrigin;
    }

    public void setBuildingOrigin(boolean buildingOrigin) {
        isBuildingOrigin = buildingOrigin;
    }
}
