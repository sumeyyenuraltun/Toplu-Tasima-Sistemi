package tasimaProject;

import java.util.HashMap;
import java.util.Map;

public class Taxi extends Vehicle{
    double openingFee, costPerKm,speed;



    public Taxi(double OpeningFee, double CostPerKm, double speed){
        this.openingFee = OpeningFee;
        this.costPerKm = CostPerKm;
        this.speed = speed;
    }


    public double ucretHesapla(double distance){
        return this.openingFee + (costPerKm * distance);
    }
    public double sureHesapla(double distance) {
    	return distance*60/ this.speed;
    }

    public double getOpeningFee() { return openingFee; }
    public double getCostPerKm() { return costPerKm; }


    @Override
    public Map<String, Class<? extends OdemeYontemi>> getPaymentMethods() {
        Map<String, Class<? extends OdemeYontemi>> availablePayments = new HashMap<>(super.getPaymentMethods());
        availablePayments.remove("KentKart");  // Exclude KentKart for Taxi
        return availablePayments;
    }

    public String processPayment(String paymentType, double amount) {
        // Burada ödeme türüne göre ödeme işlemi yapılır
        if (getPaymentMethods().containsKey(paymentType)) {
            OdemeYontemi odemeYontemi = OdemeYontemiFactory.factoryMethod(paymentType, amount);
            return odemeYontemi.odemeYap();
        } else {
            return "Geçersiz ödeme yöntemi.";
        }
    }

}
