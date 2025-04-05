package tasimaProject;

import java.util.List;

public class Bus extends Vehicle{
    private double ticketPrice;
    private double timeofArrival;

    List<nextStops> busNextStops;
    List<Transfer> busTransfer;

    // Constructor with properly typed parameters
    public Bus(List<nextStops> busNext, List<Transfer> busTransfers) {
        this.busNextStops = busNext;
        this.busTransfer = busTransfers;
    }

    public List<nextStops> getNextStops() {
        return busNextStops;
    }
    public List<Transfer> getTransfer(){
        return busTransfer;
    }

}
