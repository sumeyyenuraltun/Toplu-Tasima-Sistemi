package tasimaProject;

class Nakit extends OdemeYontemi {
    public Nakit(double tutar) {
        super(tutar);
    }

    @Override
    public String odemeYap() {
        return "Nakit olarak " + String.format("%.2f", tutar) + " TL ödeme var.";
    }
}
