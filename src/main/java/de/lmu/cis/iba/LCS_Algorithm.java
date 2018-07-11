package de.lmu.cis.iba;

import java.util.ArrayList;
import java.util.HashMap;


public class LCS_Algorithm {
	
	  private Online_CDAWG_sym scdawg = null;
	  public ArrayList<ArrayList<LCS_Triple>> longest_common_subsequences = new ArrayList<ArrayList<LCS_Triple>>();

	  public LCS_Algorithm(Online_CDAWG_sym scdawg) {
		  this.scdawg = scdawg;
	  }

	  public ArrayList[] build_greedy_cover(Node[] nodes_in_s1, HashMap<Node, ArrayList> nodes_endpos_s2,
			    ArrayList<LCS_Triple> lcs_triples) {

			// build greedy cover

			ArrayList[] greedy_cover = new ArrayList[nodes_in_s1.length];

			Node prev = null;

			for (int i = 0; i < nodes_in_s1.length; i++)
			    greedy_cover[i] = new ArrayList();

			boolean first_node_found = false;

			for (int i = 0; i < nodes_in_s1.length; i++) {

			    if (nodes_in_s1[i] != null) {
				// System.out.println(scdawg.get_node_label(nodes_in_s1[i])+ i+
				// nodes_endpos_s2.get(nodes_in_s1[i]) );

				if (nodes_endpos_s2.get(nodes_in_s1[i]) == null)
				    continue; // Wenn keine Endpos im zweiten String dann nix
					      // machen

//				System.out.println("Size: " + nodes_endpos_s2.get(nodes_in_s1[i]).size());

				for (int j = nodes_endpos_s2.get(nodes_in_s1[i]).size() - 1; j >= 0; j--) { // iterieren
													    // über
													    // alle

				    int dec_elem = (int) nodes_endpos_s2.get(nodes_in_s1[i]).get(j);

				    // System.out.println(scdawg.get_node_label(p.node) + " " +
				    // elem_s1 + " :: " + dec_elem);

				    lcs_triples.add(new LCS_Triple(i, dec_elem, nodes_in_s1[i]));
				    int lcs_index = lcs_triples.size() - 1;

				  

		                    if ((j == nodes_endpos_s2.get(nodes_in_s1[i]).size() - 1) & !first_node_found) { // erstes
		                                                                                                            // element
		                                                                                                            // immer
		                                                                                                            // hinzufügen
		                                                                                                            // // HIER
		                                                                                                            // NUN DIE
		                                                                                                            // INDIZES
		                                                                                                            // ANSTATT
		                                                                                                            // DER WERTE
		                                                                                                            // ABER
		                                                                                                            // IMMER MIT
		                                                                                                            // TRIPEL


					greedy_cover[0].add(lcs_index);
					lcs_triples.get(lcs_index).idx_ancestor = -1;
					
					first_node_found = true;
				    }

				    else {
					
					
					
					for (int k = 0; k < greedy_cover.length; k++) { // alle
											// cover
											// listen
											// durchgehen

					    if (greedy_cover[k].size() == 0) {
						greedy_cover[k].add(lcs_index);
						lcs_triples.get(lcs_index).column_number = k;
						lcs_triples.get(lcs_index).idx_ancestor = (int) greedy_cover[k-1].get(greedy_cover[k-1].size() - 1);
						
						break;
					    }

					    else if (dec_elem < lcs_triples
						    .get((int) greedy_cover[k].get(greedy_cover[k].size() - 1)).endpos_s2) { // wenn
															     // kleiner
															     // als
															     // das
															     // letzte
															     // dann
															     // hinzufügen
						greedy_cover[k].add(lcs_index);
						lcs_triples.get(lcs_index).column_number = k;
						lcs_triples.get(lcs_index).idx_ancestor = (int) greedy_cover[k-1].get(greedy_cover[k-1].size() - 1);


						break;
					    }

					}

				    }

				} // for j

			    }

			}

			return greedy_cover;
		    }

		    // *******************************************************************************
		    // calculate_LCS()
		    // *******************************************************************************

		    public ArrayList<LCS_Triple> calculate_LCS(Node[] nodes_in_s1, HashMap<Node, ArrayList> nodes_endpos_s2) {

			ArrayList<LCS_Triple> lcs_triples = new ArrayList<LCS_Triple>();
			
			ArrayList[] greedy_cover = build_greedy_cover(nodes_in_s1, nodes_endpos_s2, lcs_triples);
			 this.print_greedy_cover(greedy_cover, lcs_triples);
			// calculate lis

			int i_count = 0;
			for (int i = 0; i < greedy_cover.length; i++) {
			    if (greedy_cover[i].size() > 0)
				i_count++;
			}

			ArrayList<LCS_Triple> lis = new ArrayList<LCS_Triple>();
			int x = lcs_triples.get((int) greedy_cover[i_count - 1].get(0)).endpos_s2; // pick
												   // any??
			lis.add(lcs_triples.get((int) greedy_cover[i_count - 1].get(0)));

			i_count -= 2;

			while (i_count >= 0) {

			    for (int j = 0; j < greedy_cover[i_count].size(); j++) {
				if (lcs_triples.get((int) greedy_cover[i_count].get(j)).endpos_s2 < x) {
				    lis.add(0, lcs_triples.get((int) greedy_cover[i_count].get(j)));
				    x = lcs_triples.get((int) greedy_cover[i_count].get(j)).endpos_s2;
				    break;
				}
			    }

			    i_count--;
			}
			return lis;

		    }

		  

		    public void printLCS() {

			for (int u = 0; u < this.longest_common_subsequences.size(); u++) {
			    ArrayList<LCS_Triple> lis = this.longest_common_subsequences.get(u);
			    for (int i = 0; i < lis.size(); i++) {
				int e1 = lis.get(i).endpos_s1;
				int e2 = lis.get(i).endpos_s2;
				String nodelabel = scdawg.get_node_label(lis.get(i).node);
				System.out.printf("\"%s\":e1:\"%s\", e2:\"%s\"\n", nodelabel, e1, e2);
			    }
			}

		    }
		    
		   
		    public void print_greedy_cover(ArrayList[] greedy_cover, ArrayList<LCS_Triple> lcs_triples) {

			for (int i = 0; i < greedy_cover.length; i++) {
			    if(greedy_cover[i].size()==0)
				continue;
			    for (int j = 0; j < greedy_cover[i].size(); j++) {
				LCS_Triple t = lcs_triples.get((int) greedy_cover[i].get(j));
				System.out.println(t.endpos_s2 + " (" + scdawg.get_node_label(t.node) + ")");
			    }
			    System.out.println("-------------------------------");
			}

		    }
		    // *******************************************************************************
		    // LCS_to_JSONString()
		    // *******************************************************************************

		    public String LCS_to_JSONString() {

			String result = "{";

			for (int u = 0; u < this.longest_common_subsequences.size(); u++) {

			    ArrayList<LCS_Triple> lis = this.longest_common_subsequences.get(u);

			    String alignment = "[";

			    String d = "";

			    for (int i = 0; i < lis.size(); i++) {
				// System.out.println(lis.get(i).endpos_s1+"
				// "+lis.get(i).endpos_s2+"
				// "+scdawg.get_node_label(lis.get(i).node));
				String nodelabel = scdawg.get_node_label(lis.get(i).node).replace("\"", "\\\"");
				alignment += d + "{\"endpos_s1\":\"" + lis.get(i).endpos_s1 + "\",\"endpos_s2\":\""
					+ lis.get(i).endpos_s2 + "\",\"nodelabel\":\"" + nodelabel + "\"}";
				d = ",";
			    }

			    alignment += "]";

			    result += "\"alignment" + u + "\":" + alignment;
			    if (u < scdawg.stringset.size() - 2)
				result += ",";

			} // for u

			return result + "}";

		    }
	
	
}
