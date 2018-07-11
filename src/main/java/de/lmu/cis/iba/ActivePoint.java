package de.lmu.cis.iba;


public class ActivePoint {

 public  Node   active_node;
		  int   active_edge;
		  int   active_length;
		 
	//******************************************************************************* 
	// ActivePoint default constructor
	//******************************************************************************* 
	public  ActivePoint()
		  {
		    active_node = new Node();
		    active_edge = 0;   
		    active_length = 0;
		 
		  } // ActivePoint()

	//******************************************************************************* 
	// ActivePoint constructor
	//******************************************************************************* 
	 public ActivePoint(Node _active_node, int _active_edge, int _active_length)
	  {
	   active_node = _active_node;
	   active_edge=_active_edge;   
	   active_length=_active_length;

	  } // ActivePoint()
	
	 public ActivePoint copy(){
		 
		 ActivePoint result = new ActivePoint();
		 result.active_edge = this.active_edge;
		 result.active_length = this.active_length;
		 result.active_node = this.active_node;
		 return result;
		 
	 }
	
}
