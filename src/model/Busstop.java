package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Busstop extends Special {
    private String buildmenu = "road";
    private Map<String, List<Double>> points = new HashMap<>();
    private List<List<String>> roads = new ArrayList<>();
}
