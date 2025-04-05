package tasimaProject;

public class Transfer {
    private String transferStopId;
    private double transferSure;
    private double transferUcret;

    public Transfer(String transferStopId, double transferSure, double transferUcret) {
        this.transferStopId = transferStopId;
        this.transferSure = transferSure;
        this.transferUcret = transferUcret;
    }

    public String getTransferStopId() {
        return transferStopId;
    }

    public void setTransferStopId(String transferStopId) {
        this.transferStopId = transferStopId;
    }

    public double getTransferSure() {
        return transferSure;
    }

    public void setTransferSure(int transferSure) {
        this.transferSure = transferSure;
    }

    public double getTransferUcret() {
        return transferUcret;
    }

    public void setTransferUcret(double transferUcret) {
        this.transferUcret = transferUcret;
    }
}