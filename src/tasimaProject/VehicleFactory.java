package tasimaProject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class VehicleFactory {
    private static final Map<String, Function<Object[], Vehicle>> vehicleCreators = new HashMap<>();

    static {
        // Farklı araç tiplerini Factory'ye tanıtıyoruz
        registerVehicle("tram", params -> new Tram((List<nextStops>) params[0], (List<Transfer>) params[1]));
        registerVehicle("taksi", params -> new Taxi((double) params[0], (double) params[1], (double) params[2]));
        registerVehicle("bus", params -> new Bus((List<nextStops>) params[0], (List<Transfer>) params[1]));
        registerVehicle("walk", params -> new Walk((double) params[0]));
    }

    public static void registerVehicle(String type, Function<Object[], Vehicle> creator) {
        vehicleCreators.put(type, creator);
    }

    public static Vehicle createVehicle(String type, Object... params) {
        Function<Object[], Vehicle> creator = vehicleCreators.get(type);
        if (creator != null) {
            return creator.apply(params);
        }
        throw new IllegalArgumentException("Geçersiz araç türü: " + type);
    }
}
