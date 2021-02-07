package view;

import java.util.*;

/**
 * Stellt eine Zuordnung von Objek-Namen aus der JSON-Datei zu den Namen im jeweiligen Resources Ordner dar
 */
public class ObjectToImageMapping {

    private Map<String, String> mapping = new HashMap<>();

    public ObjectToImageMapping(String gamemode){
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
        mapping.put("sand pit", "factories/sandPit");

        mapping.put("water", "ground/water");
        mapping.put("tree", "ground/trees");
        mapping.put("stone", "ground/stones");
        mapping.put("runway", "airport/runway");
        mapping.put("rail-ne-sw", "rail/rail-ne-sw");
        mapping.put("rail-nw-se", "rail/rail-nw-se");
        mapping.put("railstation-nw-se", "stations/rail/railstation");
        mapping.put("busstop-nw-se", "stations/road/busstop");




        mapping.put("road-ne", "road/road-ne");
        mapping.put("road-nw", "road/road-nw");
        mapping.put("road-se", "road/road-se");
        mapping.put("road-sw", "road/road-sw");
        mapping.put("road-ne-nw-se-sw", "road/road-ne-nw-se-sw");
        mapping.put("road-ne-nw-se", "road/road-ne-nw-se");
        mapping.put("road-ne-nw-sw", "road/road-ne-nw-sw");
        mapping.put("road-ne-nw", "road/road-ne-nw");
        mapping.put("road-ne-se-sw", "road/road-ne-se-sw");
        mapping.put("road-ne-se", "road/road-ne-se");
        mapping.put("road-ne-sw", "road/road-ne-sw");
        mapping.put("road-nw-se-sw", "road/road-nw-se-sw");
        mapping.put("road-nw-se", "road/road-nw-se");
        mapping.put("road-nw-sw", "road/road-nw-sw");
        mapping.put("road-se-sw", "road/road-se-sw");

        mapping.put("car_ne", "road/car_ne");
        mapping.put("car_nw", "road/car_nw");
        mapping.put("car_se", "road/car_se");
        mapping.put("car_sw", "road/car_sw");
        mapping.put("grass", "ground/grass");
        mapping.put("remove", "remove");
    }

    public Collection<String> getImageNames(){
        return mapping.values();
    }
}
