package de.lmu.cis.iba;

import de.lmu.cis.ocrd.Document;
import de.lmu.cis.ocrd.Config;
import de.lmu.cis.pocoweb.Client;
import de.lmu.cis.pocoweb.Token;

// import de.lmu.cis.ocrd.Line;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.lmu.cis.api.model.Page;
import de.lmu.cis.api.model.Project;

import de.lmu.cis.api.model.Book;

class Main {
	
	
	  private static String patternsToString(String[] patterns) {
		    String prefix = "[";
		    String res = "";
		    if (patterns == null || patterns.length == 0) {
		      res += "[]";
		    } else {
		      for (String p : patterns) {
		        res += prefix + p;
		        prefix = ",";
		      }
		      res += "]";
		    }
		    return res;
		  }

	
	private static HashMap sort_by_values_desc(HashMap map) { 
	       List list = new LinkedList(map.entrySet());
	       // Defined Custom Comparator here
	       Collections.sort(list, new Comparator() {
	            public int compare(Object o1, Object o2) {
	               return ((Comparable) ((Map.Entry) (o2)).getValue())
	                  .compareTo(((Map.Entry) (o1)).getValue());
	            }
	       });


	       HashMap sortedHashMap = new LinkedHashMap();
	       for (Iterator it = list.iterator(); it.hasNext();) {
	              Map.Entry entry = (Map.Entry) it.next();
	              sortedHashMap.put(entry.getKey(), entry.getValue());
	       } 
	       return sortedHashMap;
	  }
	
	private static void find_nodes_with_n_transitions_to_sinks(int n,Node node,Online_CDAWG_sym scdawg,HashMap result) {
		  
		  int sinkcount = 0; 
		  
		    ArrayList nodes_to_all_sinks = new ArrayList();
		    
		    ArrayList <Node> sinks_hit = new ArrayList();
		    
		    Iterator it = node.children_left.entrySet().iterator();

		      while (it.hasNext()) {
		    	  
		        Map.Entry pair = (Map.Entry)it.next();
		        Node child = (Node)pair.getValue();
		        
		      
		        	for (int j = 0; j < scdawg.sinks.size(); j++) {
		        		if(scdawg.sinks.get(j)==child) {
		        			sinkcount ++ ;
		        			if(!sinks_hit.contains(scdawg.sinks.get(j))) sinks_hit.add(scdawg.sinks.get(j));
		        		}
		        	}
		         
		    	  
		      }
		      
		  		  		      
		      
		      if (sinks_hit.size() == n &! (node==scdawg.root)) {
		    			   			      
		    	  result.put(node,sinks_hit);
		      }
		      
		      // REC AUFRUF der Funktion mit den Kindern
		      Iterator it2 = node.children.entrySet().iterator();

		      while (it2.hasNext()) {
		    	  
		        Map.Entry pair = (Map.Entry)it2.next();
		        Node child = (Node)pair.getValue();
		        find_nodes_with_n_transitions_to_sinks(n,child,scdawg,result);
		      }
		  
	  }
	
	private static void count_nodes(int n,Node node,Online_CDAWG_sym scdawg,HashMap<Node,Integer> result) {
		  
		  // Count all right transitions
		  	  
		    		    
		    Iterator it = node.children.entrySet().iterator();

		      while (it.hasNext()) {
		    	  
		        Map.Entry pair = (Map.Entry)it.next();
		        Node child = (Node)pair.getValue();
		        
		    	  if(scdawg.sinks.contains(child)) continue;

		        	if(result.containsKey(child)) result.put(child,(int)result.get(child)+1);
		        	else {result.put(child,1);}        
		      }
		    		  		      
		                                                       
		      
		      // Count all left transitions
		  	  		    
			    
			    Iterator it2 = node.children_left.entrySet().iterator();

			      while (it2.hasNext()) {
			    	  			    	  
			        Map.Entry pair = (Map.Entry)it2.next();
			        Node child = (Node)pair.getValue();
			        
			    	  if(scdawg.sinks.contains(child)) continue;

			        	if(result.containsKey(child)) result.put(child,(int)result.get(child)+1);
			        	else {result.put(child,1);}        
			      }
			    		  	
			      
		      
		      // REC AUFRUF der Funktion mit den Kindern
		      Iterator it3 = node.children.entrySet().iterator();

		      while (it3.hasNext()) {
		    	  
		        Map.Entry pair = (Map.Entry)it3.next();
		        Node child = (Node)pair.getValue();
		        count_nodes(n,child,scdawg,result);
		      }
		  
	  }

	

  public static void main(String[] args) {
    try (Client client = Client.login(Config.getInstance().getPocowebURL(),
                                      Config.getInstance().getPocowebUser(),
                                      Config.getInstance().getPocowebPass());) {
      Book book = new Book()
                      .withOcrEngine("abbyy")
                      .withOcrUser("test-ocr-user")
                      .withAuthor("Grenzboten")
                      .withTitle("Die Grenzboten")
                      .withYear(1841);
      Project project;
      try (InputStream is = new FileInputStream(
               "src/test/resources/1841-DieGrenzboten-abbyy-small.zip");) {
        project = client.newProject(book, is);
      }
      book = new Book()
                 .withOcrEngine("tesseract")
                 .withOcrUser("test-ocr-user")
                 .withAuthor("Grenzboten")
                 .withTitle("Die Grenzboten")
                 .withYear(1841);
      try (
          InputStream is = new FileInputStream(
              "src/test/resources/1841-DieGrenzboten-tesseract-small-with-error.zip");) {
        project = client.addBook(project, book, is);
      }
      book = new Book()
                 .withOcrEngine("ocropus")
                 .withOcrUser("test-ocr-user")
                 .withAuthor("Grenzboten")
                 .withTitle("Die Grenzboten")
                 .withYear(1841);
      try (InputStream is = new FileInputStream(
               "src/test/resources/1841-DieGrenzboten-ocropus-small.zip");) {
        project = client.addBook(project, book, is);
      }
      Document doc = new Document(project, client);

      ArrayList<String> stringset = new ArrayList<String>();

      doc.eachLine(new Document.Visitor() {
        public void visit(Document.LineTriple t) throws Exception {
          System.out.println(String.format("[%9s,%1d,%2d] %s", t.ocrEngine,
                                           t.pageSeq, t.line.getLineId(),
                                           t.line.getNormalized()));
          for (Token token : t.line.getTokens()) {
            System.out.println(String.format(
                "[token %2d] %s", token.getTokenId(), token.getCor()));
          }
          stringset.add("#" + t.line.getNormalized() + "$");
        }
      });

      for (int i = 0; i < stringset.size(); i++) {
        System.out.println(stringset.get(i));
      }

      Online_CDAWG_sym scdawg = new Online_CDAWG_sym(stringset, false);
      scdawg.determineAlphabet(true);
      scdawg.build_cdawg();
      // scdawg.print_automaton("svgs/scdawg");
      
      HashMap nodes_for_line_alignment = new HashMap();

      find_nodes_with_n_transitions_to_sinks(3,scdawg.root,scdawg,nodes_for_line_alignment);
    
    Iterator it2 = nodes_for_line_alignment.entrySet().iterator();

    while (it2.hasNext()) {
      Map.Entry pair = (Map.Entry)it2.next();
      
      Node n = (Node) pair.getKey();
      
//      System.out.println(scdawg.get_node_label((Node)pair.getKey())+ " "+n.id);
      ArrayList nodes = (ArrayList) pair.getValue();
      for (int i = 0 ; i<nodes.size();i++) {
    	 // System.out.println(scdawg.get_node_label((Node) nodes.get(i)));
      }
     }

     
    HashMap nodes_count = new HashMap<Node,Integer>();

    count_nodes(3,scdawg.root,scdawg,nodes_count);

    HashMap count_nodes_sorted = sort_by_values_desc(nodes_count);
    

    
    Iterator it3 = count_nodes_sorted.entrySet().iterator();

      while (it3.hasNext()) {
        Map.Entry pair = (Map.Entry)it3.next();
        
        Node n = (Node) pair.getKey();
        if((int)pair.getValue()>15) {
        System.out.println(scdawg.get_node_label((Node)pair.getKey())+ " "+pair.getValue());
        }
       }
	    
    
     //
  
     
     
//	 check_sinks(scdawg.root,scdawg);

      ArrayList test_result = new ArrayList();
      
      

      HashMap longest_quasi_nodes = new HashMap();

//      for (int j = 0; j < scdawg.sinks.size(); j++) {
//        for (int i = 0; i < scdawg.all_nodes.size(); i++) {
//          Node node = scdawg.all_nodes.get(i);
//
//          Iterator it = node.children.entrySet().iterator();
//
//          while (it.hasNext()) {
//            Map.Entry pair = (Map.Entry)it.next();
//            Node child = (Node)pair.getValue();
//
//            if (scdawg.sinks.get(j) == child) {  // wenn rechtsübergang auf sink
//              test_result.add(scdawg.sinks.get(j).stringnr + " " +
//                              scdawg.get_node_label(node));
//
//              if (!longest_quasi_nodes.containsKey(
//                      scdawg.sinks.get(j).stringnr)) {
//                longest_quasi_nodes.put(scdawg.sinks.get(j).stringnr,
//                                        scdawg.get_node_label(node));
//
//              } else {
//                if (scdawg.get_node_length(node) >
//                    longest_quasi_nodes.get(scdawg.sinks.get(j).stringnr)
//                        .toString()
//                        .length()) {
//                  longest_quasi_nodes.put(scdawg.sinks.get(j).stringnr,
//                                          scdawg.get_node_label(node));
//                }
//              }
//
//              Iterator it2 = node.children_left.entrySet().iterator();
//
//              while (it2.hasNext()) {
//                Map.Entry pair_left = (Map.Entry)it2.next();
//                Node child_left = (Node)pair_left.getValue();
//
//                if (scdawg.sinks.get(j) ==
//                    child_left) {  // wenn linksübergang auf sink
//                }
//              }
//            }
//          }
//        }
//      }
//
//      ArrayList<ArrayList> all_aligned_lines = new ArrayList<ArrayList>();
//
//      Iterator it = longest_quasi_nodes.entrySet().iterator();
//
//      while (it.hasNext()) {
//        Map.Entry pair = (Map.Entry)it.next();
//
//        int key1 = (int)pair.getKey();
//        String value1 = (String)pair.getValue();
//        System.out.println(key1 + " " + value1);
//
//        if (value1 == null) continue;
//
//        ArrayList<Integer> aligned_lines = new ArrayList();
//        aligned_lines.add(key1);
//
//        Iterator it2 = longest_quasi_nodes.entrySet().iterator();
//
//        //			  while (it2.hasNext()) {
//        //			      Map.Entry pair2 = (Map.Entry)it2.next();
//        //
//        //			      int key2 = (int) pair2.getKey();
//        //				  String value2 =  (String)
//        //pair2.getValue();
//        //
//        //			      if (key1!=key2&&value1.equals(value2)) {
//        //
//        //			    	  aligned_lines.add(key2);
//        //
//        //			    	  longest_quasi_nodes.put(key2, null);
//        //
//        //			      }
//        //			  }
//        //
//        //			  all_aligned_lines.add(aligned_lines);
//      }
//
//      for (ArrayList aligned_lines : all_aligned_lines) {
//        for (int i = 0; i < aligned_lines.size(); i++) {
//          System.out.println(aligned_lines.get(i));
//        }
//        System.out.println("-------------------");
//      }
    

      client.deleteProject(project);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("error: " + e);
    }
  }
}
