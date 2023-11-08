package hai913i.tp2.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.LinkSource;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.parse.Parser;
import hai913i.tp2.utils.Pair;

public class Couple {

    static String graphFilePath = "src/main/resources/graph.dot";
   
    protected static List<Pair<String, String>> getClassPairsFromGraphFile(String filePath) {
        List<Pair<String, String>> resultPairs = new ArrayList<>();
        MutableGraph graph;

        try (InputStream fis = new FileInputStream(filePath)) {
            graph = new Parser().read(fis);
            for (Link edge : graph.edges()) {
                String fromLabel = getNodeLabel(edge.from(), graph);
                String toLabel = getNodeLabel(edge.to().asLinkSource(), graph);
                resultPairs.add(new Pair<>(fromLabel, toLabel));
            }
        } catch (IOException io) {
            io.printStackTrace();
        }

        return resultPairs;
    }

    private static String getNodeLabel(LinkSource node, MutableGraph graph) {
        for (MutableNode graphNode : graph.nodes()) {
            if (node.name().equals(graphNode.name())) {
                return extractLabelFromAttrs(graphNode.attrs().toString());
            }
        }
        return "";
    }

    private static String extractLabelFromAttrs(String attrs) {
        String[] parts = attrs.split("=");
        if (parts.length > 1) {
            return parts[1].substring(0, parts[1].length() - 1);
        }
        return "";
    }
    
    public static double getCouplageBetweenTwoClasses(String classA, String classB, List<Pair<String, String>> classPairs) {		
        int numberOfCalls = 0;

        for (Pair<String, String> pair : classPairs) {
            boolean isACallsB = pair.getLeft().contains(classA) && pair.getRight().contains(classB);
            boolean isBCallsA = pair.getLeft().contains(classB) && pair.getRight().contains(classA);

            if (isACallsB || isBCallsA) {
                numberOfCalls++;
            }
        }
        
        return (double) numberOfCalls / classPairs.size();
    }
    
    public static void createCouplingGraph(List<Pair<String, String>> pairList) throws IOException {
        Set<String> uniqueClasses = extractUniqueClasses(pairList);
        Graph<String, DefaultWeightedEdge> graph = createWeightedGraph(uniqueClasses, pairList);

        exportGraph(graph);
    }

    private static Set<String> extractUniqueClasses(List<Pair<String, String>> pairList) {
        Set<String> uniqueClasses = new HashSet<>();
        for (Pair<String, String> pair : pairList) {
            uniqueClasses.add(extractClassName(pair.getLeft()));
            uniqueClasses.add(extractClassName(pair.getRight()));
        }
        return uniqueClasses;
    }

    private static String extractClassName(String fullName) {
        if (fullName.contains(".")) {
            return fullName.split("\\.")[0];
        }
        return fullName;
    }

    private static Graph<String, DefaultWeightedEdge> createWeightedGraph(Set<String> uniqueClasses, List<Pair<String, String>> pairList) {
        Graph<String, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        for (String className : uniqueClasses) {
            graph.addVertex(className);
        }

        for (String classA : uniqueClasses) {
            for (String classB : uniqueClasses) {
                if (!classA.equals(classB)) {
                    double coupling = getCouplageBetweenTwoClasses(classA, classB, pairList);
                    if (coupling > 0) {
                        DefaultWeightedEdge edge = graph.addEdge(classA, classB);
                        if (edge != null) {
                            graph.setEdgeWeight(edge, coupling);
                        }
                    }

                }
            }
        }
        return graph;
    }

    private static void exportGraph(Graph<String, DefaultWeightedEdge> graph) throws IOException {
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setMaximumFractionDigits(5);

        DOTExporter<String, DefaultWeightedEdge> exporter = new DOTExporter<>();
        exporter.setVertexAttributeProvider((v) -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            map.put("label", DefaultAttribute.createAttribute(v));
            return map;
        });
        exporter.setEdgeAttributeProvider((e) -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            double edgeWeight = graph.getEdgeWeight(e);
            map.put("weight", DefaultAttribute.createAttribute(edgeWeight));
            map.put("label", DefaultAttribute.createAttribute(decimalFormat.format(edgeWeight)));
            return map;
        });

        Writer writer = new StringWriter();
        exporter.exportGraph(graph, writer);
        exporter.exportGraph(graph, new File("src/main/resources/couplingGraph.dot"));

        MutableGraph mutableGraph = new Parser().read(writer.toString());
        Graphviz.fromGraph(mutableGraph).height(1000).render(Format.PNG).toFile(new File("src/main/resources/couplingGraph.png"));
    }
}
