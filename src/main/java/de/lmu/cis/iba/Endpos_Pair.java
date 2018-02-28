package de.lmu.cis.iba;

import java.util.ArrayList;
import java.util.HashMap;

public class Endpos_Pair {
  public Node[] nodes_in_s1;
  public HashMap<Node, ArrayList> nodes_endpos_s2 = new HashMap();

  public Endpos_Pair(Node[] nodes_in_s1,
                     HashMap<Node, ArrayList> nodes_endpos_s2) {
    this.nodes_in_s1 = nodes_in_s1;
    this.nodes_endpos_s2 = nodes_endpos_s2;
  }
}
