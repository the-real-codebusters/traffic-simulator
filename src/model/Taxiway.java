package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Taxiway extends Special{

    private String buildmenu = "airport";
    private Map<String, List<Double>> points = new HashMap<>();
    private List<List<String>> planes = new ArrayList<>();
}
