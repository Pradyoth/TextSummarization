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
import org.apache.jena.rdf.model.Statement;
import org.jgrapht.Graph;
import org.jgrapht.graph.*;
import java.io.*;

public class textSummarization {
	
	public static boolean isSubset(String a, String b)
	{
		String words[] = a.split(" ");
//		System.out.println(a);
//		System.out.println(b);
		for(int i=0;i<words.length;i++)
		{
			
//			System.out.println(words[i]);
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
	
	public static void main(String[] args) throws Exception {
		
		
//		String a1 = "Francis";
//		String a2 = "announced";
//		String a3 = "Pope Francis new slate";
//		
//		String b1 = "Francis";
//		String b2 = "announced";
//		String b3 = "Pope Francis new slate of cardinals";
//		
//		if(isSubset(a1,b1) && isSubset(a2,b2) && isSubset(a3,b3))
//		{
//			System.out.println("true");
//		}
		
		TDBConnection tdb = null;		
		String namedModel = "KnowlegeGraphs";		
		tdb = new TDBConnection("graph");
		File file = new File("D:\\Study\\Semester1\\DataMining\\Project\\input.txt");
	    @SuppressWarnings("resource")
		BufferedReader br = new BufferedReader(new FileReader(file));
	    String text = "";
	    String st;
	    while ((st = br.readLine()) != null) 
	        text += st+"\n";
		
	    Properties props = new Properties();
	    props.setProperty("annotators", "tokenize,ssplit,pos,lemma,depparse,parse,natlog,ner,coref,openie");
	    props.setProperty("openie.resolve_coref","true");
	    props.setProperty("openie.triple.strict","true");
//	    props.setProperty("openie.triple.all_nominals","true");
//	    props.setProperty("openie.format","ollie");
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	    System.out.println("Done.");
	    Annotation doc = new Annotation(text);
	    pipeline.annotate(doc);
	    
	    ArrayList<String> sub = new ArrayList<>();
	    ArrayList<String> obj = new ArrayList<>();
	    ArrayList<String> relation = new ArrayList<String>();
	    ArrayList<Boolean> isRemoved = new ArrayList<Boolean>();
	    
	    Map<String,nodes> adjList = new HashMap<String,nodes>();
	    
//	    BufferedWriter writer = new BufferedWriter(new FileWriter("D:\\Study\\Semester1\\DataMining\\Project\\rel_output.txt"));
	    
	    Graph<String, DefaultWeightedEdge> multiGraph = new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class); 
  	  	
	    for (CoreMap sentence : doc.get(CoreAnnotations.SentencesAnnotation.class)) {
	      Collection<RelationTriple> triples = sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);
	      for (RelationTriple triple : triples) {
	    	  
	    	  String src = triple.subjectGloss();
	    	  String des = triple.objectGloss();
	    	  String rel = triple.relationGloss();
	    	  
//	    	  System.out.println(triple.subjectGloss() + "|" +
//		  	            triple.relationGloss() + "|" +
//			            triple.objectGloss() + "\n");
	    	  
//	    	  writer.write(triple.subjectGloss() + "|" +
//	  	            triple.relationGloss() + "|" +
//		            triple.objectGloss() + "\n");
	    	  
	    	  sub.add(src);
	    	  
	    	  obj.add(des);
	    	  
	    	  relation.add(rel);
	    	  
	    	  isRemoved.add(false);
	      	}
	      
	      }
	      
//	      writer.close();
	      
	      for(int i=0;i<sub.size();i++)
	      {
//	    	  System.out.println("node:");
//	    	  System.out.println(sub.get(i)+"|"+obj.get(i)+"|"+relation.get(i));
//			  System.out.println("subsets:");
			  
	    	  for(int j=0;j<sub.size();j++)
	    	  {
	    		  if(i==j || isRemoved.get(i) || isRemoved.get(j))
	    			  continue;
//	    		  System.out.println(sub.get(j)+"|"+obj.get(j)+"|"+relation.get(j));
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
//	    			  System.out.println("removed" + ":" +sub.get(j)+"|"+obj.get(j)+"|"+relation.get(j));
	    			  isRemoved.set(j,true);	    			  
	    		  }
	    	  }
	      }
	      
	      int index=0;
//	      System.out.println(sub.size());
	      while(true)
	      {
	    	  if(index>=sub.size())
	    		  break;
	    	  if(isRemoved.get(index))
	    	  {
//	    		  System.out.println("removed" + ":" +sub.get(index)+"|"+obj.get(index)+"|"+relation.get(index));
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
//	      System.out.println(sub.size());
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
	    	  
	    	  DefaultWeightedEdge edge = new DefaultWeightedEdge();
	    	  multiGraph.addEdge(src,des,edge);
//	    	  double weight = relation.indexOf(rel);
	    	  multiGraph.setEdgeWeight(edge,1);
	    	  
	    	  tdb.addStatement( namedModel, src, rel, des );	
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
//					System.out.println("here");
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
//				System.out.println(results.get(k));
			}
	    }
	    summary = summary.replace("  "," ");
	    summary += ".";
	    System.out.println(summary.substring(2,summary.length()));
	    for(int i=0;i<sub.size();i++)
        {
    	    String src = sub.get(i);
    	    String des = obj.get(i);
    	    String rel = relation.get(i);
    	    
    	    tdb.removeStatement(namedModel,src,rel,des);
        }
	    
//	    List<Statement> results = tdb.getStatements(namedModel,null,null,null);
//		
//		for(int i=0;i<results.size();i++)
//		{
//			System.out.println(results.get(i));
//		}
	}
}


