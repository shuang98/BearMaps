import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * This class provides a shortestPath method for finding routes between two points
 * on the map. Start by using Dijkstra's, and if your code isn't fast enough for your
 * satisfaction (or the autograder), upgrade your implementation by switching it to A*.
 * Your code will probably not be fast enough to pass the autograder unless you use A*.
 * The difference between A* and Dijkstra's is only a couple of lines of code, and boils
 * down to the priority you use to order your vertices.
 */
public class Router {
    /**
     * Return a LinkedList of <code>Long</code>s representing the shortest path from st to dest,
     * where the longs are node IDs.
     */
    public static LinkedList<Long> shortestPath(GraphDB g, double stlon,
                                                double stlat, double destLon, double destLat) {
        long s = getId(g, stlon, stlat);
        long d = getId(g, destLon, destLat);
        HashMap<Long, Double> shortestDistances = new HashMap<>();
        HashMap<Long, Long> edgeTo = new HashMap<>();
        for (long key : g.nodes.keySet()) {
            shortestDistances.put(key, Double.MAX_VALUE);
            edgeTo.put(key, 0L);
        }
        shortestDistances.put(s, 0.);
        PriorityQueue<Long> fringe = new PriorityQueue<>(11,
                new VertexComparator(g, shortestDistances, d));
        LinkedList<Long> path = new LinkedList<>();
        fringe.add(s);
        long current = s;
        while (current != d) {
            current = fringe.poll();
            for (long neighbor : g.adjacent(current)) {
                if (shortestDistances.get(neighbor) > shortestDistances.get(current)
                        + g.distance(current, neighbor)) {
                    shortestDistances.put(neighbor, shortestDistances.get(current)
                            + g.distance(current, neighbor));
                    edgeTo.put(neighbor, current);
                    fringe.add(neighbor);
                }
            }
        }

        while (current != s) {
            path.addFirst(current);
            current = edgeTo.get(current);
        }
        path.addFirst(s);
        return path;
    }

    private static long getId(GraphDB g, double lon, double lat) {
        return g.closest(lon, lat);
    }

    private static class VertexComparator implements Comparator<Long> {
        GraphDB g;
        HashMap<Long, Double> shortestDistances;
        long dest;

        VertexComparator(GraphDB g, HashMap<Long, Double> shortestDistances,
                                long destination) {
            this.shortestDistances = shortestDistances;
            this.g = g;
            dest = destination;
        }

        @Override
        public int compare(Long o1, Long o2) {
            double x = shortestDistances.get(o1) - shortestDistances.get(o2)
                    + g.distance(o1, dest) - g.distance(o2, dest);
            if (x < 0) {
                return -1;
            } else if (x > 0) {
                return 1;
            } else {
                return 0;
            }
        }
    }

}
