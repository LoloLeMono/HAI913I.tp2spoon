package hai913i.tp2.application;

import java.io.IOException;
import java.util.List;

import hai913i.tp2.utils.Pair;

public class Main
{
    static String graphFilePath = "src/main/resources/graph.dot";
    
    public static void main(String[] args) {

    	
//		  Exercice n°1
    	
//        List<Pair<String, String>> classPairs = Couple.getClassPairsFromGraphFile(graphFilePath);
//        
//        double couplageBetweenTwoClasses = Couple.getCouplageBetweenTwoClasses("Test", "User", classPairs);
//        
//        System.out.println("Couplage entre Test et User : " + couplageBetweenTwoClasses);
//        
//        try {
//			Couple.createCouplingGraph(classPairs);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
    	
//    	Exercice n°2
    	Cluster.clustering();
    }

}
