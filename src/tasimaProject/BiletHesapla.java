
package tasimaProject;

public class BiletHesapla {
	 public double calculatePrice(Passenger passenger, double toplamUcret) {
	        return toplamUcret - (toplamUcret* passenger.getIndirimOrani());
	    }
}
