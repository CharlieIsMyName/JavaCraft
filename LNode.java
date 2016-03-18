

public class LNode<T>{
	private T value;
	private LNode next, pre;
	
	public LNode(T v, LNode p, LNode n){
		value = v;
		pre = p;
		next = n;
	}
	
	public void setNext(LNode node){
		next = node;
	}
	
	public void setPre (LNode node){
		pre = node;
	}
	
	public T getValue(){
		return value;
	}
	
	public LNode getNext(){
		return next;
	}
	
	public LNode getPre(){
		return pre;
	}
}