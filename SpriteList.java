import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.MouseInfo;
import java.awt.geom.*;
import javax.swing.SwingUtilities.*;
import java.io.*;
public class SpriteList{
	private Sprite[] list;	//the actual list that is storing the sprite object inside this spriteList
	private boolean same;
	public SpriteList(Sprite[] sp){
		list=new Sprite[sp.length];
		for (int i=0;i<sp.length;i++){
			list[i]=sp[i];
		}
	}
	public SpriteList(){
		list=new Sprite[0];
	}
	public Image getImage(String[] stat,int dr,int cp,String tp){	//get the image with the reqire properties
		same=stat[0].equals(stat[1]);
		if(same==false){
			reset();	//if the stat changed then every sprite returns to the beginning since i dont want to start from the middle of a set of sprite
		}
		for (int i=0;i<list.length;i++){
			if(list[i].equals(stat[1],dr,cp,tp)==true){
				return list[i].push();
			}
		}
		return null;
	}
	public Image getFirst(){		//get the image of the first sprite in the list(testing purpose)
		if(list.length==0){
			return null;
		}
		else{
			return list[0].push();
		}
	}
	public Sprite lastSpr(){	//get the last sprite object in the list
		return list[list.length-1];
	}
	public void reset(){	//reset all the sprite object in the list
		for(int i=0;i<list.length;i++){
			list[i].reset();
		}
	}	
	public void add(Sprite s){	//add a sprite object to the list
		Sprite[] l=new Sprite[list.length+1];
		for(int i=0;i<list.length;i++){
			l[i]=list[i];
		}
		l[l.length-1]=s;
		list=l;
	}
	public int length(){
		return list.length;
	}
	public SpriteList clone(){	//clone the spriteList itself and the objects inside
		SpriteList ans=new SpriteList();
		for(Sprite s:list){
			ans.add(s.clone());
		}
		return ans;
	}
}