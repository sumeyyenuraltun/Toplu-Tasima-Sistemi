package transport;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

import java.util.*;

public class calculateRouteAttributes {

    private double totalPrice, totalDistance, totalTime;
    private List<Node> path;

    public void Route(List<Node> Path) {
        this.path = Path;
        this.totalPrice = calculateTotalPrice(path);
        this.totalDistance = calculateTotalDistance(path);
        this.totalTime = calculateTOAwithoutTaxiAndWalk(path);
    }




    public double calculateTotalPrice(List<Node> path) {
        double totalPrice = 0.0;

        for (int i = 0; i < path.size() - 1; i++) {
            Node current = path.get(i);
            Node next = path.get(i + 1);

            Iterator<Edge> edgeIterator = current.leavingEdges().iterator();
            while (edgeIterator.hasNext()) {
                Edge edge = edgeIterator.next();
                if (edge.getTargetNode().equals(next)) {
                    if (edge.hasAttribute("cost")) {
                        double ucret = edge.getAttribute("cost", Double.class);
                        totalPrice += ucret;
                    }
                    break;
                }
            }
        }

        return totalPrice;
    }

    public double calculateTotalDistance(List<Node> path) {
        double totalDistance = 0.0;

        for (int i = 0; i < path.size() - 1; i++) {
            Node current = path.get(i);
            Node next = path.get(i + 1);

            Iterator<Edge> edgeIterator = current.leavingEdges().iterator();
            while (edgeIterator.hasNext()) {
                Edge edge = edgeIterator.next();
                if (edge.getTargetNode().equals(next)) {
                    if (edge.hasAttribute("distance")) {
                        double distance = edge.getAttribute("distance", Double.class);
                        totalDistance += distance;
                    }
                    break;
                }
            }
        }

        return totalDistance;
    }

    public double calculateTOAwithoutTaxiAndWalk(List<Node> path) {
        double totalTime = 0.0;

        for (int i = 0; i < path.size() - 1; i++) {
            Node current = path.get(i);
            Node next = path.get(i + 1);

            Iterator<Edge> edgeIterator = current.leavingEdges().iterator();
            while (edgeIterator.hasNext()) {
                Edge edge = edgeIterator.next();
                if (edge.getTargetNode().equals(next)) {
                    if (edge.hasAttribute("duration")) {
                        Double time = edge.getAttribute("duration", Double.class);
                        if (time != null) {
                            totalTime += time;
                        } else {
                            System.out.println("\n(null value detected)\n");
                        }
                    } else {
                        System.out.println("\n(attribute not found)\n");
                    }
                    break;
                }
            }
        }

        return totalTime;
    }


}
