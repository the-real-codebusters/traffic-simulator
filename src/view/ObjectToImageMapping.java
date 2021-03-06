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
        } else if (gamemode.equals("VitaExMachina")){
            createVitaExMachinaMapping();
        }
        else {
                // Hier könnten weitere Szenarien eingebunden werden
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


        // Flache Rails (unvollständig)
        mapping.put("rail-ne-sw", "rail/rail-ne-sw");
        mapping.put("rail-nw-se", "rail/rail-nw-se");
        mapping.put("rail-ne-se", "rail/rail-ne-se");
        mapping.put("rail-nw-sw", "rail/rail-nw-sw");
        mapping.put("rail-ne-nw", "rail/rail-ne-nw");
        mapping.put("rail-se-sw", "rail/rail-se-sw");
        mapping.put("railcurve-ne-s", "rail/railcurve-ne-s");
        mapping.put("railcurve-ne-w", "rail/railcurve-ne-w");
        mapping.put("railcurve-nw-e", "rail/railcurve-nw-e");
        mapping.put("railcurve-nw-s", "rail/railcurve-nw-s");
        mapping.put("railcurve-se-n", "rail/railcurve-se-n");
        mapping.put("railcurve-se-w", "rail/railcurve-se-w");
        mapping.put("railcurve-sw-e", "rail/railcurve-sw-e");
        mapping.put("railcurve-sw-n", "rail/railcurve-sw-n");
        mapping.put("railswitch-ne-s", "rail/railswitch-ne-s");
        mapping.put("railswitch-nw-s", "rail/railswitch-nw-s");
        mapping.put("railswitch-se-n", "rail/railswitch-se-n");
        mapping.put("railswitch-sw-n", "rail/railswitch-sw-n");
        mapping.put("railswitch-ne-w", "rail/railswitch-ne-w");
        mapping.put("railswitch-nw-e", "rail/railswitch-nw-e");
        mapping.put("railswitch-se-w", "rail/railswitch-se-w");
        mapping.put("railswitch-sw-e", "rail/railswitch-sw-e");

        mapping.put("railcrossing", "rail/railroadcrossing");
        mapping.put("railsignal-ne-sw", "rail/railsignal-ne-sw");
        mapping.put("railsignal-nw-se", "rail/railsignal-nw-se");
        mapping.put("railsignal", "rail/railsignal");

        // Stations und Airport
        mapping.put("railstation-nw-se", "stations/rail/railstation_nw_se");
        mapping.put("railstation-ne-sw", "stations/rail/railstation-ne-sw");
        mapping.put("busstop-nw-se", "stations/road/busstop-nw-se");
        mapping.put("busstop-ne-sw", "stations/road/busstop-ne-sw");
        mapping.put("runway", "airport/runway");
        mapping.put("taxiway", "airport/taxiway");
        mapping.put("big tower", "airport/big tower");
        mapping.put("tower", "airport/tower");
        mapping.put("terminal", "airport/terminal");

        // Verschiedenes
        mapping.put("height_up" , "height_up");
        mapping.put("height_down" , "height_down");
        mapping.put("remove", "remove");

        //Fahrzeuge
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

        mapping.put("bulk_wagon-ne", "vehicles/rail/bulk_wagon_ne");
        mapping.put("bulk_wagon-nw", "vehicles/rail/bulk_wagon_nw");
        mapping.put("bulk_wagon-se", "vehicles/rail/bulk_wagon_se");
        mapping.put("bulk_wagon-sw", "vehicles/rail/bulk_wagon_sw");

        mapping.put("hazmat_wagon-ne", "vehicles/rail/hazmat_wagon_ne");
        mapping.put("hazmat_wagon-nw", "vehicles/rail/hazmat_wagon_nw");
        mapping.put("hazmat_wagon-se", "vehicles/rail/hazmat_wagon_se");
        mapping.put("hazmat_wagon-sw", "vehicles/rail/hazmat_wagon_sw");

        mapping.put("panel_wagon-ne", "vehicles/rail/panel_wagon_ne");
        mapping.put("panel_wagon-nw", "vehicles/rail/panel_wagon_nw");
        mapping.put("panel_wagon-se", "vehicles/rail/panel_wagon_se");
        mapping.put("panel_wagon-sw", "vehicles/rail/panel_wagon_sw");

        mapping.put("the_engine-ne", "vehicles/rail/the_engine_ne");
        mapping.put("the_engine-nw", "vehicles/rail/the_engine_nw");
        mapping.put("the_engine-se", "vehicles/rail/the_engine_se");
        mapping.put("the_engine-sw", "vehicles/rail/the_engine_sw");


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


    private void createVitaExMachinaMapping(){

        // Fabriken
        mapping.put("data processing", "factories_test/data processing");
        mapping.put("mechanical engineering", "factories_test/mechanical engineering");
        mapping.put("recycling factory", "factories_test/recycling factory");
        mapping.put("steel factory", "factories_test/steel factory");
        mapping.put("emotion research department", "factories_test/emotion research department");
        mapping.put("final factory", "factories_test/final factory");
        mapping.put("fusion power plant", "factories_test/fusion power plant");
        mapping.put("human research department", "factories_test/human research department");
        mapping.put("institut of quantum physics", "factories_test/institut of quantum physics");
        mapping.put("iron ore mine", "factories_test/iron ore mine");
        mapping.put("nanotechnology lab", "factories_test/nanotechnology lab");

        //Nature
        mapping.put("plant_hologram", "ground/plant");
        mapping.put("neotree", "ground/tree");
        mapping.put("plantandneotree", "ground/plantandtree");
        mapping.put("crystal_stones", "ground/stones_one");
        mapping.put("diffuse_matter", "ground/stones_two");
        mapping.put("water", "ground/wasserrosa");

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
        mapping.put("grass", "ground/grass");

        mapping.put("height_up" , "height_up");
        mapping.put("height_down" , "height_down");
        mapping.put("remove", "remove");


        mapping.put("rail-ne-sw", "rail/rail-ne-sw");
        mapping.put("rail-nw-se", "rail/rail-nw-se");
        mapping.put("rail-ne-se", "rail/rail-ne-se");
        mapping.put("rail-nw-sw", "rail/rail-nw-sw");
        mapping.put("rail-ne-nw", "rail/rail-ne-nw");
        mapping.put("rail-se-sw", "rail/rail-se-sw");
        mapping.put("railcurve-ne-s", "rail/railcurve-ne-s");
        mapping.put("railcurve-ne-w", "rail/railcurve-ne-w");
        mapping.put("railcurve-nw-e", "rail/railcurve-nw-e");
        mapping.put("railcurve-nw-s", "rail/railcurve-nw-s");
        mapping.put("railcurve-se-n", "rail/railcurve-se-n");
        mapping.put("railcurve-se-w", "rail/railcurve-se-w");
        mapping.put("railcurve-sw-e", "rail/railcurve-sw-e");
        mapping.put("railcurve-sw-n", "rail/railcurve-sw-n");
        mapping.put("railswitch-ne-s", "rail/railswitch-ne-s");
        mapping.put("railswitch-nw-s", "rail/railswitch-nw-s");
        mapping.put("railswitch-se-n", "rail/railswitch-se-n");
        mapping.put("railswitch-sw-n", "rail/railswitch-sw-n");
        mapping.put("railswitch-ne-w", "rail/railswitch-ne-w");
        mapping.put("railswitch-nw-e", "rail/railswitch-nw-e");
        mapping.put("railswitch-se-w", "rail/railswitch-se-w");
        mapping.put("railswitch-sw-e", "rail/railswitch-sw-e");

        mapping.put("railsignal-ne-sw", "rail/railsignal-ne-sw");
        mapping.put("railsignal-nw-se", "rail/railsignal-nw-se");
        mapping.put("railsignal", "rail/railsignal");


        // Flache Straßen-Tiles
        mapping.put("road-ne", "road/flat/road_ne");
        mapping.put("road-nw", "road/flat/road_nw");
        mapping.put("road-se", "road/flat/road_se");
        mapping.put("road-sw", "road/flat/road_sw");
        mapping.put("road-ne-nw-se-sw", "road/flat/road_ne_nw_se_sw");
        mapping.put("road-ne-nw-se", "road/flat/road_ne_nw_se");
        mapping.put("road-ne-nw-sw", "road/flat/road_ne_nw_sw");
        mapping.put("road-ne-nw", "road/flat/road_ne_nw");
        mapping.put("road-ne-se-sw", "road/flat/road_ne_se_sw");
        mapping.put("road-ne-se", "road/flat/road_ne_se");
        mapping.put("road-ne-sw", "road/flat/road_ne_sw");
        mapping.put("road-nw-se-sw", "road/flat/road_nw_se_sw");
        mapping.put("road-nw-se", "road/flat/road_nw_se");
        mapping.put("road-nw-sw", "road/flat/road_nw_sw");
        mapping.put("road-se-sw", "road/flat/road_se_sw");

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

        //Stations
        mapping.put("busstop-nw-se", "stations/road/busstop-nw-se");
        mapping.put("busstop-ne-sw", "stations/road/busstop-ne-sw");
        mapping.put("railstation-nw-se", "stations/rail/railstation-nw-se");
        mapping.put("railstation-ne-sw", "stations/rail/railstation-ne-sw");

        //Fahrzeuge RAIL
        mapping.put("the_engine-ne", "vehicles/rail/the_engine-ne");
        mapping.put("the_engine-nw", "vehicles/rail/the_engine-nw");
        mapping.put("the_engine-se", "vehicles/rail/the_engine-se");
        mapping.put("the_engine-sw", "vehicles/rail/the_engine-sw");
        mapping.put("emotions_wagon-ne", "vehicles/rail/emotions_wagon_ne");
        mapping.put("emotions_wagon-nw", "vehicles/rail/emotions_wagon_nw");
        mapping.put("emotions_wagon-se", "vehicles/rail/emotions_wagon_se");
        mapping.put("emotions_wagon-sw", "vehicles/rail/emotions_wagon_sw");
        mapping.put("quantum_wagon-ne", "vehicles/rail/quantum_wagon_ne");
        mapping.put("quantum_wagon-nw", "vehicles/rail/quantum_wagon_nw");
        mapping.put("quantum_wagon-se", "vehicles/rail/quantum_wagon_se");
        mapping.put("quantum_wagon-sw", "vehicles/rail/quantum_wagon_sw");

        //Fahrzeuge ROAD
        mapping.put("data_bus-ne", "vehicles/road/data_bus-ne");
        mapping.put("data_bus-nw", "vehicles/road/data_bus-nw");
        mapping.put("data_bus-se", "vehicles/road/data_bus-se");
        mapping.put("data_bus-sw", "vehicles/road/data_bus-sw");

        mapping.put("nanospeeder-ne", "vehicles/road/nanospeeder-ne");
        mapping.put("nanospeeder-nw", "vehicles/road/nanospeeder-nw");
        mapping.put("nanospeeder-se", "vehicles/road/nanospeeder-se");
        mapping.put("nanospeeder-sw", "vehicles/road/nanospeeder-sw");

        mapping.put("recycling_truck-ne", "vehicles/road/recycling-truck_ne");
        mapping.put("recycling_truck-nw", "vehicles/road/recycling-truck_nw");
        mapping.put("recycling_truck-se", "vehicles/road/recycling-truck_se");
        mapping.put("recycling_truck-sw", "vehicles/road/recycling-truck_sw");


        //Fahrzeuge AIR
        mapping.put("ufo_steel-ne", "vehicles/air/ufo_steel-ne");
        mapping.put("ufo_steel-nw", "vehicles/air/ufo_steel-nw");
        mapping.put("ufo_steel-se", "vehicles/air/ufo_steel-se");
        mapping.put("ufo_steel-sw", "vehicles/air/ufo_steel-sw");

        mapping.put("ufo_iron_ore-ne", "vehicles/air/ufo_iron_ore-ne");
        mapping.put("ufo_iron_ore-nw", "vehicles/air/ufo_iron_ore-nw");
        mapping.put("ufo_iron_ore-se", "vehicles/air/ufo_iron_ore-se");
        mapping.put("ufo_iron_ore-sw", "vehicles/air/ufo_iron_ore-sw");

        mapping.put("ufo_robot_pieces-ne", "vehicles/air/ufo_robot_pieces-ne");
        mapping.put("ufo_robot_pieces-nw", "vehicles/air/ufo_robot_pieces-nw");
        mapping.put("ufo_robot_pieces-se", "vehicles/air/ufo_robot_pieces-se");
        mapping.put("ufo_robot_pieces-sw", "vehicles/air/ufo_robot_pieces-sw");

        mapping.put("drone-ne", "vehicles/air/drone-ne");
        mapping.put("drone-nw", "vehicles/air/drone-nw");
        mapping.put("drone-se", "vehicles/air/drone-se");
        mapping.put("drone-sw", "vehicles/air/drone-sw");

        mapping.put("battery-ne", "vehicles/air/battery-ne");
        mapping.put("battery-nw", "vehicles/air/battery-nw");
        mapping.put("battery-se", "vehicles/air/battery-se");
        mapping.put("battery-sw", "vehicles/air/battery-sw");

        mapping.put("air_taxi-ne", "vehicles/air/air_taxi-ne");
        mapping.put("air_taxi-nw", "vehicles/air/air_taxi-nw");
        mapping.put("air_taxi-se", "vehicles/air/air_taxi-se");
        mapping.put("air_taxi-sw", "vehicles/air/air_taxi-sw");

        // Airport
        mapping.put("runway", "airport/runway");
        mapping.put("taxiway", "airport/taxiway");
        mapping.put("big tower", "airport/big tower");
        mapping.put("terminal", "airport/terminal");

    }

    public Collection<String> getImageNames(){
        return mapping.values();
    }
}


