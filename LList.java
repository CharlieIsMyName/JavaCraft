

public class LList<T>{
	private LNode head, tail; 
	private int length; 		//The number of nodes in the list.
	
	//The head and tail start off as null with the list having length 0.
	public LList(){
		head = null;
		tail = null;
		length = 0;
	}
	
	//Adds a new value to the list.
	public void add(T n){
		if (length == 0){ 		//When length is 0, both the head and tail become the same node.
			LNode tmp = new LNode(n, null, null);
			head = tmp;
			tail = tmp;
		}
		else{ 					//To add a new value when length > 0, make it the new head, and adjust
			LNode temp = head;	//the old head to point to this new head.
			head = new LNode(n, null, head);
			temp.setPre(head);
		}
		length += 1;
	}
	
	//Adds a new element to the top of the list.
	public void push (T n){
		add(n);
	}
	
	//Remove the top element (the head) from the list and returns its value
	public T pop(){
		//If there are no elements in the list, this method will cause the program to crash!
		T x = (T)head.getValue();
		head = head.getNext();
		if (length == 1){
			//The head is already null if length is 1, but tail still isn't.
			tail = null;
		}
		else{
			//If the new head is actually a node, then make its pre null.	
			head.setPre(null);
		}
		length -= 1;
		return x;
	}
	
	
	//Adds a new element to the back of the list (it becomes the new tail).
	public void enqueue (T n){
		if (length == 0){ 	//If there's no elements, then there's no "back of the list"
			add(n);
		}
		else{		
			LNode temp = new LNode(n,tail,null);	
			tail.setNext(temp);
			tail = temp;
			length += 1;
		}
	}
	
	/*
	//Removes the element at the front of the queue (the head) and returns its value.
	public T dequeue (){
		return pop();
	}
	*/
	
	//Removes the first node in the list that has value n.
	//If no node has value n, nothing happens to the list.
	public void delete (T n){
		LNode curNode = head;
		while (curNode != null){
			if (curNode.getValue().equals(n)){
				delete(curNode);
				break;
			}
			curNode = curNode.getNext();
		}
	}
	
	//Deletes a node from the list. It is assumed that the node is actually in the list.
	 public void delete (LNode node){
		if (length == 0){		//Just for good measure... although technically
			return;				//I said that I'd assume that the list contains the node
		}
		else if (length == 1){ 	//The node we are deleting is the head
			head = null;
			tail = null;
			length = 0;
		}
		else{
			if (node == head){
				head = head.getNext();
				head.setPre(null);
			}
			else if (node == tail){
				tail = tail.getPre();
				tail.setNext(null);
			}
			else{
				LNode previous = node.getPre();
				previous.setNext(node.getNext());
				previous.getNext().setPre(previous);
			}
			length -= 1;
		}
	}
	
	//Deletes the value at a given position in the list, where position 0 is head.
	//If the position is invalid (negative or >= length), nothing happens to the list.
	public void deleteAt (int pos){
		if (pos >= length || pos < 0){
			return;
		}
		LNode curNode = head;
		for (int i = 0; i < pos; i++){
			curNode = curNode.getNext();
		}
		delete(curNode);
	}
	public LNode<T> getAt(int pos){
		LNode curNode = head;
		for (int i = 0; i < pos; i++){
			curNode = curNode.getNext();
		}
		return curNode;
	}
	//Removes all nodes that share the same value with a previous node in the list.
	public void removeDuplicates(){
		if (length == 0){ 						//No duplicates to worry about here
			return;
		}
		LNode curNode = head;
		while(curNode.getNext() != null){ 		//If we're at the end of the list, then there can't be
			T val = (T)curNode.getValue();		//any more duplicates
			LNode temp = curNode;
			while(temp.getNext() != null){
				temp = temp.getNext();
				if (temp.getValue().equals(val)){
					delete(temp);
				}
			}
			if (curNode.getNext() != null){		//Although this condition was already checked at the beginning of the
				curNode = curNode.getNext();	//loop, the list may have been modified, and curNode might have become
			}									//the last element in the list.
		}
	}
	
	//Reverses the order of the nodes in the list, and flips head and tail.
	public void reverse(){
		if (length <= 1){  			//Simple enough when theres 0 or 1 node
			return;
		}
		LNode curNode = head;
		while (curNode != null){
			LNode temp = curNode.getNext();
			curNode.setNext(curNode.getPre());
			curNode.setPre(temp);
			curNode = temp;
		}
		//Flip head and tail
		LNode tempHead = head;
		head = tail;
		tail = tempHead;
	}
	
	//Returns a copy of the list (in its exact order).
	//Changes to the copy will not affect this list and changes to this list
	//will not affect the copy.
	public LList clone(){
		LList ans = new LList();
		LNode curNode = tail;				//Start at the tail so ans has the same order
		while (curNode != null){			//as this List after each Node is added
			ans.add(curNode.getValue());
			curNode = curNode.getPre();
		}
		return ans;
	}
	
	//Prints the numbers contained in the list, starting from the head (FILO order).
	public void display(){
		LNode tmp = head;
		while (tmp != null){
			System.out.println(tmp.getValue());
			tmp = tmp.getNext();
		}
	}
	public LNode<T> getHead(){
		return head;
	}
	public LNode<T>	getLast(){
		LNode<T> tem=head;
		while(true){
			if(tem==null){
				return null;
			}
			if(tem.getNext()==null){
				return tem;
			}
			else{
				tem=tem.getNext();
			}
		}
	}
}