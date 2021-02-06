package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Die Punkte, die das Fahrzeug nacheinander ansteuern soll und die jeweilige Distanz vom vorherigen Punkt
//zu diesem Punkt. Inklusive der Startposition
public class VehicleMovement {

    public Map<PositionOnTilemap, Double> positionsOnMapAndTravelDistance = new HashMap<>();
}
