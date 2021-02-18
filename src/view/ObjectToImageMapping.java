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
        mapping.put("stone", "ground/stones_test");
//        mapping.put("tree_no_ground", "ground/trees_no_ground");
//        mapping.put("stone", "ground/stones");


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


        // Fahrzeuge
        mapping.put("car_ne", "road/car_ne");
        mapping.put("car_nw", "road/car_nw");
        mapping.put("car_se", "road/car_se");
        mapping.put("car_sw", "road/car_sw");


        mapping.put("height_up" , "height_up");
        mapping.put("height_down" , "height_down");

        mapping.put("remove", "remove");


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


        // Straßen-Tiles mit Höhenunterschied
        mapping.put("road-ne1100", "road/road_ne-sw_Slope_NE");
        mapping.put("road-ne1001", "road/road_ne-sw_Slope_NW");
        mapping.put("road-ne0110", "road/road_ne-sw_Slope_SE");
        mapping.put("road-ne0011", "road/road_ne-sw_Slope_SW");

        mapping.put("road-sw1100", "road/road_ne-sw_Slope_NE");
        mapping.put("road-sw1001", "road/road_ne-sw_Slope_NW");
        mapping.put("road-sw0110", "road/road_ne-sw_Slope_SE");
        mapping.put("road-sw0011", "road/road_ne-sw_Slope_SW");

        // se - nw funktionieren noch nicht
        mapping.put("road-se1100", "road/road_nw-se_Slope_NE");
        mapping.put("road-se1001", "road/road_nw-se_Slope_NW");
        mapping.put("road-se0110", "road/road_nw-se_Slope_SE");
        mapping.put("road-se0011", "road/road_nw-se_Slope_SW");

        mapping.put("road-nw1100", "road/road_nw-se_Slope_NE");
        mapping.put("road-nw1001", "road/road_nw-se_Slope_NW");
        mapping.put("road-nw0110", "road/road_nw-se_Slope_SE");
        mapping.put("road-nw0011", "road/road_nw-se_Slope_SW");


        mapping.put("road-ne-sw1100", "road/road_ne-sw_Slope_NE");
        mapping.put("road-ne-sw1001", "road/road_ne-sw_Slope_NW");
        mapping.put("road-ne-sw0110", "road/road_ne-sw_Slope_SE");
        mapping.put("road-ne-sw0011", "road/road_ne-sw_Slope_SW");





        mapping.put("road-ne-sw0100", "road/road_ne-sw_Slope_E");
        mapping.put("road-nw-se0100", "road/road_nw-se_Slope_E");

        mapping.put("road-ne-sw1101", "road/road_ne-sw_Slope_ENW");
        mapping.put("road-nw-se1101", "road/road_nw-se_Slope_ENW");

        mapping.put("road-nw-se0101", "road/road_nw-se_Slope_EW");
        mapping.put("road-ne-sw0101", "road/road_ne-sw_Slope_EW");

        mapping.put("road-nw-se1000", "road/road_nw-se_Slope_N");
        mapping.put("road-ne-sw1000", "road/road_ne-sw_Slope_N");

        mapping.put("road-nw-se1010", "road/road_nw-se_Slope_NS");
        mapping.put("road-ne-sw1010", "road/road_ne-sw_Slope_NS");

        mapping.put("road-nw-se1011", "road/road_nw-se_Slope_NWS");
        mapping.put("road-ne-sw1011", "road/road_ne-sw_Slope_NWS");

        mapping.put("road-nw-se0010", "road/road_nw-se_Slope_S");
        mapping.put("road-ne-sw0010", "road/road_ne-sw_Slope_S");

        mapping.put("road-nw-se1110", "road/road_nw-se_Slope_SEN");
        mapping.put("road-ne-sw1110", "road/road_ne-sw_Slope_SEN");

        mapping.put("road-nw-se0001", "road/road_nw-se_Slope_W");
        mapping.put("road-ne-sw0001", "road/road_ne-sw_Slope_W");

        mapping.put("road-nw-se0111", "road/road_nw-se_Slope_WSE");
        mapping.put("road-ne-sw0111", "road/road_ne-sw_Slope_WSE");




//        // ne nw
        mapping.put("road-ne0100", "road/road_ne-sw_Slope_E");
        mapping.put("road-nw0100", "road/road_nw-se_Slope_E");

        mapping.put("road-ne1101", "road/road_ne-sw_Slope_ENW");
        mapping.put("road-nw1101", "road/road_nw-se_Slope_ENW");

        mapping.put("road-nw0101", "road/road_nw-se_Slope_EW");
        mapping.put("road-ne0101", "road/road_ne-sw_Slope_EW");

        mapping.put("road-nw1000", "road/road_nw-se_Slope_N");
        mapping.put("road-ne1000", "road/road_ne-sw_Slope_N");

        mapping.put("road-nw1010", "road/road_nw-se_Slope_NS");
        mapping.put("road-ne1010", "road/road_ne-sw_Slope_NS");

        mapping.put("road-nw1011", "road/road_nw-se_Slope_NWS");
        mapping.put("road-ne1011", "road/road_ne-sw_Slope_NWS");

        mapping.put("road-nw0010", "road/road_nw-se_Slope_S");
        mapping.put("road-ne0010", "road/road_ne-sw_Slope_S");

        mapping.put("road-nw1110", "road/road_nw-se_Slope_SEN");
        mapping.put("road-ne1110", "road/road_ne-sw_Slope_SEN");

        mapping.put("road-nw0001", "road/road_nw-se_Slope_W");
        mapping.put("road-ne0001", "road/road_ne-sw_Slope_W");

        mapping.put("road-nw0111", "road/road_nw-se_Slope_WSE");
        mapping.put("road-ne0111", "road/road_ne-sw_Slope_WSE");



        // se sw
        mapping.put("road-sw0100", "road/road_ne-sw_Slope_E");
        mapping.put("road-se0100", "road/road_nw-se_Slope_E");

        mapping.put("road-sw1101", "road/road_ne-sw_Slope_ENW");
        mapping.put("road-se1101", "road/road_nw-se_Slope_ENW");

        mapping.put("road-se0101", "road/road_nw-se_Slope_EW");
        mapping.put("road-sw0101", "road/road_ne-sw_Slope_EW");

        mapping.put("road-se1000", "road/road_nw-se_Slope_N");
        mapping.put("road-sw1000", "road/road_ne-sw_Slope_N");

        mapping.put("road-se1010", "road/road_nw-se_Slope_NS");
        mapping.put("road-sw1010", "road/road_ne-sw_Slope_NS");

        mapping.put("road-se1011", "road/road_nw-se_Slope_NWS");
        mapping.put("road-sw1011", "road/road_ne-sw_Slope_NWS");

        mapping.put("road-se0010", "road/road_nw-se_Slope_S");
        mapping.put("road-sw0010", "road/road_ne-sw_Slope_S");

        mapping.put("road-se1110", "road/road_nw-se_Slope_SEN");
        mapping.put("road-sw1110", "road/road_ne-sw_Slope_SEN");

        mapping.put("road-se0001", "road/road_nw-se_Slope_W");
        mapping.put("road-sw0001", "road/road_ne-sw_Slope_W");

        mapping.put("road-se0111", "road/road_nw-se_Slope_WSE");
        mapping.put("road-sw0111", "road/road_ne-sw_Slope_WSE");

    }

    public Collection<String> getImageNames(){
        return mapping.values();
    }
}


