package tasimaProject;

import java.util.HashMap;
import java.util.Map;

public class PassengerCreator {
	private static final Map<String, Class<? extends Passenger>> passengerTypes = new HashMap<>();

    static {
    	passengerTypes.put("Genel", General.class);
    	passengerTypes.put("Öğrenci", Student.class);
    	passengerTypes.put("Yaşlı", Elderly.class);
        passengerTypes.put("Engelli", Disabled.class);
    }

    public static Map<String, Class<? extends Passenger>> getPassengerTypes() {
        return passengerTypes;
    }

    public static String[] getPassengerTypesArray() {
        return passengerTypes.keySet().toArray(new String[0]);
    }

    public static Passenger createPassenger(String type) {
        Class<? extends Passenger> passengerClass = passengerTypes.get(type);
        if (passengerClass != null) {
            try {
                return passengerClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Yolcu oluşturulurken hata oluştu.", e);
            }
        }
        throw new IllegalArgumentException("Geçersiz yolcu türü: " + type);
    }

    public static String getPassengerType(Passenger yolcu) {
        for (Map.Entry<String, Class<? extends Passenger>> entry : passengerTypes.entrySet()) {
            if (entry.getValue().equals(yolcu.getClass())) {
                return entry.getKey();
            }
        }
        return "Bilinmeyen Yolcu Türü";
    }

}
