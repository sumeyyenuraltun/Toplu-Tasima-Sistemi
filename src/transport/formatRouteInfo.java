package transport;

import org.graphstream.graph.Graph;
import tasimaProject.*;



import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

import java.util.List;

public class formatRouteInfo {
    public String formatRouteInfo(List<Node> path, Passenger yolcu, boolean ozelGunMu) {
        if (path == null || path.isEmpty()) {
            return "No valid route found.";
        }

        calculateRouteAttributes calcRouteAttributes = new calculateRouteAttributes();
        double totalDistance = calcRouteAttributes.calculateTotalDistance(path);

        StringBuilder info = new StringBuilder();

        info.append("Toplam Mesafe: ").append(String.format("%.2f", totalDistance)).append(" km\n");
        info.append("Duraklar:\n");

        for (int i = 0; i < path.size()-1; i++) {
            Node nodeA = path.get(i);
            Node nodeB = path.get(i+1);
            String nodeAName = nodeA.hasAttribute("ui.label") ?
                    nodeA.getAttribute("ui.label", String.class) : nodeA.getId();
            String nodeBName = nodeB.hasAttribute("ui.label") ?
                    nodeB.getAttribute("ui.label", String.class) : nodeB.getId();

            Edge edge = nodeA.getEdgeBetween(nodeB);

            double cost = (double) edge.getAttribute("cost")*(1-yolcu.getIndirimOrani());
            if(ozelGunMu) cost = 0;

            info.append(i + 1).append(". ").append(nodeAName).append(" -> ").append(nodeBName);

            if("transfer".equals(edge.getAttribute("type"))) {

                info.append(" ( \u21BB Transfer )");
            } else {
                String vehicleType = nodeA.getAttribute("Vehicle").getClass().getSimpleName();
                if (vehicleType.toLowerCase().contains("tram")) {

                    info.append(" ( \uD83D\uDE8B Tramvay )");
                } else if (vehicleType.toLowerCase().contains("bus")) {

                    info.append(" ( \uD83D\uDE8C Otobüs )");
                } else {

                    info.append(" ( " + vehicleType + " )");
                }
            }
            info.append("\n");


            info.append("\u231A ").append("Süre: " + edge.getAttribute("duration") + " dk\n");
            info.append("\uD83D\uDCB0 ");

            if (yolcu.getIndirimOrani() == 0 && !ozelGunMu)
                info.append(String.format("Ücret: %.2f TL\n", cost));
            else if (yolcu.getIndirimOrani() == 0 && ozelGunMu)
                info.append(String.format("Ücret: %.2f TL (Özel Gün %.2f TL -> %.2f TL)\n",
                        cost, (1-yolcu.getIndirimOrani()) * (double) edge.getAttribute("cost"), cost));
            else if (yolcu.getIndirimOrani() > 0 && !ozelGunMu)
                info.append(String.format("Ücret: %.2f TL (%s (%%%d) %.2f TL -> %.2f TL)\n",
                        cost, yolcu.getClass().getSimpleName(), (int) (yolcu.getIndirimOrani() * 100),
                        (double) edge.getAttribute("cost"), cost));
            else if (yolcu.getIndirimOrani() > 0 && ozelGunMu)
                info.append(String.format("Ücret: %.2f TL (%s (%%%d) %.2f TL -> %.2f TL) (Özel Gün %.2f TL -> %.2f TL)\n",
                        cost, yolcu.getClass().getSimpleName(), (int) (yolcu.getIndirimOrani() * 100),
                        (double) edge.getAttribute("cost"),
                        (1-yolcu.getIndirimOrani()) * (double) edge.getAttribute("cost"),
                        (1-yolcu.getIndirimOrani()) * (double) edge.getAttribute("cost"), cost));
        }
        info.append("");
        return info.toString();
    }
}
