package tasimaProject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

class OdemeYontemiFactory {
    private static final Map<String, BiFunction<Double, Object[], OdemeYontemi>> paymentMethods = new HashMap<>();

    // Static block to register default payment methods
    static {
        registerPaymentMethod("KentKart", (tutar, params) -> new KentKart(tutar/*, params.length > 0 ? (double) params[0] : 0.0)*/));
        registerPaymentMethod("KrediKarti", (tutar, params) -> new KrediKarti(tutar));
        registerPaymentMethod("Nakit", (tutar, params) -> new Nakit(tutar));
    }

    public static Map<String, BiFunction<Double, Object[], OdemeYontemi>> getPaymentMethods() {
        return paymentMethods;
    }

    // Register new payment methods dynamically
    public static void registerPaymentMethod(String key, BiFunction<Double, Object[], OdemeYontemi> constructor) {
        paymentMethods.put(key, constructor);
    }

    // Factory method
    public static OdemeYontemi factoryMethod(String type, double tutar, Object... params) {
        BiFunction<Double, Object[], OdemeYontemi> constructor = paymentMethods.get(type);
        if (constructor == null) {
            throw new IllegalArgumentException("Unknown payment type: " + type);
        }
        return constructor.apply(tutar, params);
    }
}