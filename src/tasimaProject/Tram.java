package tasimaProject;

import java.util.List;

public class Tram extends Vehicle{
    List<nextStops> TramNextStops;
    List<Transfer> TramTransfer;

    public Tram(List<nextStops> TramNext, List<Transfer> TramTransfers) {
        this.TramNextStops = TramNext;
        this.TramTransfer = TramTransfers;
    }

    public List<nextStops> getNextStops() {
        return TramNextStops;
    }
    public List<Transfer> getTransfer(){
        return TramTransfer;
    }

}
