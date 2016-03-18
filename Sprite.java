import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.MouseInfo;
import java.awt.geom.*;
import javax.swing.SwingUtilities.*;
import java.io.*;
public class Sprite{
	private LList<Image> list;
	private LNode<Image> head;
	private LNode<Image> current;
	private int delay;		//how many frames per image
	private int counter=0;
	private String stat;	//the stat that represent these images
	private boolean reverse;	//if true,after it reaches the end,list will get reversed(which means going back order)
	private int dir;		//what direction are these images facing
	private int camp;//0--player, 1--enemy
	private String type;
	public Sprite(Image[] im,String tp,String st,int dr,int cp,int dl){	
		list=new LList<Image>();
		for(int i=0;i<im.length;i++){
			list.add(im[i]);
		}
		list.reverse();
		head=list.getHead();
		stat=st;
		reverse=false;
		dir=dr;
		type=tp;
		camp=cp;
		delay=dl;
		reset();
	}
	public Sprite(LList<Image> l,String tp,String st,int dr,int cp,int dl){
		list=l;
		head=list.getHead();
		stat=st;
		reverse=false;
		dir=dr;
		type=tp;
		camp=cp;
		delay=dl;
		reset();
		
	}
	public void setLoop(int spot){	//set the pointer of the last sprite to the target spot
		if(spot!=-1){
			list.getLast().setNext(list.getAt(spot));
		}
		else{
			setReverse(true);
		}
	}
	public void setReverse(boolean b){	//turn on/off the reverse "switch"
		reverse=b;
	}
	public void reset(){	//return to te starting point
		current=head;
	}
	public Image push(){	//return the Image on the current spot and move on to the next spot
		if(reverse==false){
			if(counter<delay){		//if still in dealy(no need to move)
				counter+=1;
				return current.getValue(); 
			}
			else{
				counter=counter%delay;
				current=current.getNext();
				return current.getValue();
			}
		}
		else{
			if(counter<delay){
				counter+=1;
				return current.getValue(); 
			}
			else{
				counter=counter%delay;
				if(current.getNext()==null){
					list.reverse();				//if reverse is on and it reaches the bottom of the list, reverse the list
					return current.getValue();
				}
				else{
					current=current.getNext();
					return current.getValue();
				}
			}
		}
	}
	public int getDir(){
		return dir;
	}
	public int getCamp(){
		return camp;
	}
	public String getType(){
		return type;
	}
	public boolean equals(String s,int dr,int cp,String tp){		//check if this object has all the properties(stat, dir, cp,type)
		if(stat==null){				//if stat==null then do not take stat into consideration(for something such as minPieces)
			return (dir==dr||dir==-1)&&cp==camp&&type.equals(tp);
		}
		return s.equals(stat)&&(dir==dr||dir==-1)&&cp==camp&&type.equals(tp);
	}
	public Sprite clone(){		//clone the object itself
		Sprite ans=new Sprite(list,type,stat,dir,camp,delay);
		return ans;
	}
}