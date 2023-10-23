package hai913i.tp2.application;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.LinkSource;
import guru.nidi.graphviz.model.LinkTarget;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.parse.Parser;
import hai913i.tp2.utils.Pair;

public class Main
{
	public static void main(String[] args) throws IOException
	{
		
//		Properties properties = new Properties();
//		String projectPath = null;
		
		String graphFilePath = "src/main/resources/graph.dot";

		MutableGraph graph = null;
		ArrayList<Pair<String, String>> listPairClasses = new ArrayList<Pair<String, String>>();
		
		listPairClasses = createPairClassesFromFile(graphFilePath,graph);
		
		for(Pair<String, String> p : listPairClasses) {
			System.out.println(p.getLeft().toString() + " -> " + p.getRight().toString());
		}


	
	}
	
	protected static ArrayList<Pair<String, String>> createPairClassesFromFile(String filePath, MutableGraph graph) throws IOException {
		
		ArrayList<Pair<LinkSource, LinkTarget>> listPair = new ArrayList<Pair<LinkSource, LinkTarget>>();

		// We open the .dot file to retrieve the graph we created in the tp1
		// Moreover, we parse it into a MutableGraph in order to manipulate it
		try (InputStream fis = new FileInputStream(filePath))
		{
			graph = new Parser().read(fis);

        } catch (IOException io)
		{
            io.printStackTrace();
        }

		for (Link edge : graph.edges()) {
			listPair.add(new Pair<LinkSource, LinkTarget>(edge.from(), edge.to()));
		}

		ArrayList<Pair<String, String>> listPairString = new ArrayList<Pair<String, String>>();

		for (Pair<LinkSource, LinkTarget> pair : listPair) {

			Pair<String, String> newPair = new Pair<String, String>();

			for (MutableNode node : graph.nodes()) {
			
				// To store the label of the node, we have to split the string where "=" is
				// Because node.attrs() give a string like : "{label=myLabel}".
				// Then we cut it (substring()) to remove the "}" to the end.
				String labelNode[] = node.attrs().toString().split("=");
				String label = labelNode[1].substring(0, labelNode[1].length() - 1);
				
				if (pair.getLeft().name().equals(node.name())) {
					newPair.setLeft(label);
				}

				else if (pair.getRight().name().equals(node.name())) {
					newPair.setRight(label);
				}
			}

			listPairString.add(newPair);
		}

		return listPairString;
	}
	
}
