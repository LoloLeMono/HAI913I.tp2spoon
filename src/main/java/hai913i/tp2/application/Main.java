package hai913i.tp2.application;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.parse.Parser;

public class Main
{
	public static void main(String[] args) throws IOException
	{
		
		Properties properties = new Properties();
		String projectPath = null;
		MutableGraph graph = null;
		
		try (InputStream fis = new FileInputStream("src/main/resources/graph.dot"))
		{
			graph = new Parser().read(fis);

        } catch (IOException io)
		{
            io.printStackTrace();
        }
		
		for (MutableNode n : graph.nodes())
		{
			// System.out.println(n.toString());
			System.out.println(n.asLinkSource().toString());
			
			for(Link l : graph.edges()) {
				if(n.asLinkSource().equals(l.from()))
					System.out.println(n.attrs().toString());
					System.out.println(l.to().toString());
			}
		}
		

	}
	
	/*
	public int couplage(Class A, Class B)
	{
		
	}
	*/
}
