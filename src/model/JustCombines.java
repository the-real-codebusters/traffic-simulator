package model;

import java.util.HashMap;
import java.util.Map;

public class JustCombines extends Special {
    private Map<String, String> combines = new HashMap<>();

    public Map<String, String> getCombines() {
        return combines;
    }

    public void setCombines(Map<String, String> combines) {
        this.combines = combines;
    }
}
