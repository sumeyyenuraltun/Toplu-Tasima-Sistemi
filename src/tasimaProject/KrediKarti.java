package tasimaProject;

class KrediKarti extends OdemeYontemi {
    private double zamOrani;


    public KrediKarti(double tutar) {
        super(tutar);
        this.zamOrani = 0.3;

    }

    private double hesaplaZamliTutar() {

        return tutar * (1 + zamOrani);
    }

    @Override
    public String odemeYap() {
        double odenecekTutar = hesaplaZamliTutar();
        return "Kredi Kartı ile " + String.format("%.2f", odenecekTutar) + " TL ödeme var.";
    }
}
