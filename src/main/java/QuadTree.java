import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

/**
 * NORTH IS LAT POS
 * EAST IS Lon POS
 */
public class QuadTree {
    Node root;
    double qullon;
    double qlrlon;
    double qullat;
    double qlrlat;
    double qlonPP;
    ArrayList<Node> list;

    public QuadTree(Map<String, Double> params) {
        qullon = params.get("ullon");
        qlrlon = params.get("lrlon");
        qullat = params.get("ullat");
        qlrlat = params.get("lrlat");
        double w = params.get("w");

        qlonPP = (qlrlon - qullon) / w;
        list = new ArrayList<>();
        root = new Node();
    }

    public String[][] getGrid() {
        LinkedList<String> names = new LinkedList<String>();
        for (Node n : list) {
            names.add(n.imageName);
        }
        String[] nameArray = new String[names.size()];
        int index = 0;
        int rows = 0;
        while (index < nameArray.length) {
            String s = names.getFirst();
            while (names.contains(s)) {
                names.remove(s);
                nameArray[index] = s;
                s = getRight(s);
                index++;
            }
            rows++;
        }
        int cols = index / rows;
        String[][] grid = new String[rows][index / rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                grid[i][j] = "img/" + nameArray[i * cols + j] + ".png";
            }
        }
        return grid;
    }

    private String getRight(String imageName) {
        StringBuilder right = new StringBuilder("");

        int index = imageName.length() - 1;
        while (imageName.charAt(index) % 2 == 0) {
            right.insert(0, imageName.charAt(index) - 48 - 1);
            index--;
            if (index < 0) {
                return null;
            }
        }
        right.insert(0, imageName.charAt(index) - 48 + 1);
        right.insert(0, imageName.substring(0, index));
        return right.toString();
    }


    public double[] getNodeinfo(String name) {
        Node pointer = root;
        int index = 0;
        while (!name.equals(pointer.imageName)) {
            pointer = pointer.children[name.charAt(index) - 49];
            index++;
        }
        double[] info = new double[5];
        info[0] = pointer.ulLon;
        info[1] = pointer.ulLat;
        info[2] = pointer.lrLon;
        info[3] = pointer.lrLat;
        info[4] = (double) pointer.depth;
        return info;
    }


    private class Node {
        String imageName;
        double ulLon;
        double ulLat;
        double lrLon;
        double lrLat;
        double lonPP;
        Node[] children;
        int depth;

        /**
         * root node constructor
         */
        Node() {
            imageName = "root";
            ulLon = MapServer.ROOT_ULLON;
            ulLat = MapServer.ROOT_ULLAT;
            lrLon = MapServer.ROOT_LRLON;
            lrLat = MapServer.ROOT_LRLAT;
            depth = 0;
            children = new Node[4];
            for (int i = 0; i < 4; i++) {
                children[i] = new Node(i + 1, this);
            }
        }

        /**
         * Node constructor for all quadrants that aren't the root;
         *
         * @param lastDigit
         * @param parent
         */
        Node(int lastDigit, Node parent) {
            if (parent.imageName.equals("root")) {
                this.imageName = "" + lastDigit;
            } else {
                this.imageName = parent.imageName + lastDigit;
            }
            depth = parent.depth + 1;
            double dLon = parent.lrLon - parent.ulLon;
            double dLat = parent.ulLat - parent.lrLat;
            this.ulLon = parent.ulLon;
            this.ulLat = parent.ulLat;
            this.lrLon = parent.lrLon;
            this.lrLat = parent.lrLat;
            switch (lastDigit) {
                case 1:
                    this.lrLon -= dLon / 2;
                    this.lrLat += dLat / 2;
                    break;
                case 2:
                    this.ulLon += dLon / 2;
                    this.lrLat += dLat / 2;
                    break;
                case 3:
                    this.ulLat -= dLat / 2;
                    this.lrLon -= dLon / 2;
                    break;
                case 4:
                    this.ulLon += dLon / 2;
                    this.ulLat -= dLat / 2;
                    break;
                default:
                    break;
            }
            this.lonPP = (this.lrLon - this.ulLon) / 256;
            children = new Node[4];
            if (this.intersects(qullon, qullat, qlrlon, qlrlat)
                    && (this.lonPP <= qlonPP || depth == 7)) {
                list.add(this);
            } else if (intersects(qullon, qullat, qlrlon, qlrlat)) {
                for (int i = 0; i < 4; i++) {
                    children[i] = new Node(i + 1, this);
                }
            }
        }

        boolean intersects(double ullon, double ullat, double lrlon, double lrlat) {
            return !(ulLon < ullon && lrLon < ullon || ulLat > ullat && lrLat > ullat
                    || lrLon > lrlon && ulLon > lrlon || lrLat < lrlat && ulLat < lrlat);
        }


    }
}
