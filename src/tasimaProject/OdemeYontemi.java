package tasimaProject;

abstract class OdemeYontemi {
    protected double tutar;

    public OdemeYontemi(double tutar) {
        this.tutar = tutar;
    }

    public abstract String odemeYap();
}
