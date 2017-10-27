import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Alan Yao, Josh Hug
 */
public class GraphDB {
    /**
     * Your instance variables for storing the graph. You should consider
     * creating helper classes, e.g. Node, Edge, etc.
     */
    HashMap<Long, Node> nodes;
    HashMap<Long, Boolean> connectedNodes;
    class Node {
        long id;
        double lon;
        double lat;
        HashSet<Long> connected;

        Node(long id, double lon, double lat) {
            this.id = id;
            this.lon = lon;
            this.lat = lat;
            connected = new HashSet<>();
        }

        void addAdjacent(Node n) {
            connected.add(n.id);
        }
    }


    /**
     * Example constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcome to do so.
     *
     * @param dbPath Path to the XML file to be parsed.
     */
    public GraphDB(String dbPath) {
        nodes = new HashMap<>();
        connectedNodes = new HashMap<>();
        try {
            File inputFile = new File(dbPath);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputFile, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     *
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     * Remove nodes with no connections from the graph.
     * While this does not guarantee that any two nodes in the remaining graph are connected,
     * we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        for (long v : connectedNodes.keySet()) {
            if (!connectedNodes.get(v)) {
                nodes.remove(v);
            }
        }
    }
    //560215250

    /**
     * Returns an iterable of all vertex IDs in the graph.
     */
    Iterable<Long> vertices() {
        //YOUR CODE HERE, this currently returns only an empty list.
        return nodes.keySet();
    }

    /**
     * Returns ids of all vertices adjacent to v.
     */
    Iterable<Long> adjacent(long v) {
        return nodes.get(v).connected;
    }

    /**
     * Returns the Euclidean distance between vertices v and w, where Euclidean distance
     * is defined as sqrt( (lonV - lonV)^2 + (latV - latV)^2 ).
     */
    double distance(long v, long w) {
        double lonV = nodes.get(v).lon;
        double lonW = nodes.get(w).lon;
        double latV = nodes.get(v).lat;
        double latW = nodes.get(w).lat;
        return Math.sqrt((lonV - lonW) * (lonV - lonW) + (latV - latW) * (latV - latW));
    }

    double distance(long v, double lon, double lat) {
        double lonV = nodes.get(v).lon;
        double latV = nodes.get(v).lat;
        return Math.sqrt((lonV - lon) * (lonV - lon) + (latV - lat) * (latV - lat));
    }

    /**
     * Returns the vertex id closest to the given longitude and latitude.
     */
    long closest(double lon, double lat) {
        long closest = 0;
        double d = Double.POSITIVE_INFINITY;
        for (long n : nodes.keySet()) {
            double currentDistance = distance(n, lon, lat);
            if (currentDistance < d) {
                closest = n;
                d = currentDistance;
            }
        }
        return closest;
    }

    /**
     * Longitude of vertex v.
     */
    double lon(long v) {
        return nodes.get(v).lon;
    }

    /**
     * Latitude of vertex v.
     */
    double lat(long v) {
        return nodes.get(v).lat;
    }

    void addNode(long id, double lon, double lat) {

        nodes.put(id, new Node(id, lon, lat));
        connectedNodes.put(id, false);
    }

    void addWay(LinkedList<Long> way) {

        if (way.size() == 1) {
            return;
        }
        while (way.size() != 1) {
            nodes.get(way.getFirst()).addAdjacent(nodes.get(way.get(1)));
            nodes.get(way.get(1)).addAdjacent(nodes.get(way.getFirst()));
            long v = way.removeFirst();
            connectedNodes.put(v, true);
        }
        connectedNodes.put(way.getFirst(), true);

    }
}
