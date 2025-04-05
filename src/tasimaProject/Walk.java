package tasimaProject;

import java.util.HashMap;
import java.util.Map;

public class Walk extends Vehicle{
    double speed;
    public Walk(double speed){
        this.speed = speed;
    }
    
    public double sureHesapla(double distance) {
    	return distance*60/ this.speed;
    }

    @Override
    public String processPayment(String paymentType, double amount) {
        return "Ödeme gerekmiyor."; // Yürüyüş için ödeme yok
    }

}

