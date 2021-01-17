package view;

import java.util.HashMap;
import java.util.Map;

public class BuildingToImageMapping {

    private Map<String, String> mapping = new HashMap<>();

    public BuildingToImageMapping(String gamemode){
        if(gamemode.equals("planverkehr")){
            createPlanverkehrMapping();
        }
        else {

        }
    }

    public String getImageNameForBuildingName(String buildingName){
        return mapping.get(buildingName);
    }

    private void createPlanverkehrMapping(){
        mapping.put("construction yard", "factories/constructionYard");
        mapping.put("glassworks", "factories/glassworks");
        mapping.put("photovoltaic factory", "factories/photovoltaicFactory");
        mapping.put("silicone factory", "factories/siliconeFactory");
        mapping.put("chemical plant", "factories/chemicalPlant");
        mapping.put("submerged-arc furnace", "factories/furnace");
        mapping.put("water", "ground/water");
        mapping.put("tree", "ground/trees");
        mapping.put("gras", "ground/gras");
        mapping.put("runway", "airport/runway");
        mapping.put("rail-ne-sw", "rail/rail-ne-sw");
        mapping.put("road-ne-sw", "road/road-ne-sw");
        mapping.put("road-ne-nw-se-sw", "road/cross");
        mapping.put("road-ne-nw", "road/curve-nw-ne");
        mapping.put("road-ne", "road/road-ne");
        mapping.put("road-nw", "road/road-nw");
        mapping.put("road-se", "road/road-se");
        mapping.put("road-nw-se", "road/road-nw-se");
        mapping.put("road-sw", "road/road-sw");
        mapping.put("road-sw-ne", "road/road-sw-ne");

    }
}
