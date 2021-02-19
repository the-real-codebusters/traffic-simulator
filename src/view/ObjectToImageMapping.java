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

        // Fabriken
        mapping.put("construction yard", "factories/constructionYard");
        mapping.put("glassworks", "factories/glassworks");
        mapping.put("photovoltaic factory", "factories/photovoltaicFactory");
        mapping.put("silicone factory", "factories/siliconeFactory");
        mapping.put("chemical plant", "factories/chemicalPlant");
        mapping.put("submerged-arc furnace", "factories/furnace");
        mapping.put("sand pit", "factories/sandPit");


        // Natur
        mapping.put("grass", "ground/grass");
        mapping.put("water", "ground/water");
        mapping.put("tree", "ground/trees");
        mapping.put("stone", "ground/stones");
//        mapping.put("tree_no_ground", "ground/trees_no_ground");


        // Flache Rails (unvollständig)
        mapping.put("rail-ne-sw", "rail/rail-ne-sw");
        mapping.put("rail-nw-se", "rail/rail-nw-se");
//        mapping.put("railcurve-ne-s", "rail/railcurve-ne-s");
//        mapping.put("railcurve-ne-w", "rail/railcurve-ne-w");
//        mapping.put("railcurve-nw-e", "rail/railcurve-nw-e");
//        mapping.put("railcurve-nw-s", "rail/railcurve-nw-s");
//        mapping.put("railcurve-se-n", "rail/railcurve-se-n");
//        mapping.put("railcurve-se-w", "rail/railcurve-se-w");
//        mapping.put("railcurve-sw-e", "rail/railcurve-sw-e");
//        mapping.put("railcurve-sw-n", "rail/railcurve-sw-n");
//        mapping.put("railswitch-ne-s", "rail/railswitch-ne-s");
//        mapping.put("railswitch-nw-s", "rail/railswitch-nw-s");
//        mapping.put("railswitch-se-n", "rail/railswitch-se-n");
//        mapping.put("railswitch-sw-n", "rail/railswitch-sw-n");



        // Verschiedenes
        mapping.put("railstation-nw-se", "stations/rail/railstation_nw_se");
        mapping.put("busstop-nw-se", "stations/road/busstop");
        mapping.put("runway", "airport/runway");
        mapping.put("height_up" , "height_up");
        mapping.put("height_down" , "height_down");
        mapping.put("remove", "remove");


        // Flache Straßen-Tiles
        mapping.put("road-ne", "road/flat/road-ne");
        mapping.put("road-nw", "road/flat/road-nw");
        mapping.put("road-se", "road/flat/road-se");
        mapping.put("road-sw", "road/flat/road-sw");
        mapping.put("road-ne-nw-se-sw", "road/flat/road-ne-nw-se-sw");
        mapping.put("road-ne-nw-se", "road/flat/road-ne-nw-se");
        mapping.put("road-ne-nw-sw", "road/flat/road-ne-nw-sw");
        mapping.put("road-ne-nw", "road/flat/road-ne-nw");
        mapping.put("road-ne-se-sw", "road/flat/road-ne-se-sw");
        mapping.put("road-ne-se", "road/flat/road-ne-se");
        mapping.put("road-ne-sw", "road/flat/road-ne-sw");
        mapping.put("road-nw-se-sw", "road/flat/road-nw-se-sw");
        mapping.put("road-nw-se", "road/flat/road-nw-se");
        mapping.put("road-nw-sw", "road/flat/road-nw-sw");
        mapping.put("road-se-sw", "road/flat/road-se-sw");


        // Fahrzeuge
        mapping.put("car_ne", "road_vehicles/car_ne");
        mapping.put("car_nw", "road_vehicles/car_nw");
        mapping.put("car_se", "road_vehicles/car_se");
        mapping.put("car_sw", "road_vehicles/car_sw");


        // Grasfelder mit verschiedenen Höhen
        mapping.put("0000", "ground/grass");
        mapping.put("0100", "ground_heights/Slope_E");
        mapping.put("1101", "ground_heights/Slope_ENW");
        mapping.put("0101", "ground_heights/Slope_EW");
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


        // StraßenTiles mit Höhe: slope ne-sw
        mapping.put("road-ne-sw0100", "road/slope_ne_sw/road_Slope_ne-sw_E");
        mapping.put("road-ne0100", "road/slope_ne_sw/road_Slope_ne-sw_E");
        mapping.put("road-sw0100", "road/slope_ne_sw/road_Slope_ne-sw_E");

        mapping.put("road-ne-sw1101", "road/slope_ne_sw/road_Slope_ne-sw_ENW");
        mapping.put("road-ne1101", "road/slope_ne_sw/road_Slope_ne-sw_ENW");
        mapping.put("road-sw1101", "road/slope_ne_sw/road_Slope_ne-sw_ENW");

        mapping.put("road-ne-sw0101", "road/slope_ne_sw/road_Slope_ne-sw_EW");
        mapping.put("road-ne0101", "road/slope_ne_sw/road_Slope_ne-sw_EW");
        mapping.put("road-sw0101", "road/slope_ne_sw/road_Slope_ne-sw_EW");

        mapping.put("road-ne-sw1000", "road/slope_ne_sw/road_Slope_ne-sw_N");
        mapping.put("road-ne1000", "road/slope_ne_sw/road_Slope_ne-sw_N");
        mapping.put("road-sw1000", "road/slope_ne_sw/road_Slope_ne-sw_N");

        mapping.put("road-ne-sw1100", "road/slope_ne_sw/road_Slope_ne-sw_NE");
        mapping.put("road-ne1100", "road/slope_ne_sw/road_Slope_ne-sw_NE");
        mapping.put("road-sw1100", "road/slope_ne_sw/road_Slope_ne-sw_NE");

        mapping.put("road-ne-sw1010", "road/slope_ne_sw/road_Slope_ne-sw_NS");
        mapping.put("road-ne1010", "road/slope_ne_sw/road_Slope_ne-sw_NS");
        mapping.put("road-sw1010", "road/slope_ne_sw/road_Slope_ne-sw_NS");

        mapping.put("road-ne-sw1001", "road/slope_ne_sw/road_Slope_ne-sw_NW");
        mapping.put("road-ne1001", "road/slope_ne_sw/road_Slope_ne-sw_NW");
        mapping.put("road-sw1001", "road/slope_ne_sw/road_Slope_ne-sw_NW");

        mapping.put("road-ne-sw1011", "road/slope_ne_sw/road_Slope_ne-sw_NWS");
        mapping.put("road-ne1011", "road/slope_ne_sw/road_Slope_ne-sw_NWS");
        mapping.put("road-sw1011", "road/slope_ne_sw/road_Slope_ne-sw_NWS");

        mapping.put("road-ne-sw0010", "road/slope_ne_sw/road_Slope_ne-sw_S");
        mapping.put("road-ne0010", "road/slope_ne_sw/road_Slope_ne-sw_S");
        mapping.put("road-sw0010", "road/slope_ne_sw/road_Slope_ne-sw_S");

        mapping.put("road-ne-sw0110", "road/slope_ne_sw/road_Slope_ne-sw_SE");
        mapping.put("road-ne0110", "road/slope_ne_sw/road_Slope_ne-sw_SE");
        mapping.put("road-sw0110", "road/slope_ne_sw/road_Slope_ne-sw_SE");

        mapping.put("road-ne-sw1110", "road/slope_ne_sw/road_Slope_ne-sw_SEN");
        mapping.put("road-ne1110", "road/slope_ne_sw/road_Slope_ne-sw_SEN");
        mapping.put("road-sw1110", "road/slope_ne_sw/road_Slope_ne-sw_SEN");

        mapping.put("road-ne-sw0011", "road/slope_ne_sw/road_Slope_ne-sw_SW");
        mapping.put("road-ne0011", "road/slope_ne_sw/road_Slope_ne-sw_SW");
        mapping.put("road-sw0011", "road/slope_ne_sw/road_Slope_ne-sw_SW");

        mapping.put("road-ne-sw0001", "road/slope_ne_sw/road_Slope_ne-sw_W");
        mapping.put("road-ne0001", "road/slope_ne_sw/road_Slope_ne-sw_W");
        mapping.put("road-sw0001", "road/slope_ne_sw/road_Slope_ne-sw_W");

        mapping.put("road-ne-sw0111", "road/slope_ne_sw/road_Slope_ne-sw_WSE");
        mapping.put("road-ne0111", "road/slope_ne_sw/road_Slope_ne-sw_WSE");
        mapping.put("road-sw0111", "road/slope_ne_sw/road_Slope_ne-sw_WSE");


        // StraßenTiles mit Höhe: slope nw-se
        mapping.put("road-nw-se0100", "road/slope_nw_se/road_Slope_nw-se_E");
        mapping.put("road-nw0100", "road/slope_nw_se/road_Slope_nw-se_E");
        mapping.put("road-se0100", "road/slope_nw_se/road_Slope_nw-se_E");

        mapping.put("road-nw-se1101", "road/slope_nw_se/road_Slope_nw-se_ENW");
        mapping.put("road-nw1101", "road/slope_nw_se/road_Slope_nw-se_ENW");
        mapping.put("road-se1101", "road/slope_nw_se/road_Slope_nw-se_ENW");

        mapping.put("road-nw-se0101", "road/slope_nw_se/road_Slope_nw-se_EW");
        mapping.put("road-nw0101", "road/slope_nw_se/road_Slope_nw-se_EW");
        mapping.put("road-se0101", "road/slope_nw_se/road_Slope_nw-se_EW");

        mapping.put("road-nw-se1000", "road/slope_nw_se/road_Slope_nw-se_N");
        mapping.put("road-nw1000", "road/slope_nw_se/road_Slope_nw-se_N");
        mapping.put("road-se1000", "road/slope_nw_se/road_Slope_nw-se_N");

        mapping.put("road-nw-se1100", "road/slope_nw_se/road_Slope_nw-se_NE");
        mapping.put("road-nw1100", "road/slope_nw_se/road_Slope_nw-se_NE");
        mapping.put("road-se1100", "road/slope_nw_se/road_Slope_nw-se_NE");

        mapping.put("road-nw-se1010", "road/slope_nw_se/road_Slope_nw-se_NS");
        mapping.put("road-nw1010", "road/slope_nw_se/road_Slope_nw-se_NS");
        mapping.put("road-se1010", "road/slope_nw_se/road_Slope_nw-se_NS");

        mapping.put("road-nw-se1001", "road/slope_nw_se/road_Slope_nw-se_NW");
        mapping.put("road-nw1001", "road/slope_nw_se/road_Slope_nw-se_NW");
        mapping.put("road-se1001", "road/slope_nw_se/road_Slope_nw-se_NW");

        mapping.put("road-nw-se1011", "road/slope_nw_se/road_Slope_nw-se_NWS");
        mapping.put("road-nw1011", "road/slope_nw_se/road_Slope_nw-se_NWS");
        mapping.put("road-se1011", "road/slope_nw_se/road_Slope_nw-se_NWS");

        mapping.put("road-nw-se0010", "road/slope_nw_se/road_Slope_nw-se_S");
        mapping.put("road-nw0010", "road/slope_nw_se/road_Slope_nw-se_S");
        mapping.put("road-se0010", "road/slope_nw_se/road_Slope_nw-se_S");

        mapping.put("road-nw-se0110", "road/slope_nw_se/road_Slope_nw-se_SE");
        mapping.put("road-nw0110", "road/slope_nw_se/road_Slope_nw-se_SE");
        mapping.put("road-se0110", "road/slope_nw_se/road_Slope_nw-se_SE");

        mapping.put("road-nw-se1110", "road/slope_nw_se/road_Slope_nw-se_SEN");
        mapping.put("road-nw1110", "road/slope_nw_se/road_Slope_nw-se_SEN");
        mapping.put("road-se1110", "road/slope_nw_se/road_Slope_nw-se_SEN");

        mapping.put("road-nw-se0011", "road/slope_nw_se/road_Slope_nw-se_SW");
        mapping.put("road-nw0011", "road/slope_nw_se/road_Slope_nw-se_SW");
        mapping.put("road-se0011", "road/slope_nw_se/road_Slope_nw-se_SW");

        mapping.put("road-nw-se0001", "road/slope_nw_se/road_Slope_nw-se_W");
        mapping.put("road-nw0001", "road/slope_nw_se/road_Slope_nw-se_W");
        mapping.put("road-se0001", "road/slope_nw_se/road_Slope_nw-se_W");

        mapping.put("road-nw-se0111", "road/slope_nw_se/road_Slope_nw-se_WSE");
        mapping.put("road-nw0111", "road/slope_nw_se/road_Slope_nw-se_WSE");
        mapping.put("road-se0111", "road/slope_nw_se/road_Slope_nw-se_WSE");


    }

    public Collection<String> getImageNames(){
        return mapping.values();
    }
}


