import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.util.CoreMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Statement;
import org.jgrapht.Graph;
import org.jgrapht.graph.*;
import java.io.*;
import java.nio.file.Files;
import java.util.stream.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class textSummarization {
	
	public static boolean isSubset(String a, String b)
	{
		String words[] = a.split(" ");
		for(int i=0;i<words.length;i++)
		{
			if(!b.contains(words[i]))
			{
				return false;
			}
		}
		return true;
	}
	
	public static Map<String, Integer> sortByValue(final Map<String, Integer> wordCounts) {
        return wordCounts.entrySet()
                .stream()
                .sorted((Map.Entry.<String, Integer>comparingByValue().reversed()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }
	
	public static void extract(StanfordCoreNLP pipeline, String path) throws IOException
	{
		System.out.println("Parsing file:\n"+path);
		TDBConnection tdb = null;		
		String namedModel = "KnowlegeGraphs";		
		tdb = new TDBConnection("graph");
		File file = new File(path);
	    @SuppressWarnings("resource")
		BufferedReader br = new BufferedReader(new FileReader(file));
	    String text = "";
	    String st;
	    while ((st = br.readLine()) != null) 
	        text += st+"\n";
		
	    Annotation doc = new Annotation(text);
	    pipeline.annotate(doc);
	    
	    ArrayList<String> sub = new ArrayList<>();
	    ArrayList<String> obj = new ArrayList<>();
	    ArrayList<String> relation = new ArrayList<String>();
	    ArrayList<Boolean> isRemoved = new ArrayList<Boolean>();
	    
	    Map<String,nodes> adjList = new HashMap<String,nodes>();
	    
	    Graph<String, DefaultWeightedEdge> multiGraph = new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class); 
  	  	
	    for (CoreMap sentence : doc.get(CoreAnnotations.SentencesAnnotation.class)) {
	      Collection<RelationTriple> triples = sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);
	      for (RelationTriple triple : triples) {
	    	  
	    	  String src = triple.subjectGloss();
	    	  String des = triple.objectGloss();
	    	  String rel = triple.relationGloss();
	    	  
	    	  sub.add(src);
	    	  
	    	  obj.add(des);
	    	  
	    	  relation.add(rel);
	    	  
	    	  isRemoved.add(false);
	      	}
	      
	      }
	      
	      for(int i=0;i<sub.size();i++)
	      {			  
	    	  for(int j=0;j<sub.size();j++)
	    	  {
	    		  if(i==j || isRemoved.get(i) || isRemoved.get(j))
	    			  continue;
	    		  String textJ = sub.get(j) + " " + relation.get(j) + " " + obj.get(j);
	    		  if(textJ.contains("at_time"))
				  {
	    			  textJ = textJ.replace("at_time","");
				  }
	    		  String textI = sub.get(i) + " " + relation.get(i) + " " + obj.get(i);
	    		  if(textI.contains("at_time"))
				  {
	    			  textI = textI.replace("at_time","");
				  }
	    		  if(isSubset(sub.get(j),sub.get(i)) && isSubset(obj.get(j),obj.get(i)) && isSubset(relation.get(j),relation.get(i)))//isSubset(textJ,textI))
	    		  {
	    			  isRemoved.set(j,true);	    			  
	    		  }
	    	  }
	      }
	      
	      int index=0;
	      while(true)
	      {
	    	  if(index>=sub.size())
	    		  break;
	    	  if(isRemoved.get(index))
	    	  {
		    	  sub.remove(index);
				  obj.remove(index);
				  relation.remove(index);
				  isRemoved.remove(index);
	    	  }
	    	  else
	    	  {
	    		  index++;
	    	  }
	      }

	      for(int i=0;i<sub.size();i++)
	      {
	    	  String src = sub.get(i);
	    	  String des = obj.get(i);
	    	  String rel = relation.get(i);
	    	  
	    	  if(!adjList.containsKey(src))
	    	  {
	    		  nodes temp = new nodes();
	    		  adjList.put(src,temp);
	    		  multiGraph.addVertex(src);
	    	  }
	    	  
	    	  if(!adjList.containsKey(des))
	    	  {
	    		  nodes temp = new nodes();
	    		  adjList.put(des,temp);
	    		  multiGraph.addVertex(des);
	    	  }
	    	  
	    	  nodes node = adjList.get(src);
	    	  node.insert(des,rel);
	    	  adjList.put(src,node);
	    	  
	    	  try
	    	  {
	    		  DefaultWeightedEdge edge = new DefaultWeightedEdge();
	    		  multiGraph.addEdge(src,des,edge);
	    		  multiGraph.setEdgeWeight(edge,1);
	    	  
	    		  tdb.addStatement( namedModel, src, rel, des );
	    	  }
	    	  catch(Exception e)
	    	  {
	    		  System.out.print(e);
	    		  System.out.println(" ERROR: " + src + " " + des + " " + rel);
	    	  }
	    }
	    
	    PageRank pg_graph = new PageRank(multiGraph);
	    @SuppressWarnings("unchecked")
		Map<String,Integer> scores = sortByValue(pg_graph.getScores());
	    
	    Set< Map.Entry<String,Integer> > set = (Set<Entry<String, Integer>>) scores.entrySet();
	    
	    int c=0;
	    
	    ArrayList<String> top_n_nodes = new ArrayList<>();
	    
	    String summary = "";
	    String prevSubj = "";
		String prevPred = "";
		String prevObje = "";
	    
	    for (Map.Entry<String,Integer> me:set) 
	    {
	    	if(c==2)
	    		break;
	    	c++;
	    	List<Statement> results = tdb.getStatements(namedModel,me.getKey(),null,null);
			
			for(int k=0;k<results.size();k++)
			{
				String subj = results.get(k).getSubject().toString();
				String pred = results.get(k).getPredicate().toString();
				String obje = results.get(k).getObject().toString();
				if(obje.contains(subj))
				{
					obje = obje.replace(subj,"");
				}
				if(pred.contains("at_time"))
				{
					pred = pred.replace("at_time","");
				}
				if(isSubset(pred,summary) && isSubset(obje,summary))
					continue;
				if(pred == prevPred)
					summary += ", " + obje;
				else
					summary += "."+" "+subj+" "+pred+" "+obje;
				prevSubj = subj;
				prevPred = pred;
				prevObje = obje;
			}
	    }
	    summary = summary.replace("  "," ");
	    summary += ".";
//	    System.out.println(summary.substring(2,summary.length()));
	    
	    summary = summary.substring(2,summary.length()) + "\n";
	    BufferedWriter bw = null;
	    try 
	    {
	    	int lastIndex = path.lastIndexOf(File.separator);
	    	int secondLastIndex = path.substring(0,lastIndex).lastIndexOf(File.separator);
	    	int thirdLastIndex = path.substring(0,secondLastIndex).lastIndexOf(File.separator);
	    	String new_path = path.substring(0,thirdLastIndex+1) + "output" + path.substring(lastIndex,path.length());
	    	System.out.println("Summary output to : " + new_path);
	    	File out_file = new File(new_path);
	    	out_file.getParentFile().mkdirs();
	    	if (!out_file.exists()) 
	    	{
	    		out_file.createNewFile();
	   	  	}
	    	
	    	FileWriter fw = new FileWriter(out_file);
	    	bw = new BufferedWriter(fw);
	  	  	bw.write(summary);
	    }
	    catch (IOException e) 
	    {
            e.printStackTrace();
        }
	    finally
	    {
            try 
            {
            	bw.close();
            } 
            catch (IOException e) 
            {
                e.printStackTrace();
            }
        }
	    
	    for(int i=0;i<sub.size();i++)
        {
    	    String src = sub.get(i);
    	    String des = obj.get(i);
    	    String rel = relation.get(i);
    	    
    	    tdb.removeStatement(namedModel,src,rel,des);
        }
	}
	
	public static void main(String[] args) throws Exception {
		
		Properties props = new Properties();
	    props.setProperty("annotators", "tokenize,ssplit,pos,lemma,depparse,parse,natlog,ner,coref,openie");
	    props.setProperty("openie.resolve_coref","true");
	    props.setProperty("openie.triple.strict","true");
//	    props.setProperty("openie.triple.all_nominals","true");
//	    props.setProperty("openie.format","ollie");
	    System.out.println("Loading models...");
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	    System.out.println("Done.");
	    
	    String dir = System.getenv("PROJECT_HOME") + File.separator + "data" + File.separator + "parsed" + File.separator + "text";
	    String out_dir = System.getenv("PROJECT_HOME") + File.separator + "data" + File.separator + "output";
	    FileUtils.cleanDirectory(new File(out_dir)); 
	    try(Stream<Path> paths = Files.walk( Paths.get(dir) ))
        {
            System.out.println("Parsing started...");
            paths
            .filter(Files::isRegularFile)
            .forEach( path -> {
				try {
					extract(pipeline,path.toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
        }
        catch(Exception e)
        {
            throw e;
        }
	    
	    System.out.println("Finished Parsing.");
	}
}


