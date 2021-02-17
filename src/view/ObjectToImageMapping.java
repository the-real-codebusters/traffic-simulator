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

    public String getImageNameForObjectName(String objectName){
        return mapping.get(objectName);
    }

    private void createPlanverkehrMapping(){
        mapping.put("construction yard", "factories/constructionYard");
        mapping.put("glassworks", "factories/glassworks");
        mapping.put("photovoltaic factory", "factories/photovoltaicFactory");
        mapping.put("silicone factory", "factories/siliconeFactory");
        mapping.put("chemical plant", "factories/chemicalPlant");
        mapping.put("submerged-arc furnace", "factories/furnace");
        mapping.put("sand pit", "factories/sandPit");

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

        mapping.put("panel_transporter-ne", "vehicles/road/panel-ne");
        mapping.put("panel_transporter-nw", "vehicles/road/panel-nw");
        mapping.put("panel_transporter-se", "vehicles/road/panel-se");
        mapping.put("panel_transporter-sw", "vehicles/road/panel-sw");

        mapping.put("bulk_truck-ne", "vehicles/road/bulk_ne");
        mapping.put("bulk_truck-nw", "vehicles/road/bulk_nw");
        mapping.put("bulk_truck-se", "vehicles/road/bulk_se");
        mapping.put("bulk_truck-sw", "vehicles/road/bulk_sw");

        mapping.put("hazmat_truck-ne", "vehicles/road/car_small-ne");
        mapping.put("hazmat_truck-nw", "vehicles/road/car_small-nw");
        mapping.put("hazmat_truck-se", "vehicles/road/car_small-se");
        mapping.put("hazmat_truck-sw", "vehicles/road/car_small-sw");

        mapping.put("silicone_truck-ne", "vehicles/road/silicone-ne");
        mapping.put("silicone_truck-nw", "vehicles/road/silicone-nw");
        mapping.put("silicone_truck-se", "vehicles/road/silicone-se");
        mapping.put("silicone_truck-sw", "vehicles/road/silicone-sw");

//        mapping.put("grass", "ground/grass");
        mapping.put("grass", "ground/grass_tile");
//                mapping.put("water", "ground/water");
        mapping.put("water", "ground/water_tile");

        mapping.put("height_up" , "height_up");
        mapping.put("height_down" , "height_down");

        mapping.put("remove", "remove");

    }

    public Collection<String> getImageNames(){
        return mapping.values();
    }
}
