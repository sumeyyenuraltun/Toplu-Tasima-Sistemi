package tasimaProject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Vehicle {
    private static final Map<String, Class<? extends OdemeYontemi>> paymentMethods = new HashMap<>();

    static {
        paymentMethods.put("KentKart", KentKart.class);
        paymentMethods.put("KrediKarti", KrediKarti.class);
        paymentMethods.put("Nakit", Nakit.class);
    }

    public Map<String, Class<? extends OdemeYontemi>> getPaymentMethods() {
        return new HashMap<>(paymentMethods);
    }

    public String processPayment(String paymentType, double amount) {
        // Eğer ödeme türü geçerli değilse, geçersiz ödeme olduğunu belirtiyoruz
        if (!getPaymentMethods().containsKey(paymentType)) {
            return "Geçersiz ödeme yöntemi.";
        }

        // Geçerli ödeme türü ise ödeme nesnesi oluşturulup ödeme yapılır
        OdemeYontemi odemeYontemi = OdemeYontemiFactory.factoryMethod(paymentType, amount);
        return odemeYontemi.odemeYap();  // Ödeme işlemi gerçekleştirilir
    }

    public List<nextStops> getNextStops() {
        return new ArrayList<>();
    }

    public List<Transfer> getTransfer() {
        return new ArrayList<>();
    }

    public double ucretHesapla(double distance) {
        // Varsayılan işlem: Sabit bir ücret döndürülür.
        return 0.0;
    }

    // Süre hesaplama metodu (Varsayılan implementasyon)
    public double sureHesapla(double distance) {
        // Varsayılan işlem: 0 süre döndürülür.
        return 0.0;
    }
}