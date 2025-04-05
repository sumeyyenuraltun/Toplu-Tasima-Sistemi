package tasimaProject;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;

import java.util.List;

public class GraphManager {
    private Graph graph;

    public GraphManager() {
        graph = new SingleGraph("TransportationNetwork");
        System.setProperty("org.graphstream.ui", "swing");
    }

    public void createGraphFromDuraklar(List<Duraklar> durakList) {
        for (Duraklar durak : durakList) {
            Node node = graph.addNode(durak.getId());
            node.setAttribute("ui.label", durak.getName());
            node.setAttribute("type", durak.getType());
            node.setAttribute("lat", durak.getLat());
            node.setAttribute("lon", durak.getLon());
            node.setAttribute("Vehicle", durak.getVehicle());
        }
        for (Duraklar durak : durakList) {
            if (durak.getVehicle().getNextStops() != null) {
                for (nextStops next : durak.getVehicle().getNextStops()){
                    String edgeId = durak.getId() + "_to_" + next.getStopId();
                    System.out.println("Checking nextStop: " + edgeId);


                    if (graph.getEdge(edgeId) == null) {
                        Edge edge = graph.addEdge(edgeId, durak.getId(), next.getStopId(), true);
                        edge.setAttribute("distance", next.getMesafe());
                        edge.setAttribute("duration", next.getSure());
                        edge.setAttribute("cost", next.getUcret());
                        System.out.println("Edge created: " + edgeId);
                    } else {
                        System.out.println("Edge already exists: " + edgeId);
                    }
                }
            } else {
                System.out.println("No nextStop for " + durak.getId());
            }

            if (durak.getVehicle().getTransfer() != null) {
                for(Transfer transfer: durak.getVehicle().getTransfer()) {
                    String transferEdgeId = durak.getId() + "_transfer_" + transfer.getTransferStopId();
                    System.out.println("Checking transfer: " + transferEdgeId);
                    if (graph.getEdge(transferEdgeId) == null) {
                        Edge transferEdge = graph.addEdge(transferEdgeId, durak.getId(), transfer.getTransferStopId(), true);
                        transferEdge.setAttribute("type", "transfer");
                        transferEdge.setAttribute("duration", transfer.getTransferSure());
                        transferEdge.setAttribute("cost", transfer.getTransferUcret());
                        System.out.println("Transfer edge created: " + transferEdgeId);
                    } else {
                        System.out.println("Transfer edge already exists: " + transferEdgeId);
                    }
                }
            } else {
                System.out.println("No transfer for " + durak.getId());
            }
        }
    }

    public Graph getGraph() {
        return graph;
    }


    public List<Path> findAllRoutes(String startId, String endId) {

        return null;
    }
}
