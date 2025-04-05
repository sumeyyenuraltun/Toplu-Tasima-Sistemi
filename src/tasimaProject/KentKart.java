package tasimaProject;

class KentKart extends OdemeYontemi {
    private double bakiye;

    public KentKart(double tutar) {
        super(tutar);
        this.bakiye =10;
    }

    public String bakiyeSorgula() {
        if (bakiye >= tutar) {
            return "KentKart bakiyesi: " + String.format("%.2f", bakiye) + " TL";
        } else {
            return "KentKart bakiyesi: " + String.format("%.2f", bakiye)+ "  Bakiyeniz yetersiz! Lütfen kartınızı doldurunuz.";
        }
    }

    @Override
    public String odemeYap() {

            return "KentKart ile " + String.format("%.2f", tutar) + " TL ödeme var.";

    }
}