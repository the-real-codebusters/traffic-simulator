package model;

import java.util.HashSet;
import java.util.Set;

public class Model {

    private Set<String> buildmenus = new HashSet<>();

    public Set<String> getBuildmenus() {
        return buildmenus;
    }

    public void setBuildmenus(Set<String> buildmenus) {
        this.buildmenus = buildmenus;
    }
}
