package tasimaProject;
import org.graphstream.graph.Graph;

import java.io.IOException;
import java.util.List;


public class Main {

    public static void main(String[] args) {
        parseVeriSeti parser = new parseVeriSeti();

        try {
            List<Duraklar> duraklarList = parser.parseJson();

            System.out.println("Parsed Duraklar List:");
            System.out.println("--------------------");
            for (Duraklar durak : duraklarList) {
                System.out.println("ID: " + durak.getId());
                System.out.println("Name: " + durak.getName());
                System.out.println("Type: " + durak.getType());
                System.out.println("Location: (" + durak.getLat() + ", " + durak.getLon() + ")");
                System.out.println("Son Durak: " + durak.getSonDurak());

                if (durak.getVehicle().getTransfer() != null) {
                    for(Transfer transfer: durak.getVehicle().getTransfer()) {
                        System.out.println("Transfer Info:");
                        System.out.println("  - Stop ID: " + transfer.getTransferStopId());
                        System.out.println("  - Sure: " + transfer.getTransferSure());
                        System.out.println("  - Ucret: " + transfer.getTransferUcret());
                    }
                }
                if (durak.getVehicle().getNextStops() != null) {
                    for(nextStops next: durak.getVehicle().getNextStops()) {
                        System.out.println("Next Stop Info:");
                        System.out.println("  - Stop ID: " + next.getStopId());
                        System.out.println("  - Mesafe: " + next.getMesafe());
                        System.out.println("  - Sure: " + next.getSure());
                        System.out.println("  - Ucret: " + next.getUcret());
                    }
                }

                System.out.println("--------------------");
            }

            GraphManager graphManager = new GraphManager();
            graphManager.createGraphFromDuraklar(duraklarList);

            Graph graph = graphManager.getGraph();

            MapVisualizer.visualize(graph);

        } catch (IOException e) {
            System.err.println("Error reading JSON file:");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error:");
            e.printStackTrace();
        }
    }

}
