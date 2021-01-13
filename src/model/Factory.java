package model;

import java.util.HashMap;
import java.util.Map;

public class Factory extends Special {

    private Map<String, Integer> produce = new HashMap<>();
    private Map<String, Integer> consume = new HashMap<>();
    private int duration;
}
