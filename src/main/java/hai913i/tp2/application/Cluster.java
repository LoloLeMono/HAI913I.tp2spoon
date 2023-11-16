package hai913i.tp2.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.LinkSource;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.parse.Parser;
import hai913i.tp2.utils.Pair;

public class Cluster {
	
    static String couplingGraphFile = "src/main/resources/couplingGraph.dot";
	
	public static void clustering(){
		ArrayList<Pair<Double, Pair<String, String>>> edgeList =  new ArrayList<Pair<Double, Pair<String, String>>> ();
		ArrayList<String> listCluster = extractGraphWithDotFile(couplingGraphFile, edgeList);
		ArrayList<Pair<String, String>> dendro = clusteringChoice(listCluster, edgeList);
					
		for (Pair<String, String> pair : dendro) {
			
			System.out.println(pair.getLeft() + " avec " + pair.getRight());
//			System.out.println(pair.getRight());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			
		System.out.println("\n");
		System.out.println("==========================");
		System.out.println("\n");

		
//		System.out.println("Liste des clusters restants : ");
//			
//		for (String cluster : listCluster) {
//			
//			System.out.println(cluster);
//		}
//			
//		System.out.println("\n");
//		System.out.println("==========================");
//		System.out.println("\n");
		


	}
		
    protected static ArrayList<String> extractGraphWithDotFile(String filePath, ArrayList<Pair<Double, Pair<String, String>>> edgeList) {
        ArrayList<String> listCluster = new ArrayList<>();

        File dotFile = new File(filePath);
        try (FileInputStream inputDotFile = new FileInputStream(dotFile)) {
            MutableGraph graph = new Parser().read(inputDotFile);
            for (MutableNode node : graph.nodes()) {
                String label = extractLabelFromNode(node);
                if (!(Character.isLowerCase(label.charAt(0)) || label.contains("<") || label.contains(">")))
                	listCluster.add(label);
                }
            
            for(Link link : graph.edges()) {
            	String firstNode = extractLabelFromLinkSource(link.asLinkSource());
            	String secondNode = extractLabelFromLinkSource(link.asLinkTarget().asLinkSource());
            	double weight = Double.valueOf(link.attrs().get("weight").toString());
//            	System.out.println(firstNode + " -- " + secondNode + " -- "+ weight);
            	edgeList.add(new Pair<>(weight, new Pair<>(firstNode, secondNode)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return listCluster;
    }

    private static String extractLabelFromNode(MutableNode node) {
        String[] labelParts = node.attrs().toString().split("=");
        return labelParts[1].substring(0, labelParts[1].length() - 1);
    }
    
    private static String extractLabelFromLinkSource(LinkSource link) {
    	Pattern pattern = Pattern.compile("\\{label=(.*?)\\}");
        Matcher matcher = pattern.matcher(link.toString());

        if (matcher.find()) {
            return matcher.group(1);  // Affichera "end"
        }
        return "";
    }
    
    protected static ArrayList<Pair<String, String>> clusteringChoice(ArrayList<String> listCluster, ArrayList<Pair<Double, Pair<String, String>>> edgeList) {
    	
		System.out.println("Démarrage du clustering");
		System.out.println("\n");
                
        ArrayList<Pair<String, String>> dendro = new ArrayList<Pair<String, String>>() ;
        
        while(listCluster.size() > 1){
        	
        	double maxCoupling = -1.0;
            String bestCluster1 = null, bestCluster2 = null;
            
            
            for (int i = 0; i < listCluster.size(); i++) {
                for (int j = i + 1; j < listCluster.size(); j++) {
                    String[] cluster1Components = getClusterComponents(listCluster.get(i));
                    String[] cluster2Components = getClusterComponents(listCluster.get(j));

                    double currentCoupling = calculateCoupling(cluster1Components, cluster2Components, edgeList);
                    
                    if (currentCoupling > maxCoupling) {
                        maxCoupling = currentCoupling;
                        bestCluster1 = listCluster.get(i);
                        bestCluster2 = listCluster.get(j);
                    }
                }
            }

            listCluster.remove(bestCluster1);
            listCluster.remove(bestCluster2);
            listCluster.add(bestCluster1 + "." + bestCluster2);
//            System.out.println("Nouveau cluster créer : " + bestCluster1 + "." + bestCluster2);
            dendro.add(new Pair<String, String>(bestCluster1, bestCluster2));
        }

		System.out.println("Clustering terminé");
		System.out.println("==========================");

        return dendro;
    }

    private static String[] getClusterComponents(String cluster) {
        return cluster.split("\\.");
    }

 // Méthode pour calculer le coupling entre deux clusters
    private static double calculateCoupling(String[] cluster1Components, String[] cluster2Components, ArrayList<Pair<Double, Pair<String, String>>> edgeList) {
        double coupling = 0.0;

        for (Pair<Double, Pair<String, String>> weightedEdge : edgeList) {
            String node1 = weightedEdge.getRight().getLeft();
            String node2 = weightedEdge.getRight().getRight();
            double weight = weightedEdge.getLeft();

            boolean node1InCluster1 = Arrays.asList(cluster1Components).contains(node1);
            boolean node2InCluster2 = Arrays.asList(cluster2Components).contains(node2);

            boolean node1InCluster2 = Arrays.asList(cluster2Components).contains(node1);
            boolean node2InCluster1 = Arrays.asList(cluster1Components).contains(node2);

            if ((node1InCluster1 && node2InCluster2) || (node1InCluster2 && node2InCluster1)) {
                coupling += weight;
            }
        }

        return coupling;
    }
}
