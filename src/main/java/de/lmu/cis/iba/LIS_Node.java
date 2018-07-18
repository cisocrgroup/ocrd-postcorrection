package de.lmu.cis.iba;

import java.util.HashMap;
import java.util.Map;

public class LIS_Node {

	public String label;
	public HashMap<Integer, LIS_Node> children = new HashMap<Integer, LIS_Node>();
	public int level = 0;
	public int lcs_idx;

	public LIS_Node(String label, int level, int lcs_idx) {
		this.label = label;
		this.level = level;
		this.lcs_idx = lcs_idx;
	}

	public LIS_Node clone(LIS_Node src) {

		LIS_Node result = new LIS_Node(src.label, src.level, src.lcs_idx);

		for (Map.Entry<Integer, LIS_Node> child : src.children.entrySet()) {
			result.children.put(child.getKey(), child.getValue());
		}

		return result;

	}

}
