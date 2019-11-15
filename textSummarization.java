import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.util.CoreMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.graph.*;

import java.io.*;

public class textSummarization {
	
	public static Map<String, Integer> sortByValue(final Map<String, Integer> wordCounts) {
        return wordCounts.entrySet()
                .stream()
                .sorted((Map.Entry.<String, Integer>comparingByValue().reversed()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }
	
	public static void main(String[] args) throws Exception {
		
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
	    //props.setProperty("openie.triple.all_nominals","true");
	    //props.setProperty("openie.format","ollie");
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	    System.out.println("Done.");
	    Annotation doc = new Annotation(text);
	    pipeline.annotate(doc);
	    
	    ArrayList<String> sub = new ArrayList<>();
	    ArrayList<String> obj = new ArrayList<>();
	    ArrayList<String> relation = new ArrayList<String>();
	    
	    BufferedWriter writer = new BufferedWriter(new FileWriter("D:\\Study\\Semester1\\DataMining\\Project\\rel_output.txt"));
	    
	    Graph<String, DefaultWeightedEdge> multiGraph = new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class); 
	    
	    for (CoreMap sentence : doc.get(CoreAnnotations.SentencesAnnotation.class)) {
	      Collection<RelationTriple> triples = sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);
	      for (RelationTriple triple : triples) {
	    	  
	    	  String src = triple.subjectGloss();
	    	  String des = triple.objectGloss();
	    	  String rel = triple.relationGloss();
	    	  
	    	  if(!sub.contains(src))
	    	  {
	    		  sub.add(src);
	    		  multiGraph.addVertex(src);
	    	  }
	    	  if(!obj.contains(des))
	    	  {
	    		  obj.add(des);	    	
	    		  multiGraph.addVertex(des);
	    	  }
	    	  
	    	  relation.add(rel);
	    	  DefaultWeightedEdge edge = new DefaultWeightedEdge();
	    	  multiGraph.addEdge(src,des,edge);
	    	  double weight = relation.indexOf(rel);
	    	  multiGraph.setEdgeWeight(edge,weight);
	    	  
	    	  System.out.println(triple.confidence + "|" +
	            triple.subjectGloss() + "|" +
	            triple.relationGloss() + "|" +
	            triple.objectGloss());
	    	  
	    	  writer.write(triple.subjectGloss() + "|" +
	  	            triple.relationGloss() + "|" +
		            triple.objectGloss() + "\n");
	      }
	    }
	    
	    writer.close();
	    
	    PageRank pg_graph = new PageRank(multiGraph);
	    @SuppressWarnings("unchecked")
		Map<String,Integer> scores = sortByValue(pg_graph.getScores());
	    
	    Set< Map.Entry<String,Integer> > set = (Set<Entry<String, Integer>>) scores.entrySet();
	    
	    for (Map.Entry<String,Integer> me:set) 
	    {
	    	System.out.print(me.getKey()+":"); 
	    	System.out.println(me.getValue()); 
	    }
	}
}


