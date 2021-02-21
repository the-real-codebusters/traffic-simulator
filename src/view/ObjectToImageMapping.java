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
//        mapping.put("sand pit", "factories/sandPit");
        mapping.put("sand pit", "factories/sandPit_test");

        mapping.put("tree", "ground/trees");
        // wird momentan noch nicht benutzt
        mapping.put("tree_no_ground", "ground/trees_no_ground");

//        mapping.put("stone", "ground/stones");
        mapping.put("stone", "ground/stones_test");
        mapping.put("runway", "airport/runway");
        mapping.put("rail-ne-sw", "rail/rail-ne-sw");
        mapping.put("rail-nw-se", "rail/rail-nw-se");
        mapping.put("railstation-nw-se", "stations/rail/railstation");
        mapping.put("busstop-nw-se", "stations/road/busstop");

        // Flache Straßen-Tiles
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

        mapping.put("hazmat_plane-ne", "vehicles/air/hazmat_plane-ne");
        mapping.put("hazmat_plane-nw", "vehicles/air/hazmat_plane-nw");
        mapping.put("hazmat_plane-se", "vehicles/air/hazmat_plane-se");
        mapping.put("hazmat_plane-sw", "vehicles/air/hazmat_plane-sw");

        mapping.put("silicone_truck-ne", "vehicles/road/silicone-ne");
        mapping.put("silicone_truck-nw", "vehicles/road/silicone-nw");
        mapping.put("silicone_truck-se", "vehicles/road/silicone-se");
        mapping.put("silicone_truck-sw", "vehicles/road/silicone-sw");

        mapping.put("plane-ne", "vehicles/air/plane-ne");
        mapping.put("plane-nw", "vehicles/air/plane-nw");
        mapping.put("plane-se", "vehicles/air/plane-se");
        mapping.put("plane-sw", "vehicles/air/plane-sw");

//        mapping.put("grass", "ground/grass");
//        mapping.put("grass", "ground/grass_tile");
        mapping.put("grass", "ground/grass_tile_test");
//                mapping.put("water", "ground/water");
        mapping.put("water", "ground/water_tile");

        mapping.put("height_up" , "height_up");
        mapping.put("height_down" , "height_down");

        mapping.put("remove", "remove");

        // Grasfelder mit verschiedenen Höhen
        mapping.put("0100", "ground_heights/Slope_E");
        mapping.put("1101", "ground_heights/Slope_ENW");
        mapping.put("0101", "ground_heights/Slope_EW");
        mapping.put("0000", "ground_heights/Slope_Flat");
        mapping.put("1000", "ground_heights/Slope_N");
        mapping.put("1100", "ground_heights/Slope_NE");
        mapping.put("1010", "ground_heights/Slope_NS");
        mapping.put("1001", "ground_heights/Slope_NW");
        mapping.put("1011", "ground_heights/Slope_NWS");
        mapping.put("0010", "ground_heights/Slope_S");
        mapping.put("0110", "ground_heights/Slope_SE");
        mapping.put("1110", "ground_heights/Slope_SEN");
        mapping.put("1210", "ground_heights/Slope_Steep_E");
        mapping.put("2101", "ground_heights/Slope_Steep_N");
        mapping.put("0121", "ground_heights/Slope_Steep_S");
        mapping.put("1012", "ground_heights/Slope_Steep_W");
        mapping.put("0011", "ground_heights/Slope_SW");
        mapping.put("0001", "ground_heights/Slope_W");
        mapping.put("0111", "ground_heights/Slope_WSE");

        // Straßen-Tiles mit Höhenunterschied
//        mapping.put("road-ne-sw1100", "road/road_ne-sw_Slope_NE");
//        mapping.put("road-ne-sw1001", "road/road_ne-sw_Slope_NW");
//        mapping.put("road-ne-sw0110", "road/road_ne-sw_Slope_SE");
//        mapping.put("road-ne-sw0011", "road/road_ne-sw_Slope_SW");

        mapping.put("road-ne1100", "road/road_ne-sw_Slope_NE");
        mapping.put("road-ne1001", "road/road_ne-sw_Slope_NW");
        mapping.put("road-ne0110", "road/road_ne-sw_Slope_SE");
        mapping.put("road-ne0011", "road/road_ne-sw_Slope_SW");

        mapping.put("road-nw-se1100", "road/road_nw-se_Slope_NE");
        mapping.put("road-nw-se1001", "road/road_nw-se_Slope_NW");
        mapping.put("road-nw-se0110", "road/road_nw-se_Slope_SE");
        mapping.put("road-nw-se0011", "road/road_nw-se_Slope_SW");

    }

    public Collection<String> getImageNames(){
        return mapping.values();
    }
}
