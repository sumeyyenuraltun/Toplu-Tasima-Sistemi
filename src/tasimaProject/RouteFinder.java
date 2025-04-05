package tasimaProject;

import transport.*;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import org.graphstream.graph.Edge;

import java.util.*;

public class RouteFinder {
    private final Graph graph;
    private final Map<String, List<List<Node>>> allRoutesCache;
    private final Map<String, List<Node>> shortestRoutesCache;

    public RouteFinder(Graph graph) {
        this.graph = graph;
        this.allRoutesCache = new HashMap<>();
        this.shortestRoutesCache = new HashMap<>();
    }


    public List<List<Node>> findAllRoutes(Node startNode, Node endNode) {
        if (startNode == null || endNode == null) {
            System.out.println("Start node or end node is null");
            return new ArrayList<>();
        }

        String startId = startNode.getId();
        String endId = endNode.getId();
        String cacheKey = startId + "_" + endId;
        
        if (allRoutesCache.containsKey(cacheKey)) {
            return allRoutesCache.get(cacheKey);
        }

        System.out.println("Finding all routes from " + startId + " to " + endId);

        System.out.println("Nodes in the graph:");
        graph.nodes().forEach(node -> System.out.println("- " + node.getId()));

        System.out.println("Edges in the graph:");
        graph.edges().forEach(edge -> System.out.println("- " + edge.getSourceNode().getId() + " -> " + edge.getTargetNode().getId()));

        List<List<Node>> allRoutes = new ArrayList<>();

        if (startNode.equals(endNode)) {
            List<Node> singleNodePath = new ArrayList<>();
            singleNodePath.add(startNode);
            allRoutes.add(singleNodePath);
            return allRoutes;
        }


        Map<String, List<List<Node>>> allPaths = new HashMap<>();

        List<List<Node>> startPaths = new ArrayList<>();
        List<Node> initialPath = new ArrayList<>();
        initialPath.add(startNode);
        startPaths.add(initialPath);
        allPaths.put(startId, startPaths);

        PriorityQueue<PathDistance> queue = new PriorityQueue<>();
        queue.add(new PathDistance(initialPath, 0.0));
        

        Set<String> seenRoutes = new HashSet<>();

        calculateRouteAttributes getRouteAttributes = new calculateRouteAttributes();

        while (!queue.isEmpty()) {
            PathDistance currentPath = queue.poll();
            List<Node> path = currentPath.path;

            Node lastNode = path.get(path.size() - 1);
            String lastNodeId = lastNode.getId();

            if (lastNode.equals(endNode)) {
                if (!containsEquivalentPath(allRoutes, path)) {
                    allRoutes.add(new ArrayList<>(path));
                    double totalPrice = getRouteAttributes.calculateTotalPrice(path);
                    double totalDistance = getRouteAttributes.calculateTotalDistance(path);
                    double totalTimeWithoutTaxiorWalk = getRouteAttributes.calculateTOAwithoutTaxiAndWalk(path);
                    System.out.println("Found path to destination: " + pathToString(path));
                    System.out.println("Toplam Fiyat: " + totalPrice + " TL");
                }
                continue;
            }

            Iterator<Edge> edges = lastNode.leavingEdges().iterator();
            while (edges.hasNext()) {
                Edge edge = edges.next();
                Node nextNode = edge.getTargetNode();
                

                if (containsNode(path, nextNode)) {
                    continue;
                }

                double edgeDistance = 0.0;
                if (edge.hasAttribute("distance")) {
                    edgeDistance = edge.getAttribute("distance", Double.class);
                }

                List<Node> newPath = new ArrayList<>(path);
                newPath.add(nextNode);

                double newDistance = currentPath.distance + edgeDistance;

                String pathKey = pathToString(newPath);

                if (!seenRoutes.contains(pathKey)) {
                    queue.add(new PathDistance(newPath, newDistance));
                    seenRoutes.add(pathKey);

                    allPaths.computeIfAbsent(nextNode.getId(), k -> new ArrayList<>()).add(newPath);
                }
            }
        }

        calculateRouteAttributes calcRouteAtt = new calculateRouteAttributes();
        allRoutes.sort(Comparator.comparingDouble(calcRouteAtt::calculateTotalDistance));


        System.out.println("Found " + allRoutes.size() + " routes in total");

        allRoutesCache.put(cacheKey, allRoutes);
        return allRoutes;
    }

    public List<List<Node>> findAllRoutes(String startId, String endId) {
        Node startNode = graph.getNode(startId);
        Node endNode = graph.getNode(endId);
        return findAllRoutes(startNode, endNode);
    }

    private static class PathDistance implements Comparable<PathDistance> {
        List<Node> path;
        double distance;
        
        PathDistance(List<Node> path, double distance) {
            this.path = path;
            this.distance = distance;
        }
        
        @Override
        public int compareTo(PathDistance other) {
            return Double.compare(this.distance, other.distance);
        }
    }
    
    private boolean containsNode(List<Node> path, Node targetNode) {
        for (Node node : path) {
            if (node.equals(targetNode)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean containsEquivalentPath(List<List<Node>> allPaths, List<Node> path) {
        for (List<Node> existingPath : allPaths) {
            if (existingPath.size() != path.size()) {
                continue;
            }
            
            boolean equivalent = true;
            for (int i = 0; i < path.size(); i++) {
                if (!existingPath.get(i).equals(path.get(i))) {
                    equivalent = false;
                    break;
                }
            }
            
            if (equivalent) {
                return true;
            }
        }
        return false;
    }
    
    private String pathToString(List<Node> path) {
        StringBuilder sb = new StringBuilder();
        for (Node node : path) {
            sb.append(node.getId()).append(" -> ");
        }
        if (sb.length() > 4) {
            sb.setLength(sb.length() - 4); // Remove trailing " -> "
        }
        return sb.toString();
    }


    public List<Node> findShortestRoute(Node startNode, Node endNode) {
        if (startNode == null || endNode == null) {
            System.out.println("Start node or end node is null");
            return new ArrayList<>();
        }

        String cacheKey = startNode.getId() + "_" + endNode.getId();
        if (shortestRoutesCache.containsKey(cacheKey)) {
            return shortestRoutesCache.get(cacheKey);
        }

        System.out.println("Finding shortest route from " + startNode.getId() + " to " + endNode.getId());
        
        List<List<Node>> allRoutes = findAllRoutes(startNode, endNode);
        
        if (allRoutes.isEmpty()) {
            System.out.println("No routes found between " + startNode.getId() + " and " + endNode.getId());
            return new ArrayList<>();
        }

        List<Node> shortestRoute = allRoutes.get(0);

        calculateRouteAttributes routeAtt = new calculateRouteAttributes();


        System.out.println("Shortest route: " + pathToString(shortestRoute));
        System.out.println("Distance: " + routeAtt.calculateTotalDistance(shortestRoute));
        
        shortestRoutesCache.put(cacheKey, shortestRoute);
        return shortestRoute;
    }

    public List<Node> findShortestRoute(String startId, String endId) {
        Node startNode = graph.getNode(startId);
        Node endNode = graph.getNode(endId);
        return findShortestRoute(startNode, endNode);
    }

} 