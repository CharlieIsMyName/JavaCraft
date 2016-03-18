//June 17, 2014
//Starcraft Project
//by: Charlie Wang, Kassem Bazzi
//March 31, 2014

//Node with important A* information
//Methods that allows easy addition of potential neighboring nodes to an open NodeList
//Checks for equality through grid coordinates

import java.util.*;
import java.awt.*;

public class Node implements Comparable<Node>{
	private int gridX, gridY;
	private int Hval, Gval, Fval, originalF; //originalF - Fvalue without extra G values
	//dirG - the conventional G value due to movement cost
	//terG - a G cost associated with terrain 
	//addG - additional G costs due to overlapping paths
	private int dirG, addG, parG, terG;
	private Node parent, dest;
	private String dir; 	//diagonal "d", straight "s", none "n"
	
	public Node(int x, int y, int dirG, int addG, int terG, Node parent, Node dest){
		this.gridX = x;
		this.gridY = y;
		this.dirG = dirG;
		this.addG = addG; 		//additional costs added to a node due to it being (or will be) occupied, etc.
		this.terG = terG;
		this.parent = parent;
		this.dest = dest;	
		if (parent == null){
			Gval = 0;
			parG = 0;
		}
		else{
			parG = parent.Gval;
		}
		Hval = dest==null ? 0 : Math.abs(gridX-dest.gridX)*10 + Math.abs(gridY-dest.gridY)*10;
		originalF = Hval + Gval; 	//without including the tweaked Gval
		setVals();
	}
	
	///////////////////////////////////////
	//Setter Methods
	///////////////////////////////////////
	public void setH(){
		if (dest != null){
			Hval = Math.abs(gridX-dest.gridX)*10 + Math.abs(gridY-dest.gridY)*10;
		}
	}
	
	public void setG(){
		Gval = parG + dirG + addG + terG;
	}
	
	public void setF(){
		Fval = Hval + Gval;
	}
	
	public void setVals(){
		setG();
		setF();
	}
	
	public void setParent(Node n){
		parent = n;
	}
	
	private String coordStr(int x, int y){
		return x + "," + y;
	}
	
	//Adds all neighboring nodes (including diagonals) to the open list as long as they
	//are not in the closed list and are walkable (0 or 2)
	//ter - terrain grid, occ - occupied grid (whether or not a grid is occupied by a unit)
	public void addNeighbours(ArrayList<Integer> walkable, int maxCost, SMap map, NodeList open,
							  HashSet<String> closed, Node dest, boolean complete){
		//terG -additional terrain cost for incomplete paths
		int[][] ter = map.getTerGrid();
		int[][] occ = map.getOccGrid();
		int[][] overG = map.getOverGGrid();
		Unit[][] unGrid = map.getUnitGrid();
		
		int mapX = map.getNumSquaresX();
		int mapY = map.getNumSquaresY();
		int additionalG, directionalG,terG; 	//additional G score due to the grid being occupied, etc
		for (int dx = -1; dx < 2; dx++){
			for (int dy = -1; dy < 2; dy++){
				if(map.withinGrid(gridX+dx, gridY+dy) && !closed.contains(coordStr(gridX+dx,gridY+dy)) &&
				  (occ[gridX+dx][gridY+dy] != 1 || !(unGrid[gridX+dx][gridY+dy].status().equals("standing")))){
					terG = 0;
					
					if (walkable.contains(ter[gridX+dx][gridY+dy])){
						directionalG = Math.abs(dx) == Math.abs(dy) ? 14 : 10;
						additionalG = occ[gridX+dx][gridY+dy] == 1 ? 12 : 0;
						if(!walkable.contains(5)){ 	//scv's don't need to worry about this extra G value
							additionalG += overG[gridX+dx][gridY+dy];
						}
						Node temp = new Node(gridX+dx,gridY+dy,directionalG,additionalG,terG,this,dest);
						if (temp.originalF < maxCost){
							if (open.contains(coordStr(gridX + dx, gridY + dy))){
								if (temp.getG() < open.get(gridX+dx,gridY+dy).getG()){
									open.replace(temp);
								}
							}
							else{
								open.insert(temp);
							}
						}
					}
				}
			}
		}
	}

	//Returns a Linked List of Point by linking each node to its parent starting from this
	//until reaches null (the starting Node)
	//NODES IN GRID COORDINATES
	public LinkedList<Point> createPath(SMap m, ArrayList<Integer> walkable){
		LinkedList ans = new LinkedList();
		Node cNode = this;
		Point temp;
		while (cNode != null){
			temp = new Point(cNode.gridX,cNode.gridY);
			ans.addFirst(temp);
			if(!walkable.contains(5)){ 		//only add G vals if not SCV
				m.addGVal(temp);
			}
			cNode = cNode.parent;
		}
		if (ans.size() > 0){
			ans.removeFirst();
		}
		return ans;
	}
	
	//Checks if two nodes are equal based on their coordinates
	public boolean equals(Node n){
		if (gridX == n.gridX && gridY == n.gridY){
			return true;
		}
		return false;
	}
	
	///////////////////////////////////////////
	//Accessor Methods
	///////////////////////////////////////////
	public int getX(){
		return gridX;
	}
	
	public int getY(){
		return gridY;
	}
	
	public int getG(){
		return Gval;
	}
	
	public int getF(){
		return Fval;
	}
	
	public int getOriginalF(){
		return originalF;
	}
	
	public int getH(){
		return Hval;
	}
	
	public Node getParent(){
		return parent;
	}
	
	//compares 2 nodes
	public int compareTo(Node other){ 	
		if (Fval >= other.Fval){
			return 1;
		}
		else if(Fval == other.Fval){
			return 0;
		}
		return -1;
	}
	
	//Returns a unique hashCode for Node
	public int hashCode(){
		String temp = gridX + "," + gridY;
		return temp.hashCode();
	}
	
	//Returns a point of its grid location
	public String getStrPoint(){
		return gridX + "," + gridY;
	}
	
	//Compares the location this node with other
	public boolean locEquals(Node other){
		return locEquals(other.gridX, other.gridY);
	}
	//Compares the location this node with other
	public boolean locEquals(int x, int y){
		return gridX == x && gridY == y;
	}
	
	public String toString(){
		return "(" + gridX + "," + gridY + ")" + " - Fval: " + Fval;
	}
}