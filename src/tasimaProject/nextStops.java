package tasimaProject;

public class nextStops {
    private String stopId;
    private double mesafe;
    private double sure;
    private double ucret;

    public nextStops(String stopId, double mesafe, double sure, double ucret) {
        this.stopId = stopId;
        this.mesafe = mesafe;
        this.sure = sure;
        this.ucret = ucret;
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public double getMesafe() {
        return mesafe;
    }

    public void setMesafe(double mesafe) {
        this.mesafe = mesafe;
    }

    public double getSure() {
        return sure;
    }

    public void setSure(int sure) {
        this.sure = sure;
    }

    public double getUcret() {
        return ucret;
    }

    public void setUcret(double ucret) {
        this.ucret = ucret;
    }


}