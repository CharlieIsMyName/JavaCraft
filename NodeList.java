//June 17, 2014
//Starcraft Project
//by: Charlie Wang, Kassem Bazzi
//March 31, 2014

//Stores nodes into a binary heap so that retrieving the Node with lowest Fcost is
//most efficent.

import java.util.*;
import java.awt.*;
public class NodeList{
	private Node[] heap;
	private int lastIndex;
	private Map<String,Node> nodesAdded; 	//The string that points to nodes in the map is of the form "x,y"
	private int cap; 	//capacity of the heap
	
	public NodeList(int cap){
		heap = new Node[cap+1];
		nodesAdded = new HashMap<String,Node>(); 
		lastIndex = 0;
		this.cap = cap;
	}
	
	//Returns the number of elements in the list -- O(1)
	public int size(){
		return lastIndex;
	}
	
	//O(1)
	public boolean isEmpty(){
		return lastIndex == 0;
	}
	
	//Doesn't remove it, just returns first element in the heap -- O(1)
	public Node getFirst(){
		if (lastIndex == 0){
			return null;
		}
		return heap[1];
	}
	
	//O(1)
	public int compare(Node a, Node b){
		return a.compareTo(b);
	}
	
	//Inserts a node into the heap -- O(lgn)
	public void insert(Node node){
		if (lastIndex == cap){
			return;
		}
		lastIndex += 1;
		heap[lastIndex] = node;
		bubbleUp();
		nodesAdded.put(node.getStrPoint(), node);
	}

	//removes and returns the first node in the heap -- O(lgn)
	public Node removeFirst(){
		if (lastIndex == 0){
			return null;
		}
		else{
			Node first = heap[1];
			heap[1] = heap[lastIndex];
			lastIndex -= 1;
			bubbleDown();
			nodesAdded.remove(first.getStrPoint());
			return first;
		}
	}
	
	//Used with insert to re-order nodes in the heap to preserve
	//binary heap nature -- O(lgn)
	private void bubbleUp(){
		int index = lastIndex;
		while (index > 1){
			int parent = index/2;
			if (heap[index].compareTo(heap[parent]) >= 0){
				break;
			}
			swap(index, parent);
			index = parent;
		}
	}
	
	//Swaps node at a with node at b -- O(1)
	private void swap(int a, int b){
		Node temp = heap[b];
		heap[b] = heap[a];
		heap[a] = temp;
	}
	
	//Used with remove to re-order nodes in the heap after
	//taking out the first node -- O(lgn)
	private void bubbleDown(){
		int index = 1;
		while (true){
			int child = index*2; 	//child 1
			if (child > lastIndex){
				break;
			}
			if (child + 1 <= lastIndex){ 	//if not, then child has to be child, otherwise must compare 2 children
				child = lowerChild(child,child+1);
			}
			if (heap[index].compareTo(heap[child]) <= 0){
				break;
			}
			swap(index,child);
			index = child;
		}
	}
	
	//Returns the index with the lower Fscore when comparing 2 indices -- O(1)
	private int lowerChild(int a, int b){
		return heap[a].compareTo(heap[b])==-1 ? a : b;
	}
	
	//Returns if the NodeList has a node with the same (x,y) as n -- O(1)
	public boolean contains(String pt){
		return nodesAdded.containsKey(pt);
	}
	
	//Gets the node with coordinates (x,y) -- O(1)
	public Node get(int x, int y){
		return nodesAdded.get(x+","+y);
	}
	
	//Replaces the current node wit  n's coordinates with n
	//Is this ok..? -- O(n)
	public void replace(Node n){
		nodesAdded.remove(n.getStrPoint());
		nodesAdded.put(n.getStrPoint(), n);
		for (int i=1; i<=lastIndex; i++){
			if (heap[i].locEquals(n)){
				heap[i] = n;
				break;
			}
		}
	}
	
	public String toString(){
		String ans = "{";
		for (Node n : heap){
			//ans += "(" + n.getX() + "," + n.getY() + ")";
			ans += n;
		}
		return ans + "}";
	}
}