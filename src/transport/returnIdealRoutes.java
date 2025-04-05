package transport;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

import java.util.*;

public class returnIdealRoutes {

    public Map<String, List<Node>> returnIdealRoutes(List<List<Node>> allRoutes) {
        Map<String, List<Node>> routesAllAttributes = new HashMap<>();

        List<Double> routePrices = new ArrayList<>();
        List<Double> routeDurations = new ArrayList<>();
        List<Integer> routeTransfers = new ArrayList<>();

        for (List<Node> route : allRoutes) {
            double costCounter = 0, durationCounter = 0;
            int transferCounter = 0;

            for (int i = 0; i < route.size() - 1; i++) {
                Node nodeA = route.get(i);
                Node nodeB = route.get(i + 1);
                Edge edge = nodeA.getEdgeBetween(nodeB);

                if (edge != null) {
                    costCounter += (Double) edge.getAttribute("cost");
                    durationCounter += (Double) edge.getAttribute("duration");

                    if ("transfer".equals(edge.getAttribute("type"))) {
                        transferCounter++;
                    }
                }
            }

            routePrices.add(costCounter);
            routeDurations.add(durationCounter);
            routeTransfers.add(transferCounter);
        }

        int lowestCostIndex = routePrices.indexOf(Collections.min(routePrices));
        int shortestDurationIndex = routeDurations.indexOf(Collections.min(routeDurations));
        int leastTransferIndex = routeTransfers.indexOf(Collections.min(routeTransfers));

        if (lowestCostIndex != -1)
            routesAllAttributes.put("cost", allRoutes.get(lowestCostIndex));

        if (shortestDurationIndex != -1)
            routesAllAttributes.put("duration", allRoutes.get(shortestDurationIndex));

        if (leastTransferIndex != -1)
            routesAllAttributes.put("transfer", allRoutes.get(leastTransferIndex));

        return routesAllAttributes;
    }
}
